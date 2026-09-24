package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.ui.components.ChannelIntegrationDialog
import com.example.ui.components.CreateTicketDialog
import com.example.ui.components.ScheduleAppointmentDialog
import com.example.ui.components.SimulateInboundDialog
import com.example.ui.screens.AgingTrackerScreen
import com.example.ui.screens.AppointmentScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.DailyTasksScreen
import com.example.ui.screens.InboxScreen
import com.example.ui.screens.NewCustomerScreen
import com.example.ui.screens.WebDeskScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FtthSupportApp(
    viewModel: SupportViewModel
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val currentTab by viewModel.currentTab.collectAsState()
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val selectedAgingFilter by viewModel.selectedAgingFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val allTickets by viewModel.allTickets.collectAsState()
    val filteredTickets by viewModel.filteredTickets.collectAsState()
    val selectedTicket by viewModel.selectedTicket.collectAsState()
    val activeChatMessages by viewModel.activeChatMessages.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var createDialogInitialType by remember { mutableStateOf(TicketType.LOS_RED_LIGHT) }
    var showSimulateDialog by remember { mutableStateOf(false) }
    var showIntegrationDialog by remember { mutableStateOf(false) }
    var isWebDeskMode by remember { mutableStateOf(false) }
    var appointmentTicketToSchedule by remember { mutableStateOf<SupportTicket?>(null) }

    // Counts for Badges
    val overdueCount = allTickets.count { it.isOverdue() }
    val urgentCount = allTickets.count { it.priority == TicketPriority.URGENT && it.status != TicketStatus.CLOSED }
    val scheduledCount = allTickets.count { it.status == TicketStatus.SCHEDULED }
    val newCustomerCount = allTickets.count { it.ticketType == TicketType.NEW_CUSTOMER && it.status != TicketStatus.RESOLVED }

    if (selectedTicket != null) {
        ChatDetailScreen(
            ticket = selectedTicket,
            messages = activeChatMessages,
            onBack = { viewModel.closeTicketDetail() },
            onSendReply = { replyText ->
                viewModel.sendReply(selectedTicket!!.id, selectedTicket!!.channel, replyText)
            },
            onTogglePriority = {
                viewModel.togglePriority(selectedTicket!!)
            },
            onUpdateStatus = { newStatus ->
                viewModel.updateTicketStatus(selectedTicket!!.id, newStatus)
            },
            onScheduleAppointment = {
                appointmentTicketToSchedule = selectedTicket
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FTTH OmniSupport",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Viber • Telegram • Messenger • ISP Desk",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = { isWebDeskMode = !isWebDeskMode },
                            modifier = Modifier.testTag("toggle_web_desk"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = if (isWebDeskMode) Icons.Default.PhoneAndroid else Icons.Default.Language,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = if (isWebDeskMode) Color(0xFF00897B) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isWebDeskMode) "App" else "HTML Desk",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showIntegrationDialog = true },
                            modifier = Modifier.testTag("action_channel_integration")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = "Channels & Bots",
                                tint = Color(0xFF00897B)
                            )
                        }

                        IconButton(
                            onClick = { showSimulateDialog = true },
                            modifier = Modifier.testTag("action_simulate_msg")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = "Simulate",
                                tint = Color(0xFFF57C00)
                            )
                        }

                        IconButton(
                            onClick = {
                                createDialogInitialType = TicketType.LOS_RED_LIGHT
                                showCreateDialog = true
                            },
                            modifier = Modifier.testTag("action_add_ticket")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Ticket"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!isTablet && !isWebDeskMode) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // 1. Inbox
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.INBOX,
                            onClick = { viewModel.selectTab(NavigationTab.INBOX) },
                            icon = {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = "Inbox")
                            },
                            label = { Text(NavigationTab.INBOX.labelMm, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tab_inbox")
                        )

                        // 2. Daily Tasks
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.DAILY_TASKS,
                            onClick = { viewModel.selectTab(NavigationTab.DAILY_TASKS) },
                            icon = {
                                if (urgentCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$urgentCount") } }) {
                                        Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = "Tasks")
                                    }
                                } else {
                                    Icon(imageVector = Icons.Default.FormatListBulleted, contentDescription = "Tasks")
                                }
                            },
                            label = { Text(NavigationTab.DAILY_TASKS.labelMm, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tab_tasks")
                        )

                        // 3. Aging Tracker
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.AGING_TRACKER,
                            onClick = { viewModel.selectTab(NavigationTab.AGING_TRACKER) },
                            icon = {
                                if (overdueCount > 0) {
                                    BadgedBox(badge = {
                                        Badge(containerColor = Color(0xFFD32F2F)) {
                                            Text("$overdueCount")
                                        }
                                    }) {
                                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Aging")
                                    }
                                } else {
                                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = "Aging")
                                }
                            },
                            label = { Text(NavigationTab.AGING_TRACKER.labelMm, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tab_aging")
                        )

                        // 4. New Customer
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.NEW_CUSTOMERS,
                            onClick = { viewModel.selectTab(NavigationTab.NEW_CUSTOMERS) },
                            icon = {
                                if (newCustomerCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$newCustomerCount") } }) {
                                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "New Customer")
                                    }
                                } else {
                                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "New Customer")
                                }
                            },
                            label = { Text(NavigationTab.NEW_CUSTOMERS.labelMm, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tab_new_customers")
                        )

                        // 5. Appointments
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.APPOINTMENTS,
                            onClick = { viewModel.selectTab(NavigationTab.APPOINTMENTS) },
                            icon = {
                                if (scheduledCount > 0) {
                                    BadgedBox(badge = { Badge { Text("$scheduledCount") } }) {
                                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Appointments")
                                    }
                                } else {
                                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Appointments")
                                }
                            },
                            label = { Text(NavigationTab.APPOINTMENTS.labelMm, fontSize = 10.sp) },
                            modifier = Modifier.testTag("tab_appointments")
                        )
                    }
                }
            }
        ) { innerPadding ->
            if (isWebDeskMode) {
                WebDeskScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                // Large screen Navigation Rail
                if (isTablet) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationRailItem(
                            selected = currentTab == NavigationTab.INBOX,
                            onClick = { viewModel.selectTab(NavigationTab.INBOX) },
                            icon = { Icon(Icons.Default.Chat, contentDescription = null) },
                            label = { Text(NavigationTab.INBOX.labelMm, fontSize = 11.sp) }
                        )
                        NavigationRailItem(
                            selected = currentTab == NavigationTab.DAILY_TASKS,
                            onClick = { viewModel.selectTab(NavigationTab.DAILY_TASKS) },
                            icon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) },
                            label = { Text(NavigationTab.DAILY_TASKS.labelMm, fontSize = 11.sp) }
                        )
                        NavigationRailItem(
                            selected = currentTab == NavigationTab.AGING_TRACKER,
                            onClick = { viewModel.selectTab(NavigationTab.AGING_TRACKER) },
                            icon = { Icon(Icons.Default.AccessTime, contentDescription = null) },
                            label = { Text(NavigationTab.AGING_TRACKER.labelMm, fontSize = 11.sp) }
                        )
                        NavigationRailItem(
                            selected = currentTab == NavigationTab.NEW_CUSTOMERS,
                            onClick = { viewModel.selectTab(NavigationTab.NEW_CUSTOMERS) },
                            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            label = { Text(NavigationTab.NEW_CUSTOMERS.labelMm, fontSize = 11.sp) }
                        )
                        NavigationRailItem(
                            selected = currentTab == NavigationTab.APPOINTMENTS,
                            onClick = { viewModel.selectTab(NavigationTab.APPOINTMENTS) },
                            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            label = { Text(NavigationTab.APPOINTMENTS.labelMm, fontSize = 11.sp) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        NavigationTab.INBOX -> {
                            InboxScreen(
                                tickets = filteredTickets,
                                selectedChannel = selectedChannel,
                                selectedPriority = selectedPriority,
                                searchQuery = searchQuery,
                                onSelectChannel = { viewModel.selectChannel(it) },
                                onSelectPriority = { viewModel.selectPriority(it) },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onOpenTicket = { viewModel.openTicket(it) },
                                onTogglePriority = { viewModel.togglePriority(it) },
                                onBookAppointment = { ticket ->
                                    appointmentTicketToSchedule = ticket
                                },
                                onQuickCall = { ticket ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${ticket.customerPhone}")
                                    }
                                    context.startActivity(intent)
                                },
                                onOpenSimulateDialog = { showSimulateDialog = true },
                                onCreateTicket = {
                                    createDialogInitialType = TicketType.LOS_RED_LIGHT
                                    showCreateDialog = true
                                }
                            )
                        }

                        NavigationTab.DAILY_TASKS -> {
                            DailyTasksScreen(
                                tickets = allTickets,
                                onOpenTicket = { viewModel.openTicket(it) },
                                onTogglePriority = { viewModel.togglePriority(it) },
                                onBookAppointment = { ticket ->
                                    appointmentTicketToSchedule = ticket
                                },
                                onQuickCall = { ticket ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${ticket.customerPhone}")
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }

                        NavigationTab.AGING_TRACKER -> {
                            AgingTrackerScreen(
                                tickets = allTickets,
                                selectedFilter = selectedAgingFilter,
                                onSelectFilter = { viewModel.selectAgingFilter(it) },
                                onOpenTicket = { viewModel.openTicket(it) },
                                onTogglePriority = { viewModel.togglePriority(it) },
                                onBookAppointment = { ticket ->
                                    appointmentTicketToSchedule = ticket
                                },
                                onQuickCall = { ticket ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${ticket.customerPhone}")
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }

                        NavigationTab.NEW_CUSTOMERS -> {
                            NewCustomerScreen(
                                tickets = allTickets,
                                onOpenTicket = { viewModel.openTicket(it) },
                                onTogglePriority = { viewModel.togglePriority(it) },
                                onBookAppointment = { ticket ->
                                    appointmentTicketToSchedule = ticket
                                },
                                onQuickCall = { ticket ->
                                    val intent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${ticket.customerPhone}")
                                    }
                                    context.startActivity(intent)
                                },
                                onCreateNewCustomer = {
                                    createDialogInitialType = TicketType.NEW_CUSTOMER
                                    showCreateDialog = true
                                }
                            )
                        }

                        NavigationTab.APPOINTMENTS -> {
                            AppointmentScreen(
                                tickets = allTickets,
                                onOpenTicket = { viewModel.openTicket(it) },
                                onReschedule = { ticket ->
                                    appointmentTicketToSchedule = ticket
                                },
                                onMarkCompleted = { ticketId ->
                                    viewModel.updateTicketStatus(ticketId, TicketStatus.RESOLVED)
                                },
                                onNewAppointmentClick = {
                                    createDialogInitialType = TicketType.LOS_RED_LIGHT
                                    showCreateDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

    // Modal Dialogs
    if (showCreateDialog) {
        CreateTicketDialog(
            initialType = createDialogInitialType,
            onDismiss = { showCreateDialog = false },
            onCreate = { customerName, phone, address, township, plan, channel, priority, ticketType, initialMessage, opticalPower, splitterPort, appointmentDate, appointmentSlot ->
                viewModel.createTicket(
                    customerName,
                    phone,
                    address,
                    township,
                    plan,
                    channel,
                    priority,
                    ticketType,
                    initialMessage,
                    opticalPower,
                    splitterPort,
                    appointmentDate,
                    appointmentSlot
                )
            }
        )
    }

    if (showSimulateDialog) {
        SimulateInboundDialog(
            onDismiss = { showSimulateDialog = false },
            onSimulate = { channel, customerName, phone, township, plan, message, priority, ticketType ->
                viewModel.simulateIncomingInquiry(
                    channel,
                    customerName,
                    phone,
                    township,
                    plan,
                    message,
                    priority,
                    ticketType
                )
            }
        )
    }

    if (showIntegrationDialog) {
        ChannelIntegrationDialog(
            viewModel = viewModel,
            onDismiss = { showIntegrationDialog = false }
        )
    }

    if (appointmentTicketToSchedule != null) {
        ScheduleAppointmentDialog(
            ticket = appointmentTicketToSchedule!!,
            onDismiss = { appointmentTicketToSchedule = null },
            onConfirm = { dateMillis, timeSlot, technician, autoNotify ->
                viewModel.bookAppointment(
                    appointmentTicketToSchedule!!.id,
                    dateMillis,
                    timeSlot,
                    technician,
                    autoNotify
                )
                appointmentTicketToSchedule = null
            }
        )
    }
}
