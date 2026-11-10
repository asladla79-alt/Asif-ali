package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PaymentOrder
import com.example.ui.theme.*
import com.example.utils.QRGenerator
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositScreen(
    currentOrder: PaymentOrder?,
    minDeposit: Int,
    upiId: String,
    storeName: String,
    isVerifying: Boolean,
    errorMessage: String?,
    onStartOrder: (Int) -> Unit,
    onVerifyPayment: (orderId: String, utr: String, onDone: (Boolean) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var amountInput by remember { mutableStateOf("49") }
    var phoneInput by remember { mutableStateOf("") }
    var utrInput by remember { mutableStateOf("") }
    var secondsLeft by remember { mutableIntStateOf(900) } // 15 min window (900 sec)

    // Countdown timer for active order
    LaunchedEffect(currentOrder?.orderId) {
        if (currentOrder != null && currentOrder.status == "pending") {
            secondsLeft = 900
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
            }
        }
    }

    val upiUri = remember(currentOrder, upiId, storeName) {
        if (currentOrder != null) {
            QRGenerator.buildUpiUri(upiId, storeName, currentOrder.amount, currentOrder.orderId)
        } else ""
    }

    val qrBitmap = remember(upiUri) {
        if (upiUri.isNotEmpty()) {
            QRGenerator.generateQRMatrix(upiUri, size = 512)
        } else null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AUTO UPI DEPOSIT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("100% Auto-Receive via FamPay & Gmail IMAP", fontSize = 11.sp, color = CodeForgeCyan)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("deposit_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg, titleContentColor = TextPrimary)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentOrder == null || currentOrder.status == "success") {
                // Deposit creation form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, PrimaryGradient, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR", tint = CodeForgeCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERATE INSTANT UPI QR", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Amount (Min ₹$minDeposit)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { if (it.all { ch -> ch.isDigit() }) amountInput = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("deposit_amount_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick buttons for popular amounts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(49, 99, 149, 299).forEach { preset ->
                                OutlinedButton(
                                    onClick = { amountInput = preset.toString() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(4.dp)
                                ) {
                                    Text("₹$preset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Phone Number (+91)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            placeholder = { Text("e.g. 9876543210") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone", tint = TextMuted) },
                            modifier = Modifier.fillMaxWidth().testTag("deposit_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val amt = amountInput.toIntOrNull() ?: minDeposit
                                if (amt >= minDeposit) {
                                    onStartOrder(amt)
                                } else {
                                    Toast.makeText(context, "Minimum deposit is ₹$minDeposit", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_qr_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = "Generate")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERATE PAYMENT QR", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Active Order / QR Screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, PrimaryGradient, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Timer & Order Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("ORDER ID", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                Text(currentOrder.orderId, fontSize = 13.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = CodeForgeCyan)
                            }

                            // 15 Min countdown badge
                            val minutes = secondsLeft / 60
                            val seconds = secondsLeft % 60
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (secondsLeft > 120) CodeForgePurple.copy(alpha = 0.25f) else Color.Red.copy(alpha = 0.25f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = if (secondsLeft > 120) Color(0xFFD0BCFF) else Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("%02d:%02d", minutes, seconds),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (secondsLeft > 120) Color(0xFFD0BCFF) else Color(0xFFFF5252),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code container
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "UPI Payment QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Amount: ₹${currentOrder.amount}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        Text(
                            text = "Pay to: $upiId ($storeName)",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick action: Open UPI App or Copy UPI Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("UPI URI", upiUri))
                                    Toast.makeText(context, "UPI Link Copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "No UPI App found", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CodeForgeGreen)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay via App", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSurfaceBorder))
                        Spacer(modifier = Modifier.height(16.dp))

                        // UTR Input section (Optional / Auto Gmail check)
                        Text(
                            text = "ENTER 12-DIGIT UTR (OPTIONAL)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = utrInput,
                            onValueChange = { utrInput = it },
                            placeholder = { Text("e.g. 6749353033 or FMPIB...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("deposit_utr_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (errorMessage != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Red.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = Color(0xFFFF5252), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(errorMessage, fontSize = 12.sp, color = Color(0xFFFF8A80))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // I Have Paid Button -> Triggers Auto Verify (Gmail IMAP + Render)
                        Button(
                            onClick = {
                                onVerifyPayment(currentOrder.orderId, utrInput) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Payment Verified Successfully! Balance Added.", Toast.LENGTH_LONG).show()
                                        onBack()
                                    }
                                }
                            },
                            enabled = !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("i_have_paid_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Checking Gmail IMAP noreply@famapp.in...")
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Paid")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("I HAVE PAID (AUTO VERIFY)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Information & Trust Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("⚡ Real-Time Auto Receiver Info", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CodeForgeCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. After scanning QR & paying in FamPay / PhonePe / Paytm, tap 'I Have Paid'.\n2. The Python gateway searches Gmail via IMAP (noreply@famapp.in) and verifies UTR & Amount.\n3. Balance is credited instantly to your account without human delays.",
                        fontSize = 11.sp,
                        color = TextMuted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
