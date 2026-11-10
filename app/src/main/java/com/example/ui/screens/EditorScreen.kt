package com.example.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.Project
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: Project,
    onSaveFile: (String, String) -> Unit,
    onAddFile: (String) -> Unit,
    onPublish: () -> Unit,
    onBack: () -> Unit
) {
    val fileNames = project.files.keys.toList().ifEmpty { listOf("index.html") }
    var selectedFile by remember { mutableStateOf(fileNames.first()) }
    var codeContent by remember(selectedFile, project.files[selectedFile]) {
        mutableStateOf(project.files[selectedFile] ?: "")
    }

    var isLivePreview by remember { mutableStateOf(false) }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showFindReplaceDialog by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var saveStatusMsg by remember { mutableStateOf("All changes saved") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(selectedFile, fontSize = 11.sp, color = CodeForgeCyan, fontFamily = FontFamily.Monospace)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isLivePreview = !isLivePreview
                        },
                        modifier = Modifier.testTag("preview_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isLivePreview) Icons.Default.Code else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Preview",
                            tint = if (isLivePreview) CodeForgeCyan else CodeForgeGreen
                        )
                    }

                    IconButton(
                        onClick = { showFindReplaceDialog = true },
                        modifier = Modifier.testTag("find_replace_button")
                    ) {
                        Icon(Icons.Default.FindReplace, contentDescription = "Find & Replace", tint = TextSecondary)
                    }

                    IconButton(
                        onClick = {
                            onSaveFile(selectedFile, codeContent)
                            saveStatusMsg = "Saved at " + java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                        },
                        modifier = Modifier.testTag("save_code_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", tint = CodeForgeBlue)
                    }

                    Button(
                        onClick = onPublish,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CodeForgePurple),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("editor_publish_button")
                    ) {
                        Text("Publish", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F131D),
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = Color(0xFF0F131D)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // File tabs row + New File Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0D14))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                fileNames.forEach { fn ->
                    val isSelected = fn == selectedFile
                    Surface(
                        onClick = {
                            onSaveFile(selectedFile, codeContent)
                            selectedFile = fn
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF1E2638) else Color.Transparent,
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = PrimaryGradient) else null,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (fn.endsWith(".html")) Icons.Default.Language else if (fn.endsWith(".css")) Icons.Default.Palette else Icons.Default.Javascript,
                                contentDescription = fn,
                                tint = if (fn.endsWith(".html")) CodeForgeCyan else if (fn.endsWith(".css")) CodeForgePink else CodeForgeGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = fn,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add File", tint = CodeForgeCyan, modifier = Modifier.size(18.dp))
                }
            }

            // Editor / Live Preview toggle
            if (isLivePreview) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                ) {
                    val htmlToRender = project.files["index.html"] ?: codeContent
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewClient = WebViewClient()
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                loadDataWithBaseURL("https://localapp/", htmlToRender, "text/html", "UTF-8", null)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("https://localapp/", htmlToRender, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // Code Editor with line numbers
                val lines = codeContent.lines()
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F131D))
                        .padding(8.dp)
                ) {
                    // Line numbers column
                    val lineNumScroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .width(36.dp)
                            .verticalScroll(lineNumScroll)
                            .padding(top = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..maxOf(lines.size, 1)) {
                            Text(
                                text = "$i",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextMuted,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(DarkSurfaceBorder))
                    Spacer(modifier = Modifier.width(8.dp))

                    // Code text field
                    val codeScroll = rememberScrollState()
                    BasicTextField(
                        value = codeContent,
                        onValueChange = { codeContent = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(codeScroll)
                            .testTag("code_editor_input"),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CodeForgeCyan)
                    )
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Add New File") },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    placeholder = { Text("e.g. firebase.js or custom.css") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            onAddFile(newFileNameInput.trim())
                            selectedFile = newFileNameInput.trim()
                            newFileNameInput = ""
                            showNewFileDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Find & Replace Dialog
    if (showFindReplaceDialog) {
        AlertDialog(
            onDismissRequest = { showFindReplaceDialog = false },
            title = { Text("Find & Replace") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = findQuery,
                        onValueChange = { findQuery = it },
                        label = { Text("Find") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = replaceQuery,
                        onValueChange = { replaceQuery = it },
                        label = { Text("Replace With") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (findQuery.isNotEmpty()) {
                            codeContent = codeContent.replace(findQuery, replaceQuery)
                            onSaveFile(selectedFile, codeContent)
                            showFindReplaceDialog = false
                        }
                    }
                ) {
                    Text("Replace All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFindReplaceDialog = false }) { Text("Close") }
            }
        )
    }
}
