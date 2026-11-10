package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.*
import com.example.ui.theme.CodeForgeTheme
import com.example.utils.DefaultTemplates
import com.example.viewmodel.CodeForgeViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: CodeForgeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsState()

            CodeForgeTheme(amoled = state.amoledMode) {
                when (val scr = state.currentScreen) {
                    is Screen.Splash -> {
                        SplashScreen(
                            buttons = state.splashButtons,
                            onEnterApp = { viewModel.navigateTo(Screen.Dashboard) },
                            onAdminLogin = { viewModel.navigateTo(Screen.SuperAdmin) }
                        )
                    }

                    is Screen.Auth -> {
                        SplashScreen(
                            buttons = state.splashButtons,
                            onEnterApp = { viewModel.navigateTo(Screen.Dashboard) },
                            onAdminLogin = { viewModel.navigateTo(Screen.SuperAdmin) }
                        )
                    }

                    is Screen.Dashboard -> {
                        DashboardScreen(
                            user = state.userProfile,
                            projects = state.projects,
                            deletedProjects = state.deletedProjects,
                            amoled = state.amoledMode,
                            onToggleAmoled = { viewModel.toggleAmoled() },
                            onCreateNewProject = { viewModel.navigateTo(Screen.CreateProject) },
                            onOpenProject = { pId -> viewModel.navigateTo(Screen.Editor(pId)) },
                            onOpenDeposit = { viewModel.navigateTo(Screen.Deposit()) },
                            onOpenPublish = { pId -> viewModel.navigateTo(Screen.Publish(pId)) },
                            onOpenCodeMixer = { pId -> viewModel.navigateTo(Screen.CodeMixer(pId)) },
                            onDuplicateProject = { pId -> viewModel.duplicateProject(pId) },
                            onDeleteProject = { pId -> viewModel.deleteProject(pId) },
                            onRestoreProject = { pId -> viewModel.restoreProject(pId) },
                            onOpenAdmin = { viewModel.navigateTo(Screen.SuperAdmin) },
                            onOpenNotifications = { viewModel.navigateTo(Screen.Notifications) },
                            onOpenHistory = { viewModel.navigateTo(Screen.History) }
                        )
                    }

                    is Screen.CreateProject -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        CreateProjectScreen(
                            templates = DefaultTemplates.getTemplates(),
                            onCreate = { name, pkg, tpl ->
                                viewModel.createProject(name, pkg, tpl)
                            },
                            onBack = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }

                    is Screen.Editor -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        val project = state.projects.find { it.id == scr.projectId }
                        if (project != null) {
                            EditorScreen(
                                project = project,
                                onSaveFile = { fn, content ->
                                    viewModel.updateProjectFile(project.id, fn, content)
                                },
                                onAddFile = { fn ->
                                    viewModel.addProjectFile(project.id, fn)
                                },
                                onPublish = {
                                    viewModel.navigateTo(Screen.Publish(project.id))
                                },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        } else {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    }

                    is Screen.CodeMixer -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        val project = state.projects.find { it.id == scr.projectId }
                        if (project != null) {
                            CodeMixerScreen(
                                project = project,
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        } else {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    }

                    is Screen.Deposit -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        DepositScreen(
                            currentOrder = state.currentOrder,
                            minDeposit = state.minDeposit,
                            upiId = state.upiId,
                            storeName = state.storeName,
                            isVerifying = state.isVerifyingPayment,
                            errorMessage = state.paymentErrorMessage,
                            onStartOrder = { amt -> viewModel.startDepositOrder(amt) },
                            onVerifyPayment = { orderId, utr, onDone ->
                                viewModel.verifyPayment(orderId, utr, onDone)
                            },
                            onBack = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }

                    is Screen.Publish -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        val project = state.projects.find { it.id == scr.projectId }
                        if (project != null) {
                            PublishScreen(
                                project = project,
                                balance = state.userProfile.balance,
                                publishCharge = state.publishCharge,
                                onPublishSuccess = { appName, pkg, version ->
                                    viewModel.publishApp(project.id, appName, pkg, version) {
                                        // Handled inside ViewModel
                                    }
                                },
                                onNavigateDeposit = {
                                    viewModel.navigateTo(Screen.Deposit(returnToPublish = true))
                                },
                                onBack = { viewModel.navigateTo(Screen.Dashboard) }
                            )
                        } else {
                            viewModel.navigateTo(Screen.Dashboard)
                        }
                    }

                    is Screen.FinalApk -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        FinalApkScreen(
                            appName = scr.appName,
                            packageName = scr.packageName,
                            onBackToDashboard = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }

                    is Screen.SuperAdmin -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        SuperAdminScreen(
                            upiId = state.upiId,
                            storeName = state.storeName,
                            minDeposit = state.minDeposit,
                            publishCharge = state.publishCharge,
                            backendUrl = state.backendUrl,
                            splashButtons = state.splashButtons,
                            transactions = state.transactions,
                            onSaveSettings = { upi, store, minD, chg, backend ->
                                viewModel.adminUpdateSettings(upi, store, minD, chg, backend)
                            },
                            onAddBalance = { amt -> viewModel.adminAddBalance(amt) },
                            onAddButton = { title, url -> viewModel.adminAddDynamicButton(title, url) },
                            onRemoveButton = { id -> viewModel.adminRemoveDynamicButton(id) },
                            onBack = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }

                    is Screen.Notifications -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        NotificationsScreen(
                            notifications = state.notifications,
                            onBack = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }

                    is Screen.History -> {
                        BackHandler { viewModel.navigateTo(Screen.Dashboard) }
                        HistoryScreen(
                            transactions = state.transactions,
                            onBack = { viewModel.navigateTo(Screen.Dashboard) }
                        )
                    }
                }
            }
        }
    }
}
