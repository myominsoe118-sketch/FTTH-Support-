package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportChannel
import com.example.data.model.SupportTicket
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.ui.components.ChannelBadge
import com.example.ui.components.PriorityBadge

enum class AppointmentCategoryFilter(val labelMm: String, val labelEn: String) {
    ALL("အားလုံး", "All"),
    COMPLAIN("Complain ပြင်ဆင်ရေး", "Complain Repairs"),
    NEW_INSTALL("အသစ်တပ်ဆင်", "New Installs")
}

@Composable
fun AppointmentScreen(
    tickets: List<SupportTicket>,
    onOpenTicket: (Long) -> Unit,
    onReschedule: (SupportTicket) -> Unit,
    onMarkCompleted: (Long) -> Unit,
    onNewAppointmentClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(AppointmentCategoryFilter.ALL) }
    var selectedChannelFilter by remember { mutableStateOf(SupportChannel.ALL) }

    val allScheduledTickets = tickets.filter { it.appointmentTimeSlot != null || it.status == TicketStatus.SCHEDULED }

    val filteredScheduled = allScheduledTickets.filter { ticket ->
        val matchCategory = when (selectedCategory) {
            AppointmentCategoryFilter.ALL -> true
            AppointmentCategoryFilter.COMPLAIN -> ticket.ticketType != TicketType.NEW_CUSTOMER
            AppointmentCategoryFilter.NEW_INSTALL -> ticket.ticketType == TicketType.NEW_CUSTOMER
        }
        val matchChannel = selectedChannelFilter == SupportChannel.ALL || ticket.channel == selectedChannelFilter
        matchCategory && matchChannel
    }

    val complainCount = allScheduledTickets.count { it.ticketType != TicketType.NEW_CUSTOMER }
    val newInstallCount = allScheduledTickets.count { it.ticketType == TicketType.NEW_CUSTOMER }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Appointments Header Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF512DA8)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FTTH ရက်ချိန်းများ စီမံခန့်ခွဲခြင်း",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF311B92)
                                )
                                Text(
                                    text = "Viber, Telegram, Messenger မှ အသစ်တပ်ဆင် + Complain ရက်ချိန်းများ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF512DA8)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("🚨 Complain ပြင်ဆင်ရန်", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
                                    Text("$complainCount ခု", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("🏠 လိုင်းသစ် တပ်ဆင်ရန်", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                    Text("$newInstallCount ခု", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }
                }
            }

            // Category Tab Row (Complain vs New Install)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppointmentCategoryFilter.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text("${cat.labelMm} (${cat.labelEn})", fontSize = 11.sp) }
                        )
                    }
                }
            }

            // Channel Filter Selector Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = SupportChannel.values().indexOf(selectedChannelFilter),
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SupportChannel.values().forEach { ch ->
                        Tab(
                            selected = selectedChannelFilter == ch,
                            onClick = { selectedChannelFilter = ch },
                            text = { Text(ch.displayName, fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (filteredScheduled.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ရွေးချယ်ထားသော စာရင်းတွင် ရက်ချိန်း မရှိသေးပါ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredScheduled, key = { "appt_${it.id}" }) { ticket ->
                    AppointmentCard(
                        ticket = ticket,
                        onOpenTicket = { onOpenTicket(ticket.id) },
                        onReschedule = { onReschedule(ticket) },
                        onMarkCompleted = { onMarkCompleted(ticket.id) },
                        onCallCustomer = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${ticket.customerPhone}")
                            }
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        // Floating Action Button to quickly book/create appointment
        FloatingActionButton(
            onClick = onNewAppointmentClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFF512DA8),
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Appointment")
        }
    }
}

@Composable
fun AppointmentCard(
    ticket: SupportTicket,
    onOpenTicket: () -> Unit,
    onReschedule: () -> Unit,
    onMarkCompleted: () -> Unit,
    onCallCustomer: () -> Unit
) {
    val isComplain = ticket.ticketType != TicketType.NEW_CUSTOMER

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Time Slot Badge & Type & Priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEDE7F6))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF512DA8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ticket.appointmentTimeSlot ?: "အချိန်မသတ်မှတ်ရသေး",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF512DA8)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChannelBadge(channel = ticket.channel)
                    PriorityBadge(priority = ticket.priority)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Complain / New Customer label with Channel identifier
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isComplain) Color(0xFFFFEBEE) else Color(0xFFE8F5E9))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isComplain) "📢 ${ticket.channel.displayName} Complain On-site ပြင်ဆင်ရေး: ${ticket.ticketType.labelMm}"
                    else "🏠 ${ticket.channel.displayName} မှ အသစ်တပ်ဆင်ရက်ချိန်း",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isComplain) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Ticket No
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ticket.customerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = ticket.ticketNo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Address
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${ticket.customerAddress} (${ticket.township})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Assigned Tech & Plan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👷 တာဝန်ကျ: ${ticket.assignedTechnician}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF37474F)
                )
                Text(
                    text = ticket.servicePlan,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Latest channel message record
            if (ticket.latestMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "မှတ်တမ်း: ${ticket.latestMessage}",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCallCustomer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color(0xFF2E7D32)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onReschedule,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("ရက်ပြောင်း (Reschedule)", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            if (ticket.status != TicketStatus.RESOLVED) {
                                onMarkCompleted()
                            } else {
                                onOpenTicket()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (ticket.status == TicketStatus.RESOLVED) Color.Gray else Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (ticket.status == TicketStatus.RESOLVED) "ပြီးစီးပြီး" else "ပြီးစီးကြောင်းမှတ်သား",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
