package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.data.model.TicketStatus
import com.example.ui.AgingFilterOption
import com.example.ui.components.TicketCard

@Composable
fun AgingTrackerScreen(
    tickets: List<SupportTicket>,
    selectedFilter: AgingFilterOption,
    onSelectFilter: (AgingFilterOption) -> Unit,
    onOpenTicket: (Long) -> Unit,
    onTogglePriority: (SupportTicket) -> Unit,
    onQuickCall: (SupportTicket) -> Unit,
    onBookAppointment: (SupportTicket) -> Unit,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val openTickets = tickets.filter { it.status != TicketStatus.RESOLVED && it.status != TicketStatus.CLOSED }
    val overdueTickets = openTickets.filter { it.isOverdue(now) }
    val warningTickets = openTickets.filter { it.isWarning(now) }
    val freshTickets = openTickets.filter { !it.isOverdue(now) && !it.isWarning(now) }

    // Sorted by oldest ticket first (most urgent aging)
    val displayTickets = when (selectedFilter) {
        AgingFilterOption.ALL -> openTickets.sortedBy { it.createdAt }
        AgingFilterOption.OVERDUE -> overdueTickets.sortedBy { it.createdAt }
        AgingFilterOption.WARNING -> warningTickets.sortedBy { it.createdAt }
        AgingFilterOption.FRESH -> freshTickets.sortedBy { it.createdAt }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // SLA Status Overview Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF263238))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFFFFB74D),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ရက်ကြာမှု စစ်ဆေးခြင်း (SLA & Ticket Aging)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Overdue Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFD32F2F).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("ရက်ကြာနေပြီ", fontSize = 11.sp, color = Color(0xFFFFCDD2), fontWeight = FontWeight.SemiBold)
                            Text("${overdueTickets.size} ခု", fontSize = 18.sp, color = Color(0xFFFF8A80), fontWeight = FontWeight.ExtraBold)
                            Text("(> ၄၈ နာရီကျော်)", fontSize = 10.sp, color = Color(0xFFFFCDD2))
                        }
                    }

                    // Warning Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFF57C00).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("၁-၂ ရက်ကြာ", fontSize = 11.sp, color = Color(0xFFFFE0B2), fontWeight = FontWeight.SemiBold)
                            Text("${warningTickets.size} ခု", fontSize = 18.sp, color = Color(0xFFFFB74D), fontWeight = FontWeight.ExtraBold)
                            Text("(၂၄-၄၈ နာရီ)", fontSize = 10.sp, color = Color(0xFFFFE0B2))
                        }
                    }

                    // Fresh Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF388E3C).copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("ယနေ့အသစ်", fontSize = 11.sp, color = Color(0xFFC8E6C9), fontWeight = FontWeight.SemiBold)
                            Text("${freshTickets.size} ခု", fontSize = 18.sp, color = Color(0xFFA5D6A7), fontWeight = FontWeight.ExtraBold)
                            Text("(< ၂၄ နာရီ)", fontSize = 10.sp, color = Color(0xFFC8E6C9))
                        }
                    }
                }
            }
        }

        // Aging Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = AgingFilterOption.values().indexOf(selectedFilter),
            edgePadding = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            AgingFilterOption.values().forEach { option ->
                Tab(
                    selected = selectedFilter == option,
                    onClick = { onSelectFilter(option) },
                    text = {
                        Text(
                            text = "${option.labelMm} (${option.labelEn})",
                            fontSize = 12.sp,
                            fontWeight = if (selectedFilter == option) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tickets List sorted by age
        if (displayTickets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ရွေးချယ်ထားသော စာရင်းတွင် ရက်ကြာနေသော Ticket မရှိပါ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(displayTickets, key = { "aging_${it.id}" }) { ticket ->
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
}
