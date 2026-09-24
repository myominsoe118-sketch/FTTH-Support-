package com.example.data.repository

import com.example.data.local.ChatMessageDao
import com.example.data.local.TicketDao
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.SupportChannel
import com.example.data.model.SupportTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.data.remote.ChannelApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FtthSupportRepository(
    private val ticketDao: TicketDao,
    private val chatMessageDao: ChatMessageDao,
    val settingsManager: ChannelSettingsManager? = null
) {
    private var lastTelegramUpdateId: Long = 0
    val allTickets: Flow<List<SupportTicket>> = ticketDao.getAllTickets()

    fun getTicketById(id: Long): Flow<SupportTicket?> = ticketDao.getTicketById(id)

    fun getMessagesForTicket(ticketId: Long): Flow<List<ChatMessage>> =
        chatMessageDao.getMessagesForTicket(ticketId)

    suspend fun insertTicket(ticket: SupportTicket): Long = withContext(Dispatchers.IO) {
        ticketDao.insertTicket(ticket)
    }

    suspend fun updateTicket(ticket: SupportTicket) = withContext(Dispatchers.IO) {
        ticketDao.updateTicket(ticket)
    }

    suspend fun markAsRead(ticketId: Long) = withContext(Dispatchers.IO) {
        ticketDao.markAsRead(ticketId)
    }

    suspend fun updateStatus(ticketId: Long, newStatus: TicketStatus) = withContext(Dispatchers.IO) {
        ticketDao.updateStatus(ticketId, newStatus.name, System.currentTimeMillis())
    }

    suspend fun updatePriority(ticketId: Long, priority: TicketPriority) = withContext(Dispatchers.IO) {
        ticketDao.updatePriority(ticketId, priority.name, System.currentTimeMillis())
    }

    suspend fun updateAppointment(
        ticketId: Long,
        date: Long?,
        slot: String?,
        tech: String,
        autoNotifyCustomer: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        ticketDao.updateAppointment(ticketId, date, slot, tech, now)

        if (autoNotifyCustomer && slot != null) {
            val ticket = ticketDao.getTicketByIdSync(ticketId)
            if (ticket != null) {
                val isComplain = ticket.ticketType != TicketType.NEW_CUSTOMER
                val purpose = if (isComplain) "Complain ချို့ယွင်းချက် ပြင်ဆင်ရေး (${ticket.ticketType.labelMm})" else "FTTH Fiber အသစ် တပ်ဆင်ရေး"
                val recordNote = "📅 [${ticket.channel.displayName} On-site Record]:\n" +
                        "• အမျိုးအစား: $purpose\n" +
                        "• ရက်ချိန်းအချိန်: $slot\n" +
                        "• တာဝန်ကျ နည်းပညာရှင်: $tech\n" +
                        "• ဖောက်သည်: ${ticket.customerName} (${ticket.customerPhone})\n" +
                        "• လိပ်စာ: ${ticket.customerAddress}, ${ticket.township}\n" +
                        "✅ Channel (${ticket.channel.displayName}) သို့ အကြောင်းကြားလွှာ ပေးပို့မှတ်တမ်းတင်ပြီးပါပြီ။"

                chatMessageDao.insertMessage(
                    ChatMessage(
                        ticketId = ticketId,
                        sender = MessageSender.AGENT,
                        senderName = "${ticket.channel.displayName} Bot / Desk",
                        channel = ticket.channel,
                        text = recordNote,
                        timestamp = now,
                        isSentSuccess = true
                    )
                )

                ticketDao.updateTicket(
                    ticket.copy(
                        latestMessage = "[ရက်ချိန်း]: $slot • $tech",
                        updatedAt = now,
                        status = TicketStatus.SCHEDULED
                    )
                )
            }
        }
    }

    suspend fun sendReply(
        ticketId: Long,
        channel: SupportChannel,
        replyText: String,
        agentName: String = "Support Desk"
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val message = ChatMessage(
            ticketId = ticketId,
            sender = MessageSender.AGENT,
            senderName = agentName,
            channel = channel,
            text = replyText,
            timestamp = now,
            isSentSuccess = true
        )
        chatMessageDao.insertMessage(message)

        val ticket = ticketDao.getTicketByIdSync(ticketId)
        if (ticket != null) {
            val updated = ticket.copy(
                latestMessage = "Desk: $replyText",
                updatedAt = now,
                status = if (ticket.status == TicketStatus.OPEN) TicketStatus.IN_PROGRESS else ticket.status
            )
            ticketDao.updateTicket(updated)

            // Direct outbound dispatch to real apps if configured
            val cfg = settingsManager?.config?.value
            if (cfg != null) {
                try {
                    when (channel) {
                        SupportChannel.TELEGRAM -> {
                            if (cfg.telegramBotToken.isNotBlank()) {
                                // customerPhone stores chat ID for Telegram tickets
                                val chatId = ticket.customerPhone.trim()
                                if (chatId.matches(Regex("^-?\\d+$"))) {
                                    ChannelApiService.sendTelegramMessage(
                                        botToken = cfg.telegramBotToken,
                                        chatId = chatId,
                                        text = replyText
                                    )
                                }
                            }
                        }
                        SupportChannel.VIBER -> {
                            if (cfg.viberAuthToken.isNotBlank() && ticket.customerPhone.isNotBlank()) {
                                ChannelApiService.sendViberMessage(
                                    viberAuthToken = cfg.viberAuthToken,
                                    receiverViberId = ticket.customerPhone.trim(),
                                    senderName = cfg.viberSenderName,
                                    text = replyText
                                )
                            }
                        }
                        SupportChannel.MESSENGER -> {
                            if (cfg.messengerPageAccessToken.isNotBlank() && ticket.customerPhone.isNotBlank()) {
                                ChannelApiService.sendMessengerMessage(
                                    pageAccessToken = cfg.messengerPageAccessToken,
                                    recipientPsid = ticket.customerPhone.trim(),
                                    text = replyText
                                )
                            }
                        }
                        else -> {}
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FtthSupportRepository", "Failed to dispatch message to $channel", e)
                }
            }
        }
    }

    /**
     * Poll real Telegram updates using Bot API getUpdates
     */
    suspend fun pollTelegramUpdatesOnce(): Int = withContext(Dispatchers.IO) {
        val cfg = settingsManager?.config?.value ?: return@withContext 0
        if (cfg.telegramBotToken.isBlank()) return@withContext 0

        val updates = ChannelApiService.getTelegramUpdates(
            botToken = cfg.telegramBotToken,
            offset = if (lastTelegramUpdateId > 0) lastTelegramUpdateId + 1 else null
        )

        var newCount = 0
        for (up in updates) {
            if (up.updateId >= lastTelegramUpdateId) {
                lastTelegramUpdateId = up.updateId
            }
            val chatIdStr = up.chatId.toString()
            val existingTicket = ticketDao.findOpenTicketByPhone(chatIdStr)

            if (existingTicket != null) {
                chatMessageDao.insertMessage(
                    ChatMessage(
                        ticketId = existingTicket.id,
                        sender = MessageSender.CUSTOMER,
                        senderName = up.senderName.ifBlank { "Telegram Customer" },
                        channel = SupportChannel.TELEGRAM,
                        text = up.text,
                        timestamp = up.date
                    )
                )
                ticketDao.updateTicket(
                    existingTicket.copy(
                        latestMessage = up.text,
                        updatedAt = up.date,
                        unreadCount = existingTicket.unreadCount + 1
                    )
                )
                newCount++
            } else {
                val isComplain = up.text.contains("မရ", ignoreCase = true) ||
                        up.text.contains("ကျ", ignoreCase = true) ||
                        up.text.contains("slow", ignoreCase = true) ||
                        up.text.contains("los", ignoreCase = true) ||
                        up.text.contains("မီးနီ", ignoreCase = true) ||
                        up.text.contains("error", ignoreCase = true) ||
                        up.text.contains("မတက်", ignoreCase = true)

                val ticketType = if (isComplain) TicketType.LOS_RED_LIGHT else TicketType.NEW_CUSTOMER
                val priority = if (isComplain) TicketPriority.URGENT else TicketPriority.NORMAL
                val randomNum = (1000..9999).random()

                val newTicket = SupportTicket(
                    ticketNo = "TG-$randomNum",
                    customerName = up.senderName.ifBlank { up.username ?: "Telegram Customer" },
                    customerPhone = chatIdStr,
                    customerAddress = "Telegram Direct (@${up.username ?: chatIdStr})",
                    township = "လှိုင် (Hlaing)",
                    servicePlan = "FTTH 50 Mbps",
                    channel = SupportChannel.TELEGRAM,
                    priority = priority,
                    ticketType = ticketType,
                    status = TicketStatus.OPEN,
                    createdAt = up.date,
                    updatedAt = up.date,
                    opticalPowerDbm = -21.5,
                    ponSplitterPort = "FAT-TG-01",
                    latestMessage = up.text,
                    unreadCount = 1
                )
                val id = ticketDao.insertTicket(newTicket)
                chatMessageDao.insertMessage(
                    ChatMessage(
                        ticketId = id,
                        sender = MessageSender.CUSTOMER,
                        senderName = up.senderName.ifBlank { "Telegram Customer" },
                        channel = SupportChannel.TELEGRAM,
                        text = up.text,
                        timestamp = up.date
                    )
                )
                newCount++
            }
        }
        return@withContext newCount
    }

    suspend fun simulateIncomingCustomerMessage(
        channel: SupportChannel,
        customerName: String,
        customerPhone: String,
        township: String,
        servicePlan: String,
        messageText: String,
        priority: TicketPriority,
        ticketType: TicketType
    ) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val randomNum = (1000..9999).random()
        val ticketNo = "FTTH-$randomNum"

        val newTicket = SupportTicket(
            ticketNo = ticketNo,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = "အမှတ်(${(10..99).random()})၊ လမ်းသစ်၊ $township",
            township = township,
            servicePlan = servicePlan,
            channel = channel,
            priority = priority,
            ticketType = ticketType,
            status = TicketStatus.OPEN,
            createdAt = now,
            updatedAt = now,
            opticalPowerDbm = -22.4,
            ponSplitterPort = "FAT-${township.take(3).uppercase()}-01 / P${(1..8).random()}",
            latestMessage = messageText,
            unreadCount = 1
        )
        val insertedId = ticketDao.insertTicket(newTicket)

        chatMessageDao.insertMessage(
            ChatMessage(
                ticketId = insertedId,
                sender = MessageSender.CUSTOMER,
                senderName = customerName,
                channel = channel,
                text = messageText,
                timestamp = now
            )
        )
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        if (ticketDao.getTicketCount() > 0) return@withContext

        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60L * 60L * 1000L

        // 1. OVERDUE Ticket (> 3 days / 80 hours ago) - Viber
        val ticket1 = SupportTicket(
            ticketNo = "FTTH-1082",
            customerName = "ဦးမင်းသူ (U Min Thu)",
            customerPhone = "09450123456",
            customerAddress = "အမှတ်(၂၄)၊ အင်းစိန်လမ်းမကြီး၊ လှိုင်မြို့နယ်",
            township = "လှိုင် (Hlaing)",
            servicePlan = "FTTH 100 Mbps Pro",
            channel = SupportChannel.VIBER,
            priority = TicketPriority.URGENT,
            ticketType = TicketType.LOS_RED_LIGHT,
            status = TicketStatus.OPEN,
            createdAt = now - (dayMillis * 3 + 1000 * 60 * 60 * 8), // 3 days 8 hours ago
            updatedAt = now - (dayMillis * 3),
            opticalPowerDbm = -35.2, // Optical loss / break!
            ponSplitterPort = "SP-HLN-03 / Port 5",
            latestMessage = "အင်တာနက်မရတာ ၃ ရက်ရှိပါပြီဗျာ LOS မီးနီလင်းနေတယ် ဘယ်တော့လာပြင်ပေးမလဲ",
            unreadCount = 3,
            assignedTechnician = "ကိုကျော်ဇင် (Senior Tech)"
        )

        // 2. Scheduled Appointment Today - Messenger
        val ticket2 = SupportTicket(
            ticketNo = "FTTH-1145",
            customerName = "ဒေါ်နွယ်နွယ်ဝင်း (Daw Nwe Nwe Win)",
            customerPhone = "09798765432",
            customerAddress = "တိုက်(A-4)၊ စံရိပ်ငြိမ်လမ်း၊ ကမာရွတ်",
            township = "ကမာရွတ် (Kamayut)",
            servicePlan = "FTTH 50 Mbps Home",
            channel = SupportChannel.MESSENGER,
            priority = TicketPriority.NORMAL,
            ticketType = TicketType.NEW_CUSTOMER,
            status = TicketStatus.SCHEDULED,
            createdAt = now - (dayMillis * 1 + 1000 * 60 * 60 * 4),
            updatedAt = now - (1000 * 60 * 60 * 2),
            appointmentDate = now, // Today
            appointmentTimeSlot = "13:00 - 16:00 (မွန်းလွဲ)",
            assignedTechnician = "ကိုအောင်မြင့် (FTTH Splicer)",
            opticalPowerDbm = null,
            ponSplitterPort = "SP-KMY-01 / Port 2",
            latestMessage = "ဒီနေ့ နေ့လယ် ၁ နာရီကနေ ၄ နာရီအတွင်း လာဆင်ပေးဖို့ ရက်ချိန်းယူထားပါတယ်ရှင်",
            unreadCount = 0
        )

        // 3. Warning Ticket (28 hours ago) - Telegram
        val ticket3 = SupportTicket(
            ticketNo = "FTTH-1204",
            customerName = "ကိုအောင်ဇော် (Ko Aung Zaw)",
            customerPhone = "09254332211",
            customerAddress = "အမှတ်(၅၁)၊ ဗဟိုလမ်း၊ စမ်းချောင်း",
            township = "စမ်းချောင်း (Sanchaung)",
            servicePlan = "FTTH 200 Mbps Ultra",
            channel = SupportChannel.TELEGRAM,
            priority = TicketPriority.URGENT,
            ticketType = TicketType.SLOW_SPEED,
            status = TicketStatus.IN_PROGRESS,
            createdAt = now - (dayMillis * 1 + 1000 * 60 * 60 * 4), // 28 hours ago
            updatedAt = now - (1000 * 60 * 60 * 5),
            opticalPowerDbm = -27.8, // Low signal
            ponSplitterPort = "SP-SC-08 / Port 7",
            latestMessage = "Packet loss ၃၀% ဖြစ်နေတယ် latency မြင့်ပြီး Zoom meeting လုပ်မရပါ",
            unreadCount = 1,
            assignedTechnician = "ကိုတင်မောင် (NOC Support)"
        )

        // 4. Fresh Ticket (3 hours ago) - Viber
        val ticket4 = SupportTicket(
            ticketNo = "FTTH-1230",
            customerName = "မသီတာလှိုင် (Ma Thida Hlaing)",
            customerPhone = "09965412389",
            customerAddress = "အမှတ်(၈၈)၊ ရွှေတောင်ကြားလမ်း၊ ဗဟန်း",
            township = "ဗဟန်း (Bahan)",
            servicePlan = "FTTH 50 Mbps Home",
            channel = SupportChannel.VIBER,
            priority = TicketPriority.NORMAL,
            ticketType = TicketType.ROUTER_CONFIG,
            status = TicketStatus.OPEN,
            createdAt = now - (1000 * 60 * 60 * 3), // 3 hours ago
            updatedAt = now - (1000 * 60 * 60 * 3),
            opticalPowerDbm = -20.1,
            ponSplitterPort = "SP-BHN-02 / Port 1",
            latestMessage = "Wi-Fi password ပြောင်းချင်လို့ router setting ဝင်မရဖြစ်နေပါတယ် ကူညီပေးပါဦး",
            unreadCount = 1
        )

        // 5. New Customer Request - Tomorrow appointment - Messenger
        val ticket5 = SupportTicket(
            ticketNo = "FTTH-1255",
            customerName = "ကိုစိုးလင်း (Ko Soe Lin)",
            customerPhone = "09443219876",
            customerAddress = "အမှတ်(၁၂)၊ ကမ္ဘာအေးဘုရားလမ်း၊ မရမ်းကုန်း",
            township = "မရမ်းကုန်း (Mayangone)",
            servicePlan = "FTTH 100 Mbps Pro",
            channel = SupportChannel.MESSENGER,
            priority = TicketPriority.NORMAL,
            ticketType = TicketType.NEW_CUSTOMER,
            status = TicketStatus.SCHEDULED,
            createdAt = now - (1000 * 60 * 60 * 18),
            updatedAt = now - (1000 * 60 * 60 * 6),
            appointmentDate = now + dayMillis, // Tomorrow
            appointmentTimeSlot = "09:00 - 12:00 (နံနက်)",
            assignedTechnician = "ကိုသက်ပိုင် (Install Team)",
            opticalPowerDbm = null,
            ponSplitterPort = "SP-MYG-05 / Port 8",
            latestMessage = "မနက်ဖြန် မနက်ပိုင်း အင်တာနက်လိုင်းအသစ် လာတပ်ဆင်ပေးပါရန်",
            unreadCount = 0
        )

        // 6. Direct / Phone Call - Urgent Fiber Cut
        val ticket6 = SupportTicket(
            ticketNo = "FTTH-1260",
            customerName = "ဦးကျော်စွာ (U Kyaw Swar)",
            customerPhone = "09778899001",
            customerAddress = "အမှတ်(၃)၊ ရာဇာဓိရာဇ်လမ်း၊ တောင်ဥက္ကလာ",
            township = "တောင်ဥက္ကလာ (S. Okkalapa)",
            servicePlan = "FTTH 50 Mbps Home",
            channel = SupportChannel.PHONE,
            priority = TicketPriority.URGENT,
            ticketType = TicketType.LOS_RED_LIGHT,
            status = TicketStatus.OPEN,
            createdAt = now - (1000 * 60 * 60 * 6),
            updatedAt = now - (1000 * 60 * 60 * 6),
            opticalPowerDbm = -38.0,
            ponSplitterPort = "SP-TOK-02 / Port 4",
            latestMessage = "ဖိုက်ဘာကြိုး ကားတိုက်ခံရပြီး ပြတ်သွားပါသည် အရေးကြီးအင်တာနက်လိုအပ်နေပါသည်",
            unreadCount = 1
        )

        val id1 = ticketDao.insertTicket(ticket1)
        val id2 = ticketDao.insertTicket(ticket2)
        val id3 = ticketDao.insertTicket(ticket3)
        val id4 = ticketDao.insertTicket(ticket4)
        val id5 = ticketDao.insertTicket(ticket5)
        val id6 = ticketDao.insertTicket(ticket6)

        // Add message history for ticket 1 (Viber thread)
        chatMessageDao.insertMessages(
            listOf(
                ChatMessage(
                    ticketId = id1,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ဦးမင်းသူ",
                    channel = SupportChannel.VIBER,
                    text = "မင်္ဂလာပါ အင်တာနက်လိုင်းမရတာ မနေ့ကတည်းကပါ ONU မှာ LOS မီးနီ မှိတ်တုတ်မှိတ်တုတ် ဖြစ်နေတယ်",
                    timestamp = ticket1.createdAt
                ),
                ChatMessage(
                    ticketId = id1,
                    sender = MessageSender.AGENT,
                    senderName = "FTTH Support",
                    channel = SupportChannel.VIBER,
                    text = "မင်္ဂလာပါခင်ဗျာ။ Router power ခဏပိတ်ပြီး restart ချပေးပါဦးခင်ဗျာ။ အကယ်၍ မရပါက optical fiber ကြိုး စစ်ဆေးပေးပါမည်။",
                    timestamp = ticket1.createdAt + 1000 * 60 * 15
                ),
                ChatMessage(
                    ticketId = id1,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ဦးမင်းသူ",
                    channel = SupportChannel.VIBER,
                    text = "Restart ချပြီးပြီ မရဘူးဗျ။ အင်တာနက်မရတာ ၃ ရက်ရှိပါပြီဗျာ LOS မီးနီလင်းနေတယ် ဘယ်တော့လာပြင်ပေးမလဲ",
                    timestamp = now - (1000 * 60 * 60 * 2)
                )
            )
        )

        // Add message history for ticket 3 (Telegram thread)
        chatMessageDao.insertMessages(
            listOf(
                ChatMessage(
                    ticketId = id3,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ကိုအောင်ဇော်",
                    channel = SupportChannel.TELEGRAM,
                    text = "Hello support, 200Mbps Ultra pack သုံးနေတာပါ။ လိုင်းအရမ်းနှေးပြီး ping အရမ်းတက်နေတယ်",
                    timestamp = ticket3.createdAt
                ),
                ChatMessage(
                    ticketId = id3,
                    sender = MessageSender.AGENT,
                    senderName = "NOC Support",
                    channel = SupportChannel.TELEGRAM,
                    text = "သတင်းပို့ပေးမှုအတွက် ကျေးဇူးတင်ပါသည်။ Optical DB စစ်ဆေးကြည့်ရာ Signal -27.8 dBm ဖြစ်နေ၍ Splitter port အား စစ်ဆေးပေးပါမည်။",
                    timestamp = ticket3.createdAt + 1000 * 60 * 30
                ),
                ChatMessage(
                    ticketId = id3,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ကိုအောင်ဇော်",
                    channel = SupportChannel.TELEGRAM,
                    text = "Packet loss ၃၀% ဖြစ်နေတယ် latency မြင့်ပြီး Zoom meeting လုပ်မရပါ မြန်မြန်စစ်ပေးပါဦး",
                    timestamp = ticket3.updatedAt
                )
            )
        )

        // Add message history for ticket 2 (Messenger thread)
        chatMessageDao.insertMessages(
            listOf(
                ChatMessage(
                    ticketId = id2,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ဒေါ်နွယ်နွယ်ဝင်း",
                    channel = SupportChannel.MESSENGER,
                    text = "မင်္ဂလာပါ FTTH 50Mbps လိုင်းအသစ် တပ်ဆင်ချင်လို့ပါ ကမာရွတ် စံရိပ်ငြိမ်လမ်းထဲမှာပါ",
                    timestamp = ticket2.createdAt
                ),
                ChatMessage(
                    ticketId = id2,
                    sender = MessageSender.AGENT,
                    senderName = "Sales Desk",
                    channel = SupportChannel.MESSENGER,
                    text = "မင်္ဂလာပါရှင့်။ အဆိုပါနေရာတွင် Splitter Port လွတ်ရှိပါသဖြင့် ဒီကနေ့ နေ့လယ် ၁ နာရီမှ ၄ နာရီအတွင်း အင်ဂျင်နီယာ လာရောက်တပ်ဆင်ပေးပါမည်ရှင်။",
                    timestamp = ticket2.createdAt + 1000 * 60 * 45
                ),
                ChatMessage(
                    ticketId = id2,
                    sender = MessageSender.CUSTOMER,
                    senderName = "ဒေါ်နွယ်နွယ်ဝင်း",
                    channel = SupportChannel.MESSENGER,
                    text = "ဒီနေ့ နေ့လယ် ၁ နာရီကနေ ၄ နာရီအတွင်း လာဆင်ပေးဖို့ ရက်ချိန်းယူထားပါတယ်ရှင် ကျေးဇူးတင်ပါတယ်",
                    timestamp = ticket2.updatedAt
                )
            )
        )
    }
}
