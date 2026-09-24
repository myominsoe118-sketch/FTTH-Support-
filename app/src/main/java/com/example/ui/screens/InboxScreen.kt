package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportChannel
import com.example.data.model.SupportTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketType
import com.example.ui.components.ChannelBadge
import com.example.ui.components.TicketCard

@Composable
fun InboxScreen(
    tickets: List<SupportTicket>,
    selectedChannel: SupportChannel,
    selectedPriority: TicketPriority?,
    searchQuery: String,
    onSelectChannel: (SupportChannel) -> Unit,
    onSelectPriority: (TicketPriority?) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenTicket: (Long) -> Unit,
    onTogglePriority: (SupportTicket) -> Unit,
    onQuickCall: (SupportTicket) -> Unit,
    onBookAppointment: (SupportTicket) -> Unit,
    onOpenSimulateDialog: () -> Unit,
    onCreateTicket: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTickets = tickets.size
    val urgentCount = tickets.count { it.priority == TicketPriority.URGENT }
    val complainCount = tickets.count { it.ticketType != TicketType.NEW_CUSTOMER }
    val newInstallCount = tickets.count { it.ticketType == TicketType.NEW_CUSTOMER }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Omnichannel Channels Bar
            ScrollableTabRow(
                selectedTabIndex = SupportChannel.values().indexOf(selectedChannel),
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                SupportChannel.values().forEach { channel ->
                    Tab(
                        selected = selectedChannel == channel,
                        onClick = { onSelectChannel(channel) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ChannelBadge(channel = channel, showLabel = false)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = channel.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedChannel == channel) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // Stats Quick Summary Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total Inquiries
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("စုစုပေါင်း", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("$totalTickets စောင်", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Complain Count
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Complain တိုင်ကြား", fontSize = 11.sp, color = Color(0xFFE65100))
                        Text("$complainCount ခု", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFF57C00))
                    }
                }

                // Urgent
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🚨 အရေးကြီး", fontSize = 11.sp, color = Color(0xFFC62828))
                        Text("$urgentCount ခု", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD32F2F))
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("အမည်၊ ဖုန်း၊ FTTH ID၊ မြို့နယ် ရှာရန်...", fontSize = 13.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .testTag("search_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Priority and Simulation Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedPriority == null,
                    onClick = { onSelectPriority(null) },
                    label = { Text("အားလုံး (All)") }
                )

                FilterChip(
                    selected = selectedPriority == TicketPriority.URGENT,
                    onClick = {
                        onSelectPriority(if (selectedPriority == TicketPriority.URGENT) null else TicketPriority.URGENT)
                    },
                    label = { Text("⚡ အရေးကြီး (Urgent Only)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFCDD2),
                        selectedLabelColor = Color(0xFFB71C1C)
                    )
                )

                FilterChip(
                    selected = selectedPriority == TicketPriority.NORMAL,
                    onClick = {
                        onSelectPriority(if (selectedPriority == TicketPriority.NORMAL) null else TicketPriority.NORMAL)
                    },
                    label = { Text("သာမန် (Normal)") }
                )

                OutlinedButton(
                    onClick = onOpenSimulateDialog,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("simulate_button")
                ) {
                    Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = Color(0xFFF57C00), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Channel အဝင်စမ်းသပ်ရန်", fontSize = 11.sp, color = Color(0xFFE65100))
                }
            }

            // Ticket List
            if (tickets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "မက်ဆေ့ခ်ျ သို့မဟုတ် တောင်းဆိုမှု မရှိပါ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "စစ်ထုတ်ထားသော ရှာဖွေမှုနှင့် ကိုက်ညီသည့် စာရင်းမရှိပါ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tickets, key = { it.id }) { ticket ->
                        TicketCard(
                            ticket = ticket,
                            onOpenTicket = { onOpenTicket(ticket.id) },
                            onTogglePriority = { onTogglePriority(ticket) },
                            onQuickCall = { onQuickCall(ticket) },
                            onBookAppointment = { onBookAppointment(ticket) }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create a ticket
        FloatingActionButton(
            onClick = onCreateTicket,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_create_ticket"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Ticket")
        }
    }
}
