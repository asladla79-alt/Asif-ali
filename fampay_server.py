"""
=============================================================================
CODEFORGE PRO (CodeXAs) - Auto Payment Server for Render
=============================================================================
Deploy on Render.com as a Web Service.
Configured with:
- Store: CodeXAs STORE
- UPI: ali00007@fam
- Gmail Auto-Verification: asladla79@gmail.com (IMAP: imap.gmail.com:993)
- Gmail App Password: dqli czwh vpzt sbdq
- Firebase Realtime Database: https://codexas-07-default-rtdb.firebaseio.com
=============================================================================
"""

import html as _html
import imaplib
import io
import json
import logging
import os
import re
import secrets
import socket
import sqlite3
import ssl
import threading
import time
import urllib.parse
from dataclasses import dataclass, field
from datetime import datetime, timedelta, timezone
from decimal import Decimal, InvalidOperation
from email import message_from_bytes
from email.header import decode_header, make_header
from email.utils import parseaddr, parsedate_to_datetime
from typing import Callable, Optional

from flask import Flask, request, jsonify

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
log = logging.getLogger("fampay_gateway")

app = Flask(__name__)

# ----------------------------------------------------------------------------
# Config
# ----------------------------------------------------------------------------

@dataclass(frozen=True)
class Config:
    gmail_address: str = "asladla79@gmail.com"
    gmail_app_password: str = "dqli czwh vpzt sbdq"
    upi_id: str = "ali00007@fam"
    store_name: str = "CodeXAs STORE"

    imap_server: str = "imap.gmail.com"
    imap_port: int = 993
    imap_timeout: int = 20
    lookback_days: int = 3
    max_fetch: int = 10
    max_mail_bytes: int = 3_000_000

    sender_domains: tuple = ("famapp.in", "famapp.com", "fampay.in", "famapp.co", "phonepe.com")
    require_auth: bool = False  # Set to True on strict Google SPF/DKIM validation

    order_window: int = 900              # QR 15 min valid
    late_utr_window: int = 86400         # 24h UTR submit
    clock_skew: int = 300
    max_payment_age: int = 7200
    email_max_delay: int = 3600
    verify_max_age: int = 21600          # 6 hours
    max_utr_tries: int = 5
    max_open_orders: int = 5
    min_amount: int = 10
    max_amount: int = 50000

    worker_interval: int = 15
    retry_fast: int = 30
    retry_slow: int = 300

    breaker_threshold: int = 3
    breaker_pause: int = 600

    @classmethod
    def from_env(cls, **overrides) -> "Config":
        base = dict(
            gmail_address=os.getenv("GMAIL_ADDRESS", "asladla79@gmail.com").strip(),
            gmail_app_password=os.getenv("GMAIL_APP_PASSWORD", "dqli czwh vpzt sbdq").strip(),
            upi_id=os.getenv("UPI_ID", "ali00007@fam").strip(),
            store_name=os.getenv("STORE_NAME", "CodeXAs STORE").strip(),
        )
        base.update(overrides)
        return cls(**base)

    @property
    def configured(self) -> bool:
        return bool(self.gmail_address and self.gmail_app_password and self.upi_id)


# ----------------------------------------------------------------------------
# UTR Helpers & Normalization
# ----------------------------------------------------------------------------
_FAMPAY_ID_RX = re.compile(r"FMPIB[A-Z0-9]{4,35}|FAM[A-Z0-9]{9,35}", re.I)

def normalize_utr(raw) -> Optional[str]:
    if raw is None:
        return None
    s = str(raw).strip()
    s = re.sub(r"(?i)^(?:fam\s*pay|fam\s*app|fam|utr|upi\s*ref|upi\s*txn|txn|transaction|ref)"
               r"[\s\-_]*(?:transaction|txn|upi|ref)?[\s\-_]*(?:id|no|number|num)?\.?\s*[:#\-]\s*", "", s)
    s = re.sub(r"(?i)^utr\s+(?:(?:id|no|number|num)\.?\s+)?", "", s)
    s = re.sub(r"[\s\-_]", "", s).upper()
    s = re.sub(r"^UTR(?=\d{8,})", "", s)
    if not re.fullmatch(r"[A-Z0-9]{8,40}", s) or not re.search(r"\d", s):
        return None
    m = re.fullmatch(r"FMPIB(\d{6,14})", s)
    return m.group(1) if m else s

def utr_aliases(utr) -> list:
    u = str(utr or "").strip().upper()
    out = [u]
    m = re.fullmatch(r"FMPIB(\d{6,14})", u)
    if m:
        out.append(m.group(1))
    elif re.fullmatch(r"\d{6,14}", u):
        out.append("FMPIB" + u)
    return out

# ----------------------------------------------------------------------------
# Email Parsing
# ----------------------------------------------------------------------------
def _decode_header_value(v) -> str:
    try:
        return str(make_header(decode_header(v or "")))
    except Exception:
        return str(v or "")

def _email_text(msg) -> str:
    chunks = []
    for part in msg.walk():
        if part.get_content_maintype() == "multipart":
            continue
        ctype = part.get_content_type()
        if ctype not in ("text/plain", "text/html"):
            continue
        try:
            payload = part.get_payload(decode=True)
            if payload:
                txt = payload.decode(part.get_content_charset() or "utf-8", errors="replace")
                chunks.append(txt)
        except Exception:
            continue
    return "\n".join(chunks)[:200_000]

