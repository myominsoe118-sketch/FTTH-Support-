package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.SupportChannel
import com.example.data.model.SupportTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.data.remote.BotTestResult
import com.example.data.remote.ChannelApiService
import com.example.data.repository.ChannelApiConfig
import com.example.data.repository.FtthSupportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

enum class NavigationTab(val labelMm: String, val labelEn: String) {
    INBOX("အဝင်မက်ဆေ့ခ်ျ", "Omni Inbox"),
    DAILY_TASKS("နေ့စဉ်လုပ်ငန်း", "Daily Tasks"),
    AGING_TRACKER("ရက်ကြာမှုစစ်ဆေး", "Aging Tracker"),
    NEW_CUSTOMERS("ဖောက်သည်အသစ်", "New Customers"),
    APPOINTMENTS("ရက်ချိန်းများ", "Appointments")
}

enum class AgingFilterOption(val labelMm: String, val labelEn: String) {
    ALL("အားလုံး", "All"),
    OVERDUE("ရက်ကြာနေပြီ (၂ ရက်ကျော်)", "Overdue (>48h)"),
    WARNING("ဆောင်ရွက်ဆဲ (၁-၂ ရက်)", "Pending (24-48h)"),
    FRESH("ယနေ့အသစ် (<၂၄ နာရီ)", "Fresh (<24h)")
}

data class FilterCriteria(
    val tab: NavigationTab = NavigationTab.INBOX,
    val channel: SupportChannel = SupportChannel.ALL,
    val priority: TicketPriority? = null,
    val aging: AgingFilterOption = AgingFilterOption.ALL,
    val query: String = ""
)

