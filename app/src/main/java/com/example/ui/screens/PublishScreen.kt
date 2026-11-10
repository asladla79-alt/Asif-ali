package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    project: Project,
    balance: Int,
    publishCharge: Int,
    onPublishSuccess: (appName: String, packageName: String, version: String) -> Unit,
    onNavigateDeposit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var appNameInput by remember { mutableStateOf(project.name) }
    var pkgInput by remember { mutableStateOf(project.packageName) }
    var versionInput by remember { mutableStateOf(project.version) }
    var enablePushNotif by remember { mutableStateOf(true) }
    var enableAdmob by remember { mutableStateOf(false) }
    var admobAppId by remember { mutableStateOf("") }
    var isBuilding by remember { mutableStateOf(false) }
    var buildProgress by remember { mutableFloatStateOf(0f) }
    var buildStage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PUBLISH REAL APK / AAB", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("publish_back_button")) {
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
            // Balance & Fee Notice
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, PrimaryGradient, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PUBLISH CHARGE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text("₹$publishCharge", fontSize = 24.sp, fontWeight = FontWeight.Black, color = CodeForgePink)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("YOUR BALANCE", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(
                            text = "₹$balance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= publishCharge) CodeForgeGreen else Color(0xFFFF5252)
                        )
                    }
                }
            }

            if (balance < publishCharge) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Red.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Low Balance! Please deposit ₹${publishCharge - balance} to build APK.", fontSize = 12.sp, color = Color(0xFFFF8A80), modifier = Modifier.weight(1f))
                        Button(
                            onClick = onNavigateDeposit,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Deposit Now", fontSize = 12.sp)
                        }
                    }
                }
            }

            // APK Details Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("APP SPECIFICATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CodeForgeCyan)

                    OutlinedTextField(
                        value = appNameInput,
                        onValueChange = { appNameInput = it },
                        label = { Text("App Name") },
                        modifier = Modifier.fillMaxWidth().testTag("publish_app_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = pkgInput,
                        onValueChange = { pkgInput = it },
                        label = { Text("Package Name") },
                        placeholder = { Text("com.editing.myapp") },
                        modifier = Modifier.fillMaxWidth().testTag("publish_pkg_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = versionInput,
                        onValueChange = { versionInput = it },
                        label = { Text("Version") },
                        placeholder = { Text("1.0 Pro") },
                        modifier = Modifier.fillMaxWidth().testTag("publish_version_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Options: Push Notifications & AdMob
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable Push Notifications (Firebase)", fontSize = 13.sp)
                        Switch(checked = enablePushNotif, onCheckedChange = { enablePushNotif = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Monetize with Google AdMob", fontSize = 13.sp)
                        Switch(checked = enableAdmob, onCheckedChange = { enableAdmob = it })
                    }

                    if (enableAdmob) {
                        OutlinedTextField(
                            value = admobAppId,
                            onValueChange = { admobAppId = it },
                            label = { Text("AdMob App ID (ca-app-pub-...)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Real APK Generation Animation
            if (isBuilding) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            progress = { buildProgress },
                            color = CodeForgeCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(buildStage, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        Text("Target SDK 34 • Size ~18.4 MB", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            Button(
                onClick = {
                    if (balance < publishCharge) {
                        onNavigateDeposit()
                        return@Button
                    }
                    isBuilding = true
                    buildProgress = 0.2f
                    buildStage = "1/4 Packaging Web Assets & HTML..."

                    // Simulated rapid build steps for visual feedback
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        buildProgress = 0.5f
                        buildStage = "2/4 Generating AndroidManifest & Icons..."
                    }, 800)

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        buildProgress = 0.8f
                        buildStage = "3/4 Compiling Dex & Signing v2 APK..."
                    }, 1600)

                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        buildProgress = 1.0f
                        buildStage = "4/4 APK Ready!"
                        isBuilding = false
                        onPublishSuccess(appNameInput, pkgInput, versionInput)
                    }, 2400)
                },
                enabled = !isBuilding,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_publish_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CodeForgePurple)
            ) {
                Icon(Icons.Default.BuildCircle, contentDescription = "Build")
                Spacer(modifier = Modifier.width(8.dp))
                Text("BUILD APK NOW (PAY ₹$publishCharge)", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}
