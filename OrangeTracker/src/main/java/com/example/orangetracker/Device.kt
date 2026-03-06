package com.example.orangetracker.model

import com.example.orangetracker.R

enum class DeviceStatus {
    ACTIVE, BLOCKED, LIMITED, OFFLINE
}

data class Device(
    val id: String,
    val name: String,
    val mac: String,
    val ip: String,
    val vendor: String? = null,
    var status: DeviceStatus = DeviceStatus.ACTIVE,
    var speedLimit: Int = 0, // 0 = unlimited, in Mbps
    var currentRxMbps: Double = 0.0,
    var currentTxMbps: Double = 0.0,
    val totalRxGb: Double = 0.0,
    val totalTxGb: Double = 0.0,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val isBlocked: Boolean
        get() = status == DeviceStatus.BLOCKED

    val isLimited: Boolean
        get() = status == DeviceStatus.LIMITED

    val isActive: Boolean
        get() = status == DeviceStatus.ACTIVE

    val displayName: String
        get() = if (name.isNotEmpty()) name else "Unknown Device"

    val statusText: String
        get() = when (status) {
            DeviceStatus.ACTIVE -> "Активен"
            DeviceStatus.BLOCKED -> "Заблокирован"
            DeviceStatus.LIMITED -> "Лимит $speedLimit Мбит/с"
            DeviceStatus.OFFLINE -> "Не в сети"
        }

    val statusColor: Int
        get() = when (status) {
            DeviceStatus.ACTIVE -> R.color.status_active
            DeviceStatus.BLOCKED -> R.color.status_blocked
            DeviceStatus.LIMITED -> R.color.status_limited
            DeviceStatus.OFFLINE -> R.color.status_offline
        }

    val statusBackground: Int
        get() = when (status) {
            DeviceStatus.ACTIVE -> R.drawable.bg_status_active
            DeviceStatus.BLOCKED -> R.drawable.bg_status_blocked
            DeviceStatus.LIMITED -> R.drawable.bg_status_limited
            DeviceStatus.OFFLINE -> R.drawable.bg_status_offline
        }
}