class SupportViewModel(
    private val repository: FtthSupportRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavigationTab.INBOX)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _selectedChannel = MutableStateFlow(SupportChannel.ALL)
    val selectedChannel: StateFlow<SupportChannel> = _selectedChannel.asStateFlow()

    private val _selectedPriority = MutableStateFlow<TicketPriority?>(null)
    val selectedPriority: StateFlow<TicketPriority?> = _selectedPriority.asStateFlow()

    private val _selectedAgingFilter = MutableStateFlow(AgingFilterOption.ALL)
    val selectedAgingFilter: StateFlow<AgingFilterOption> = _selectedAgingFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTicketId = MutableStateFlow<Long?>(null)
    val selectedTicketId: StateFlow<Long?> = _selectedTicketId.asStateFlow()

    val allTickets: StateFlow<List<SupportTicket>> = repository.allTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTicket: StateFlow<SupportTicket?> = _selectedTicketId
        .flatMapLatest { id ->
            if (id == null) flowOf(null) else repository.getTicketById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatMessages: StateFlow<List<ChatMessage>> = _selectedTicketId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getMessagesForTicket(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val filterCriteria: StateFlow<FilterCriteria> = combine(
        _currentTab,
        _selectedChannel,
        _selectedPriority,
        _selectedAgingFilter,
        _searchQuery
    ) { tab, channel, priority, aging, query ->
        FilterCriteria(tab, channel, priority, aging, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FilterCriteria())

    // Filtered Tickets according to active tab, channel, priority, aging, and search
    val filteredTickets: StateFlow<List<SupportTicket>> = combine(
        allTickets,
        filterCriteria
    ) { tickets, filter ->
        val now = System.currentTimeMillis()
        tickets.filter { ticket ->
            // Tab condition
            val matchTab = when (filter.tab) {
                NavigationTab.INBOX -> true
                NavigationTab.DAILY_TASKS -> ticket.status != TicketStatus.CLOSED
                NavigationTab.AGING_TRACKER -> ticket.status != TicketStatus.RESOLVED && ticket.status != TicketStatus.CLOSED
                NavigationTab.NEW_CUSTOMERS -> ticket.ticketType == TicketType.NEW_CUSTOMER
                NavigationTab.APPOINTMENTS -> ticket.appointmentDate != null || ticket.status == TicketStatus.SCHEDULED
            }

            // Channel condition
            val matchChannel = filter.channel == SupportChannel.ALL || ticket.channel == filter.channel

            // Priority condition
            val matchPriority = filter.priority == null || ticket.priority == filter.priority

            // Aging condition
            val matchAging = when (filter.aging) {
                AgingFilterOption.ALL -> true
                AgingFilterOption.OVERDUE -> ticket.isOverdue(now)
                AgingFilterOption.WARNING -> ticket.isWarning(now)
                AgingFilterOption.FRESH -> !ticket.isOverdue(now) && !ticket.isWarning(now)
            }

            // Search query condition
            val matchQuery = filter.query.isBlank() ||
                    ticket.customerName.contains(filter.query, ignoreCase = true) ||
                    ticket.customerPhone.contains(filter.query) ||
                    ticket.ticketNo.contains(filter.query, ignoreCase = true) ||
                    ticket.township.contains(filter.query, ignoreCase = true) ||
                    ticket.latestMessage.contains(filter.query, ignoreCase = true)

            matchTab && matchChannel && matchPriority && matchAging && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channelConfig: StateFlow<ChannelApiConfig>? = repository.settingsManager?.config

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }

        // Background worker for Telegram Real Live Sync (Long Polling)
        viewModelScope.launch {
            while (true) {
                delay(6000)
                if (repository.settingsManager?.config?.value?.isTelegramPollingActive == true) {
                    try {
                        val count = repository.pollTelegramUpdatesOnce()
                        if (count > 0) {
                            Log.d("SupportViewModel", "Fetched $count new messages from Telegram!")
                        }
                    } catch (e: Exception) {
                        Log.e("SupportViewModel", "Telegram polling error", e)
                    }
                }
            }
        }
    }

    fun saveTelegramToken(token: String, onFinished: (BotTestResult) -> Unit) {
        viewModelScope.launch {
            val result = ChannelApiService.testTelegramBot(token)
            if (result.isSuccess) {
                repository.settingsManager?.saveTelegramConfig(token, result.botName, pollingActive = true)
            }
            onFinished(result)
        }
    }

    fun setTelegramPolling(active: Boolean) {
        repository.settingsManager?.setTelegramPollingActive(active)
    }

    fun saveViberToken(token: String, senderName: String) {
        repository.settingsManager?.saveViberConfig(token, senderName)
    }

    fun saveMessengerToken(token: String) {
        repository.settingsManager?.saveMessengerConfig(token)
    }

    fun triggerManualTelegramSync(onComplete: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val newCount = repository.pollTelegramUpdatesOnce()
                onComplete(newCount)
            } catch (e: Exception) {
                onComplete(0)
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectChannel(channel: SupportChannel) {
        _selectedChannel.value = channel
    }

    fun selectPriority(priority: TicketPriority?) {
        _selectedPriority.value = priority
    }

    fun selectAgingFilter(option: AgingFilterOption) {
        _selectedAgingFilter.value = option
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openTicket(ticketId: Long) {
        _selectedTicketId.value = ticketId
        viewModelScope.launch {
            repository.markAsRead(ticketId)
        }
    }

    fun closeTicketDetail() {
        _selectedTicketId.value = null
    }

    fun sendReply(ticketId: Long, channel: SupportChannel, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendReply(ticketId, channel, text.trim())
        }
    }

    fun togglePriority(ticket: SupportTicket) {
        viewModelScope.launch {
            val nextPriority = if (ticket.priority == TicketPriority.URGENT) {
                TicketPriority.NORMAL
            } else {
                TicketPriority.URGENT
            }
            repository.updatePriority(ticket.id, nextPriority)
        }
    }

    fun updateTicketStatus(ticketId: Long, newStatus: TicketStatus) {
        viewModelScope.launch {
            repository.updateStatus(ticketId, newStatus)
        }
    }

    fun bookAppointment(
        ticketId: Long,
        dateMillis: Long,
        timeSlot: String,
        technician: String,
        autoNotifyCustomer: Boolean = true
    ) {
        viewModelScope.launch {
            repository.updateAppointment(ticketId, dateMillis, timeSlot, technician, autoNotifyCustomer)
        }
    }

    fun createTicket(
        customerName: String,
        phone: String,
        address: String,
        township: String,
        plan: String,
        channel: SupportChannel,
        priority: TicketPriority,
        ticketType: TicketType,
        initialMessage: String,
        opticalPower: Double?,
        splitterPort: String?,
        appointmentDate: Long?,
        appointmentSlot: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val randomNum = (1000..9999).random()
            val ticket = SupportTicket(
                ticketNo = "FTTH-$randomNum",
                customerName = customerName,
                customerPhone = phone,
                customerAddress = address,
                township = township,
                servicePlan = plan,
                channel = channel,
                priority = priority,
                ticketType = ticketType,
                status = if (appointmentDate != null) TicketStatus.SCHEDULED else TicketStatus.OPEN,
                createdAt = now,
                updatedAt = now,
                appointmentDate = appointmentDate,
                appointmentTimeSlot = appointmentSlot,
                opticalPowerDbm = opticalPower,
                ponSplitterPort = splitterPort,
                latestMessage = initialMessage,
                unreadCount = 0
            )
            val id = repository.insertTicket(ticket)
            if (initialMessage.isNotBlank()) {
                repository.sendReply(id, channel, initialMessage, agentName = "System / Customer Intake")
            }
        }
    }

    fun simulateIncomingInquiry(
        channel: SupportChannel,
        customerName: String,
        phone: String,
        township: String,
        plan: String,
        message: String,
        priority: TicketPriority,
        ticketType: TicketType
    ) {
        viewModelScope.launch {
            repository.simulateIncomingCustomerMessage(
                channel = channel,
                customerName = customerName,
                customerPhone = phone,
                township = township,
                servicePlan = plan,
                messageText = message,
                priority = priority,
                ticketType = ticketType
            )
        }
    }
}

class SupportViewModelFactory(
    private val repository: FtthSupportRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SupportViewModel::class.java)) {
            return SupportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
