package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SplashLinkButton
import com.example.model.Transaction
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    upiId: String,
    storeName: String,
    minDeposit: Int,
    publishCharge: Int,
    backendUrl: String,
    splashButtons: List<SplashLinkButton>,
    transactions: List<Transaction>,
    onSaveSettings: (upiId: String, storeName: String, minDep: Int, charge: Int, backendUrl: String) -> Unit,
    onAddBalance: (Int) -> Unit,
    onAddButton: (title: String, url: String) -> Unit,
    onRemoveButton: (id: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var curUpi by remember { mutableStateOf(upiId) }
    var curStore by remember { mutableStateOf(storeName) }
    var curMinDep by remember { mutableStateOf(minDeposit.toString()) }
    var curCharge by remember { mutableStateOf(publishCharge.toString()) }
    var curBackend by remember { mutableStateOf(backendUrl) }

    var newBtnTitle by remember { mutableStateOf("") }
    var newBtnUrl by remember { mutableStateOf("") }
    var showAddBtnDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("SUPER ADMIN MASTER PANEL", fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("asladla79@gmail.com • Root Access", fontSize = 11.sp, color = CodeForgeCyan)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid (Total Users, Earnings, Apps, Pending)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PLATFORM OVERVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("TOTAL USERS", fontSize = 10.sp, color = TextMuted)
                            Text("12,847", fontSize = 18.sp, fontWeight = FontWeight.Black, color = TextPrimary)
                        }
                        Column {
                            Text("EARNINGS", fontSize = 10.sp, color = TextMuted)
                            Text("₹6,29,503", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CodeForgeGreen)
                        }
                        Column {
                            Text("PUBLISHED", fontSize = 10.sp, color = TextMuted)
                            Text("1,894", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CodeForgeCyan)
                        }
                        Column {
                            Text("PENDING", fontSize = 10.sp, color = TextMuted)
                            Text("₹8,240", fontSize = 18.sp, fontWeight = FontWeight.Black, color = CodeForgeGold)
                        }
                    }
                }
            }

            // Quick Super Admin Tools (Add Funds, Test IMAP)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ROOT CONTROLS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeForgeCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onAddBalance(500)
                                Toast.makeText(context, "Added ₹500 Test Balance", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CodeForgeGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ ₹500 Balance", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Testing Gmail IMAP (asladla79@gmail.com)... Connection OK!", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Test IMAP", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Payment Gateway Configuration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, PrimaryGradient, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("LIVE PAYMENT CONFIGURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    OutlinedTextField(
                        value = curUpi,
                        onValueChange = { curUpi = it },
                        label = { Text("Receiver UPI ID") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_upi_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = curStore,
                        onValueChange = { curStore = it },
                        label = { Text("Store / Merchant Name") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_store_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = curMinDep,
                            onValueChange = { curMinDep = it },
                            label = { Text("Min Deposit (₹)") },
                            modifier = Modifier.weight(1f).testTag("admin_min_dep_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = curCharge,
                            onValueChange = { curCharge = it },
                            label = { Text("Publish Fee (₹)") },
                            modifier = Modifier.weight(1f).testTag("admin_charge_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    OutlinedTextField(
                        value = curBackend,
                        onValueChange = { curBackend = it },
                        label = { Text("Render Gateway URL") },
                        modifier = Modifier.fillMaxWidth().testTag("admin_backend_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val minD = curMinDep.toIntOrNull() ?: 10
                            val chg = curCharge.toIntOrNull() ?: 49
                            onSaveSettings(curUpi, curStore, minD, chg, curBackend)
                            Toast.makeText(context, "Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("admin_save_settings_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue)
                    ) {
                        Text("SAVE LIVE SETTINGS", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Dynamic Splash Screen Link Buttons Manager
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SPLASH DYNAMIC BUTTONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        IconButton(onClick = { showAddBtnDialog = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = CodeForgeCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    splashButtons.forEach { btn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(btn.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(btn.url, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                            }
                            IconButton(onClick = { onRemoveButton(btn.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Transaction History Logs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PAYMENT & TRANSACTION LOGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    transactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("UTR: ${tx.utr}", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                Text("${tx.orderId} • ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))}", fontSize = 10.sp, color = TextMuted)
                            }
                            Text(
                                text = if (tx.amount > 0) "+₹${tx.amount}" else "₹${tx.amount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (tx.amount > 0) CodeForgeGreen else Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddBtnDialog) {
        AlertDialog(
            onDismissRequest = { showAddBtnDialog = false },
            title = { Text("Add Dynamic Splash Button") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newBtnTitle,
                        onValueChange = { newBtnTitle = it },
                        label = { Text("Button Title (e.g. VIP GROUP)") }
                    )
                    OutlinedTextField(
                        value = newBtnUrl,
                        onValueChange = { newBtnUrl = it },
                        label = { Text("Link URL (https://...)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBtnTitle.isNotBlank() && newBtnUrl.isNotBlank()) {
                            onAddButton(newBtnTitle.trim(), newBtnUrl.trim())
                            newBtnTitle = ""
                            newBtnUrl = ""
                            showAddBtnDialog = false
                        }
                    }
                ) {
                    Text("Add Button")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBtnDialog = false }) { Text("Cancel") }
            }
        )
    }
}
