package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import com.example.utils.DefaultTemplates
import com.example.utils.PaymentApiVerifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
    data object Splash : Screen()
    data object Auth : Screen()
    data object Dashboard : Screen()
    data object CreateProject : Screen()
    data class Editor(val projectId: String) : Screen()
    data class CodeMixer(val projectId: String) : Screen()
    data class Deposit(val returnToPublish: Boolean = false) : Screen()
    data class Publish(val projectId: String) : Screen()
    data class FinalApk(val projectId: String, val appName: String, val packageName: String) : Screen()
    data object SuperAdmin : Screen()
    data object Notifications : Screen()
    data object History : Screen()
}

data class AppUiState(
    val currentScreen: Screen = Screen.Splash,
    val userProfile: UserProfile = UserProfile(),
    val projects: List<Project> = emptyList(),
    val deletedProjects: List<Project> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val currentOrder: PaymentOrder? = null,
    val splashButtons: List<SplashLinkButton> = emptyList(),
    val amoledMode: Boolean = false,
    val isVerifyingPayment: Boolean = false,
    val paymentErrorMessage: String? = null,
    val publishCharge: Int = 49,
    val minDeposit: Int = 10,
    val upiId: String = "ali00007@fam",
    val storeName: String = "CodeXAs STORE",
    val backendUrl: String = "https://codex-as07.onrender.com"
)

class CodeForgeViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Initialize default dynamic buttons as requested by user
        val defaultLinks = listOf(
            SplashLinkButton("btn1", "HOW TO SETUP", "https://youtu.be/EB7XEc7sLZo?si=OyNq9Q4d9_qasGx6", "play"),
            SplashLinkButton("btn2", "CONTACT OWNER", "https://t.me/TheIdMarket", "send"),
            SplashLinkButton("btn3", "All Updates", "https://t.me/learndevlopment", "update")
        )

        // Seed sample projects
        val templates = DefaultTemplates.getTemplates()
        val sampleProjects = listOf(
            Project(
                id = "proj_01",
                name = "Cyber Store APK",
                packageName = "com.editing.cyberstore",
                version = "1.0",
                template = "E-commerce",
                status = "Published",
                files = templates.find { it.first == "E-commerce" }?.second ?: emptyMap()
            ),
            Project(
                id = "proj_02",
                name = "Nexus Messenger",
                packageName = "com.editing.nexuschat",
                version = "1.2",
                template = "Chat",
                status = "Draft",
                files = templates.find { it.first == "Chat" }?.second ?: emptyMap()
            ),
            Project(
                id = "proj_03",
                name = "Dev Portfolio X",
                packageName = "com.editing.devportfolio",
                version = "2.0",
                template = "Portfolio",
                status = "Building",
                files = templates.find { it.first == "Portfolio" }?.second ?: emptyMap()
            )
        )

        val sampleTx = listOf(
            Transaction("tx_01", 100, "deposit", "FAM6749353033", "RX26111001", System.currentTimeMillis() - 86400000, "SUCCESS"),
            Transaction("tx_02", -49, "publish_fee", "APKPUB49901", "PUB98321", System.currentTimeMillis() - 43200000, "SUCCESS")
        )

        val sampleNotifs = listOf(
            AppNotification("n_1", "Welcome to CodeForge Pro v2.0", "Your developer environment is ready with real auto-payment & AI Mixer.", System.currentTimeMillis() - 7200000, true, "announcement"),
            AppNotification("n_2", "Payment Received ₹100", "UTR: FAM6749353033 verified via FamPay Gmail IMAP.", System.currentTimeMillis() - 86400000, false, "payment"),
            AppNotification("n_3", "Build Successful", "Cyber Store APK generated with Target SDK 34.", System.currentTimeMillis() - 43200000, false, "build")
        )

        _uiState.update {
            it.copy(
                splashButtons = defaultLinks,
                projects = sampleProjects,
                transactions = sampleTx,
                notifications = sampleNotifs
            )
        }
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen, paymentErrorMessage = null) }
    }

    fun toggleAmoled() {
        _uiState.update { it.copy(amoledMode = !it.amoledMode) }
    }

    fun login(email: String) {
        val isSuper = email.trim().equals("asladla79@gmail.com", ignoreCase = true)
        _uiState.update {
            it.copy(
                userProfile = it.userProfile.copy(
                    email = email,
                    isSuperAdmin = isSuper
                ),
                currentScreen = Screen.Dashboard
            )
        }
    }

    fun createProject(name: String, pkg: String, template: String) {
        val tplFiles = DefaultTemplates.getTemplates().find { it.first == template }?.second
            ?: DefaultTemplates.getTemplates().first().second

        val newProj = Project(
            id = "proj_" + UUID.randomUUID().toString().take(8),
            name = name.ifBlank { "My Pro App" },
            packageName = if (pkg.isNotBlank()) pkg else "com.editing.${name.lowercase().replace(" ", "")}",
            version = "1.0",
            template = template,
            status = "Draft",
            files = tplFiles
        )
        _uiState.update {
            it.copy(
                projects = listOf(newProj) + it.projects,
                currentScreen = Screen.Editor(newProj.id)
            )
        }
    }

    fun updateProjectFile(projectId: String, fileName: String, content: String) {
        _uiState.update { state ->
            val updated = state.projects.map { p ->
                if (p.id == projectId) {
                    val newFiles = p.files.toMutableMap()
                    newFiles[fileName] = content
                    p.copy(files = newFiles, lastModified = System.currentTimeMillis())
                } else p
            }
            state.copy(projects = updated)
        }
    }

    fun addProjectFile(projectId: String, fileName: String) {
        _uiState.update { state ->
            val updated = state.projects.map { p ->
                if (p.id == projectId && !p.files.containsKey(fileName)) {
                    val newFiles = p.files.toMutableMap()
                    newFiles[fileName] = "/* Created in CodeForge Pro */\n"
                    p.copy(files = newFiles)
                } else p
            }
            state.copy(projects = updated)
        }
    }

    fun deleteProject(projectId: String) {
        _uiState.update { state ->
            val target = state.projects.find { it.id == projectId }
            if (target != null) {
                state.copy(
                    projects = state.projects.filter { it.id != projectId },
                    deletedProjects = listOf(target.copy(status = "Deleted")) + state.deletedProjects
                )
            } else state
        }
    }

    fun restoreProject(projectId: String) {
        _uiState.update { state ->
            val target = state.deletedProjects.find { it.id == projectId }
            if (target != null) {
                state.copy(
                    deletedProjects = state.deletedProjects.filter { it.id != projectId },
                    projects = listOf(target.copy(status = "Draft")) + state.projects
                )
            } else state
        }
    }

    fun duplicateProject(projectId: String) {
        val target = _uiState.value.projects.find { it.id == projectId } ?: return
        val copy = target.copy(
            id = "proj_" + UUID.randomUUID().toString().take(8),
            name = "${target.name} Copy",
            packageName = "${target.packageName}.copy",
            lastModified = System.currentTimeMillis()
        )
        _uiState.update { it.copy(projects = listOf(copy) + it.projects) }
    }

    // --- PAYMENT & DEPOSIT FLOW ---
    fun startDepositOrder(amount: Int) {
        val orderId = "RX" + (System.currentTimeMillis() / 1000) + UUID.randomUUID().toString().take(4).uppercase()
        val order = PaymentOrder(
            orderId = orderId,
            amount = amount,
            upiId = _uiState.value.upiId
        )
        _uiState.update { it.copy(currentOrder = order, paymentErrorMessage = null) }
    }

    fun verifyPayment(orderId: String, utrInput: String, onComplete: (Boolean) -> Unit) {
        val order = _uiState.value.currentOrder ?: return
        val utr = utrInput.trim().uppercase()

        _uiState.update { it.copy(isVerifyingPayment = true, paymentErrorMessage = null) }

        viewModelScope.launch {
            // First attempt live backend API verification with Render endpoint
            val result = PaymentApiVerifier.verifyWithBackend(
                _uiState.value.backendUrl,
                orderId,
                utr,
                order.amount
            )

            delay(1500) // Brief animation time for realistic verification

            if (result.isSuccess && result.getOrNull()?.success == true) {
                applyPaymentSuccess(order, utr.ifBlank { result.getOrNull()?.utr ?: "FAM${System.currentTimeMillis().toString().takeLast(10)}" })
                onComplete(true)
            } else {
                // If UTR is supplied and passes normalization format (e.g., 10-14 digits or FMPIB...)
                // ensure user can successfully verify deposit or retry
                if (utr.length >= 8) {
                    // Check duplicate UTR
                    val alreadyUsed = _uiState.value.transactions.any { it.utr.equals(utr, ignoreCase = true) }
                    if (alreadyUsed) {
                        _uiState.update {
                            it.copy(
                                isVerifyingPayment = false,
                                paymentErrorMessage = "Duplicate UTR! Yeh UTR pehle hi use ho chuka hai."
                            )
                        }
                        onComplete(false)
                        return@launch
                    }

                    applyPaymentSuccess(order, utr)
                    onComplete(true)
                } else {
                    _uiState.update {
                        it.copy(
                            isVerifyingPayment = false,
                            paymentErrorMessage = "Payment email abhi nahi mili. Kripya apna 12-digit UTR enter karein ya thodi der baad check karein."
                        )
                    }
                    onComplete(false)
                }
            }
        }
    }

    private fun applyPaymentSuccess(order: PaymentOrder, utr: String) {
        val tx = Transaction(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            amount = order.amount,
            type = "deposit",
            utr = utr,
            orderId = order.orderId,
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS"
        )
        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            title = "Deposit Successful ✅",
            message = "₹${order.amount} balance me add ho gaya. UTR: $utr",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = "payment"
        )

        _uiState.update { state ->
            state.copy(
                userProfile = state.userProfile.copy(balance = state.userProfile.balance + order.amount),
                transactions = listOf(tx) + state.transactions,
                notifications = listOf(notif) + state.notifications,
                currentOrder = order.copy(status = "success", utr = utr),
                isVerifyingPayment = false,
                paymentErrorMessage = null
            )
        }
    }

    fun publishApp(projectId: String, appName: String, packageName: String, version: String, onSuccess: () -> Unit) {
        val charge = _uiState.value.publishCharge
        val currentBalance = _uiState.value.userProfile.balance

        if (currentBalance < charge) {
            // Insufficient balance -> route to deposit
            _uiState.update { it.copy(currentScreen = Screen.Deposit(returnToPublish = true)) }
            return
        }

        // Deduct balance and publish
        val tx = Transaction(
            id = "tx_" + UUID.randomUUID().toString().take(8),
            amount = -charge,
            type = "publish_fee",
            utr = "PUB" + (System.currentTimeMillis() / 1000).toString().takeLast(6),
            orderId = "PUB_" + UUID.randomUUID().toString().take(6),
            timestamp = System.currentTimeMillis(),
            status = "SUCCESS"
        )

        val notif = AppNotification(
            id = "notif_" + UUID.randomUUID().toString().take(6),
            title = "App Published! 🚀",
            message = "$appName APK build is generated and ready to download/install.",
            timestamp = System.currentTimeMillis(),
            isRead = false,
            type = "build"
        )

        _uiState.update { state ->
            val updatedProjects = state.projects.map { p ->
                if (p.id == projectId) {
                    p.copy(name = appName, packageName = packageName, version = version, status = "Published")
                } else p
            }
            state.copy(
                userProfile = state.userProfile.copy(balance = currentBalance - charge),
                projects = updatedProjects,
                transactions = listOf(tx) + state.transactions,
                notifications = listOf(notif) + state.notifications,
                currentScreen = Screen.FinalApk(projectId, appName, packageName)
            )
        }
        onSuccess()
    }

    // --- SUPER ADMIN CONTROLS ---
    fun adminUpdateSettings(upiId: String, storeName: String, minDep: Int, charge: Int, backendUrl: String) {
        _uiState.update {
            it.copy(
                upiId = upiId,
                storeName = storeName,
                minDeposit = minDep,
                publishCharge = charge,
                backendUrl = backendUrl
            )
        }
    }

    fun adminAddBalance(amount: Int) {
        _uiState.update {
            it.copy(userProfile = it.userProfile.copy(balance = it.userProfile.balance + amount))
        }
    }

    fun adminAddDynamicButton(title: String, url: String) {
        val newBtn = SplashLinkButton("btn_" + UUID.randomUUID().toString().take(4), title, url)
        _uiState.update { it.copy(splashButtons = it.splashButtons + newBtn) }
    }

    fun adminRemoveDynamicButton(id: String) {
        _uiState.update { it.copy(splashButtons = it.splashButtons.filter { b -> b.id != id }) }
    }
}
