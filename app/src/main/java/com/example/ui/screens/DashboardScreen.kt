package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Project
import com.example.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserProfile,
    projects: List<Project>,
    deletedProjects: List<Project>,
    amoled: Boolean,
    onToggleAmoled: () -> Unit,
    onCreateNewProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    onOpenDeposit: () -> Unit,
    onOpenPublish: (String) -> Unit,
    onOpenCodeMixer: (String) -> Unit,
    onDuplicateProject: (String) -> Unit,
    onDeleteProject: (String) -> Unit,
    onRestoreProject: (String) -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenHistory: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Published", "Draft", "Building", "Trash Bin")
    var searchQuery by remember { mutableStateOf("") }

    val displayedProjects = when (selectedTab) {
        0 -> projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
        1 -> projects.filter { it.status == "Published" && it.name.contains(searchQuery, ignoreCase = true) }
        2 -> projects.filter { it.status == "Draft" && it.name.contains(searchQuery, ignoreCase = true) }
        3 -> projects.filter { it.status == "Building" && it.name.contains(searchQuery, ignoreCase = true) }
        4 -> deletedProjects.filter { it.name.contains(searchQuery, ignoreCase = true) }
        else -> projects
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("<>", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("CODEFORGE PRO", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text("Studio Workspace", fontSize = 11.sp, color = CodeForgeCyan)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onToggleAmoled, modifier = Modifier.testTag("amoled_toggle_button")) {
                        Icon(
                            imageVector = if (amoled) Icons.Default.Brightness7 else Icons.Default.Brightness4,
                            contentDescription = "Toggle AMOLED",
                            tint = if (amoled) CodeForgeGold else TextSecondary
                        )
                    }
                    IconButton(onClick = onOpenNotifications, modifier = Modifier.testTag("notifications_button")) {
                        BadgedBox(badge = { Badge { Text("3") } }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextSecondary)
                        }
                    }
                    IconButton(onClick = onOpenAdmin, modifier = Modifier.testTag("admin_button")) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Super Admin", tint = CodeForgePurple)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (amoled) AmoledBg else DarkBg,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = if (amoled) AmoledBg else DarkBg
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Glassmorphism Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, PrimaryGradient, RoundedCornerShape(20.dp))
                        .testTag("balance_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.85f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardGlassGradient)
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet", tint = CodeForgeCyan)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CURRENT BALANCE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CodeForgeGreen.copy(alpha = 0.2f)
                                ) {
                                    Text("AUTO-PAY ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CodeForgeGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "₹${user.balance}",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )

                                Button(
                                    onClick = onOpenDeposit,
                                    modifier = Modifier.testTag("deposit_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CodeForgeBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DEPOSIT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Publish fee: ₹49 / app", fontSize = 11.sp, color = TextMuted)
                                TextButton(
                                    onClick = onOpenHistory,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("View History →", fontSize = 12.sp, color = CodeForgeCyan)
                                }
                            }
                        }
                    }
                }
            }

            // Big Create New Project Button
            item {
                Button(
                    onClick = onCreateNewProject,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("create_new_project_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(PrimaryGradient, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AddCircle, contentDescription = "New", tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "CREATE NEW PROJECT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Search Bar & Tabs
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects by name or package...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_projects_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceVariant,
                        unfocusedContainerColor = DarkSurfaceVariant,
                        focusedBorderColor = CodeForgeBlue,
                        unfocusedBorderColor = DarkSurfaceBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = CodeForgeBlue,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = if (index == 4) "$title (${deletedProjects.size})" else title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) CodeForgeCyan else TextSecondary
                                )
                            }
                        )
                    }
                }
            }

            // Project Items
            if (displayedProjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Empty", tint = TextMuted, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No projects found", color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text("Tap 'Create New Project' to start building", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(displayedProjects) { project ->
                    ProjectCardItem(
                        project = project,
                        isTrash = selectedTab == 4,
                        onOpen = { onOpenProject(project.id) },
                        onPublish = { onOpenPublish(project.id) },
                        onCodeMixer = { onOpenCodeMixer(project.id) },
                        onDuplicate = { onDuplicateProject(project.id) },
                        onDelete = { onDeleteProject(project.id) },
                        onRestore = { onRestoreProject(project.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: Project,
    isTrash: Boolean,
    onOpen: () -> Unit,
    onPublish: () -> Unit,
    onCodeMixer: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { if (!isTrash) onOpen() }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                when (project.template) {
                                    "E-commerce" -> CodeForgePink.copy(alpha = 0.2f)
                                    "Chat" -> CodeForgeBlue.copy(alpha = 0.2f)
                                    "Gaming" -> CodeForgePurple.copy(alpha = 0.2f)
                                    else -> CodeForgeCyan.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (project.template) {
                                "E-commerce" -> Icons.Default.ShoppingCart
                                "Chat" -> Icons.Default.Chat
                                "Gaming" -> Icons.Default.SportsEsports
                                else -> Icons.Default.Code
                            },
                            contentDescription = project.template,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = project.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = project.packageName,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (project.status) {
                        "Published" -> CodeForgeGreen.copy(alpha = 0.15f)
                        "Building" -> CodeForgeGold.copy(alpha = 0.15f)
                        "Deleted" -> Color.Red.copy(alpha = 0.15f)
                        else -> CodeForgePurple.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = project.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (project.status) {
                            "Published" -> CodeForgeGreen
                            "Building" -> CodeForgeGold
                            "Deleted" -> Color(0xFFFF5252)
                            else -> CodeForgePurple
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTrash) {
                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = CodeForgeGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Recover App", fontSize = 12.sp)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onOpen,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editor", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editor", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onCodeMixer,
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = PrimaryGradient),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Mixer", tint = CodeForgeCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Mixer", fontSize = 12.sp, color = CodeForgeCyan)
                        }

                        Button(
                            onClick = onPublish,
                            colors = ButtonDefaults.buttonColors(containerColor = CodeForgePurple),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = "Publish", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Publish ₹49", fontSize = 12.sp)
                        }
                    }

                    Row {
                        IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
