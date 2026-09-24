package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SupportChannel(val displayName: String) {
    ALL("All Channels"),
    VIBER("Viber"),
    TELEGRAM("Telegram"),
    MESSENGER("Messenger"),
    PHONE("Direct / Call")
}

enum class TicketPriority(val labelMm: String, val labelEn: String) {
    URGENT("အရေးကြီး", "Urgent"),
    NORMAL("သာမန်", "Normal")
}

enum class TicketType(val labelMm: String, val labelEn: String) {
    NEW_CUSTOMER("ဖောက်သည်အသစ်", "New Customer Install"),
    LOS_RED_LIGHT("LOS မီးနီပြတ်တောက်မှု", "LOS Red Light (Fiber Cut)"),
    SLOW_SPEED("အင်တာနက်လိုင်းနှေးခြင်း", "Slow Speed / Latency"),
    ROUTER_CONFIG("ONU / Wi-Fi စစ်ဆေးခြင်း", "Router / ONU Config"),
    RELOCATION("လိုင်းနေရာရွှေ့ပြောင်းခြင်း", "Relocation Request"),
    BILLING("ဘေလ်နှင့် ငွေပေးချေမှု", "Billing & Payment")
}

enum class TicketStatus(val labelMm: String, val labelEn: String) {
    OPEN("အသစ်ဖွင့်", "Open"),
    IN_PROGRESS("ဆောင်ရွက်ဆဲ", "In Progress"),
    SCHEDULED("ရက်ချိန်းထား", "Scheduled"),
    RESOLVED("ဖြေရှင်းပြီး", "Resolved"),
    CLOSED("ပိတ်သိမ်း", "Closed")
}

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketNo: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val township: String,
    val servicePlan: String, // e.g. "FTTH 50 Mbps", "FTTH 100 Mbps"
    val channel: SupportChannel,
    val priority: TicketPriority,
    val ticketType: TicketType,
    val status: TicketStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val appointmentDate: Long? = null,
    val appointmentTimeSlot: String? = null, // e.g. "09:00 AM - 12:00 PM"
    val assignedTechnician: String = "တာဝန်မသတ်မှတ်ရသေး",
    val opticalPowerDbm: Double? = null, // e.g. -21.5 dBm
    val ponSplitterPort: String? = null, // e.g. "SP-HLN-02 / Port 3"
    val latestMessage: String = "",
    val unreadCount: Int = 0
) {
    /**
     * Calculate age in hours and days
     */
    fun ageHours(now: Long = System.currentTimeMillis()): Long {
        val diff = (now - createdAt).coerceAtLeast(0)
        return diff / (1000 * 60 * 60)
    }

    fun ageDays(now: Long = System.currentTimeMillis()): Long {
        return ageHours(now) / 24
    }

    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean {
        // More than 48 hours without being resolved
        return status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED && ageHours(now) >= 48
    }

    fun isWarning(now: Long = System.currentTimeMillis()): Boolean {
        // Between 24 and 48 hours
        return status != TicketStatus.RESOLVED && status != TicketStatus.CLOSED && ageHours(now) in 24..47
    }

    fun ageFormatted(now: Long = System.currentTimeMillis()): String {
        val hours = ageHours(now)
        val days = hours / 24
        val remHours = hours % 24
        return when {
            days > 0 -> "${days}ရက် ${remHours}နာရီကြာ"
            hours > 0 -> "${hours}နာရီကြာ"
            else -> "ယခုလေးတင်"
        }
    }
}