_CUR = r"(?:₹|\brs\.?|\binr\b)"
_NUM = r"([0-9][0-9,]*(?:\.[0-9]{1,2})?)"
_AMT_KEYED = (
    re.compile(r"(?:received|credited|credit|payment\s+of|paid|deposited|amount)[^0-9₹\n]{0,40}?" + _CUR + r"\s*" + _NUM, re.I),
    re.compile(_CUR + r"\s*" + _NUM + r"\s*(?:has\s+been\s+|was\s+)?(?:received|credited|paid)", re.I),
)

def parse_amount_paise(text: str) -> Optional[int]:
    for rx in _AMT_KEYED:
        for m in rx.finditer(text):
            try:
                d = Decimal(m.group(1).replace(",", "")) * 100
                return int(d)
            except Exception:
                pass
    return None

def utr_in_text(text: str, utr: str) -> bool:
    upper = text.upper()
    for alias in utr_aliases(utr):
        if alias.upper() in upper:
            return True
    return False

# ----------------------------------------------------------------------------
# Gmail IMAP Client
# ----------------------------------------------------------------------------
class GmailVerifier:
    def __init__(self, cfg: Config):
        self.cfg = cfg

    def search_utr(self, utr: str, expected_amount: int):
        clean_pass = self.cfg.gmail_app_password.replace(" ", "").strip()
        try:
            ctx = ssl.create_default_context()
            M = imaplib.IMAP4_SSL(self.cfg.imap_server, self.cfg.imap_port, ssl_context=ctx, timeout=self.cfg.imap_timeout)
            M.login(self.cfg.gmail_address, clean_pass)
            M.select("INBOX", readonly=True)

            # Search recent emails
            typ, data = M.search(None, "ALL")
            if typ != "OK" or not data or not data[0]:
                M.logout()
                return False, "No emails found in inbox"

            msg_ids = data[0].split()[-20:]  # check latest 20 emails
            for mid in reversed(msg_ids):
                t, mdata = M.fetch(mid, "(BODY.PEEK[])")
                if t == "OK" and mdata and isinstance(mdata[0], tuple):
                    raw = mdata[0][1]
                    msg = message_from_bytes(raw)
                    body = _email_text(msg)
                    if utr_in_text(body, utr):
                        amt_paise = parse_amount_paise(body)
                        M.logout()
                        if amt_paise is not None and amt_paise == (expected_amount * 100):
                            return True, "Payment verified with exact amount and UTR"
                        return True, "Payment verified with UTR match"
            M.logout()
            return False, "UTR not found in recent payment emails"
        except Exception as e:
            log.error(f"IMAP Error: {e}")
            return False, f"IMAP exception: {str(e)[:100]}"

cfg = Config.from_env()
verifier = GmailVerifier(cfg)

# SQLite database for caching payment orders and preventing duplicate UTRs
DB_PATH = "payments.db"

def init_db():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("""CREATE TABLE IF NOT EXISTS payment_utrs (
        utr TEXT PRIMARY KEY,
        order_id TEXT,
        amount INTEGER,
        verified_at INTEGER
    )""")
    conn.commit()
    conn.close()

init_db()

@app.route("/")
def home():
    return jsonify({
        "status": "online",
        "service": "CODEFORGE PRO - Auto Payment Receiver Gateway",
        "store": cfg.store_name,
        "upi_id": cfg.upi_id,
        "gmail": cfg.gmail_address
    })

@app.route("/verify_payment", methods=["POST"])
def verify_payment():
    data = request.get_json(force=True) or {}
    order_id = data.get("order_id", "").strip()
    utr = normalize_utr(data.get("utr", ""))
    amount = int(data.get("amount", 0))

    if not order_id:
        return jsonify({"success": False, "message": "order_id is required"}), 400

    if not utr:
        return jsonify({"success": False, "message": "Valid 10-12 digit UTR is required"}), 400

    # Check duplicate UTR
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    cur.execute("SELECT 1 FROM payment_utrs WHERE utr = ?", (utr,))
    if cur.fetchone():
        conn.close()
        return jsonify({"success": False, "status": "duplicate_utr", "message": "Duplicate UTR! Already used."}), 400

    # Run Gmail IMAP verification
    ok, reason = verifier.search_utr(utr, amount)
    if ok:
        cur.execute("INSERT OR REPLACE INTO payment_utrs (utr, order_id, amount, verified_at) VALUES (?, ?, ?, ?)",
                    (utr, order_id, amount, int(time.time())))
        conn.commit()
        conn.close()
        return jsonify({
            "success": True,
            "status": "verified",
            "order_id": order_id,
            "utr": utr,
            "amount": amount,
            "message": "Payment verified via FamPay Gmail IMAP successfully!"
        })
    else:
        conn.close()
        # Even if email is slightly delayed, if user enters genuine format UTR, return clear message
        return jsonify({
            "success": False,
            "status": "not_found",
            "message": f"Payment not received yet ({reason}). Please wait 1-2 minutes or retry."
        }), 200

if __name__ == "__main__":
    port = int(os.environ.get("PORT", 5000))
    app.run(host="0.0.0.0", port=port)
