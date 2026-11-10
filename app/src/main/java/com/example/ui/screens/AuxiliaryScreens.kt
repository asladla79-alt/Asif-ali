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
import com.example.model.AppNotification
import com.example.model.Transaction
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    templates: List<Pair<String, Map<String, String>>>,
    onCreate: (name: String, pkg: String, template: String) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pkg by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf(templates.firstOrNull()?.first ?: "Blank") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NEW WEB TO APK PROJECT", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_back_button")) {
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, PrimaryGradient, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("PROJECT CONFIGURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeForgeCyan)

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (pkg.isBlank() || pkg.startsWith("com.editing.")) {
                                pkg = "com.editing." + it.lowercase().replace(" ", "").filter { ch -> ch.isLetterOrDigit() }
                            }
                        },
                        label = { Text("Project / App Name") },
                        placeholder = { Text("e.g. My NextGen App") },
                        modifier = Modifier.fillMaxWidth().testTag("new_project_name_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = pkg,
                        onValueChange = { pkg = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.editing.myapp") },
                        modifier = Modifier.fillMaxWidth().testTag("new_project_pkg_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Text("CHOOSE STARTER TEMPLATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

            templates.forEach { (tplName, files) ->
                val isSelected = tplName == selectedTemplate
                Card(
                    onClick = { selectedTemplate = tplName },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            brush = if (isSelected) PrimaryGradient else androidx.compose.ui.graphics.Brush.linearGradient(listOf(DarkSurfaceBorder, DarkSurfaceBorder)),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(tplName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (isSelected) CodeForgeCyan else TextPrimary)
                            Text("${files.size} source files pre-configured (index.html ready)", fontSize = 11.sp, color = TextMuted)
                        }
                        RadioButton(selected = isSelected, onClick = { selectedTemplate = tplName })
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val finalName = name.ifBlank { "My Pro App" }
                    val finalPkg = pkg.ifBlank { "com.editing.myapp" }
                    onCreate(finalName, finalPkg, selectedTemplate)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_create_project_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
                Spacer(modifier = Modifier.width(8.dp))
                Text("INITIALIZE & OPEN EDITOR", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<AppNotification>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NOTIFICATIONS", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            notifications.forEach { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                            Text(
                                java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notif.timestamp)),
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(notif.message, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    transactions: List<Transaction>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TRANSACTIONS & BUILDS", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            transactions.forEach { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(if (tx.amount > 0) "UPI Deposit (Auto Verified)" else "App Publishing Fee", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("UTR: ${tx.utr}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CodeForgeCyan)
                            Text("${tx.orderId} • ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(tx.timestamp))}", fontSize = 10.sp, color = TextMuted)
                        }
                        Text(
                            text = if (tx.amount > 0) "+₹${tx.amount}" else "₹${tx.amount}",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = if (tx.amount > 0) CodeForgeGreen else Color(0xFFFF5252)
                        )
                    }
                }
            }
        }
    }
}
