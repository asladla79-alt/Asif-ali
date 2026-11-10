package com.example.model

data class Project(
    val id: String,
    val name: String,
    val packageName: String = "com.editing.app",
    val version: String = "1.0",
    val template: String = "Blank",
    val status: String = "Draft", // Draft, Published, Building, Deleted
    val lastModified: Long = System.currentTimeMillis(),
    val files: Map<String, String> = emptyMap(),
    val iconName: String = "ic_app_default"
)

data class Transaction(
    val id: String,
    val amount: Int,
    val type: String, // "deposit", "publish_fee", "admin_bonus"
    val utr: String,
    val orderId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS"
)

data class PaymentOrder(
    val orderId: String,
    val amount: Int,
    val upiId: String = "ali00007@fam",
    val status: String = "pending", // pending, success, failed, review, expired
    val utr: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (15 * 60 * 1000)
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "info" // "build", "payment", "announcement"
)

data class UserProfile(
    val uid: String = "usr_dev_07",
    val email: String = "asladla79@gmail.com",
    val name: String = "Ali Developer",
    val balance: Int = 149,
    val isSuperAdmin: Boolean = true
)

data class SplashLinkButton(
    val id: String,
    val title: String,
    val url: String,
    val icon: String = "link"
)
