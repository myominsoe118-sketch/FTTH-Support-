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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportTicket
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import com.example.ui.components.TicketCard

@Composable
fun NewCustomerScreen(
    tickets: List<SupportTicket>,
    onOpenTicket: (Long) -> Unit,
    onTogglePriority: (SupportTicket) -> Unit,
    onQuickCall: (SupportTicket) -> Unit,
    onBookAppointment: (SupportTicket) -> Unit,
    onCreateNewCustomer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val newCustomerTickets = tickets.filter { it.ticketType == TicketType.NEW_CUSTOMER }
    val scheduledCount = newCustomerTickets.count { it.status == TicketStatus.SCHEDULED }
    val pendingCount = newCustomerTickets.count { it.status == TicketStatus.OPEN }
    val completedCount = newCustomerTickets.count { it.status == TicketStatus.RESOLVED }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Stats Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2E7D32)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "FTTH ဖောက်သည်အသစ် တပ်ဆင်မှုများ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Viber, Telegram, Messenger မှ တိုက်ရိုက်လက်ခံထားသော လိုင်းအသစ်များ",
                                    fontSize = 11.sp,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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
                                    Text("ဆိုင်းငံ့အသစ်", fontSize = 11.sp, color = Color(0xFF558B2F))
                                    Text("$pendingCount ဦး", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ရက်ချိန်းပြီး", fontSize = 11.sp, color = Color(0xFF512DA8))
                                    Text("$scheduledCount ဦး", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF512DA8))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("တပ်ဆင်ပြီး", fontSize = 11.sp, color = Color(0xFF00695C))
                                    Text("$completedCount ဦး", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
                                }
                            }
                        }
                    }
                }
            }

            // Customer cards
            if (newCustomerTickets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ဖောက်သည်အသစ် တောင်းဆိုမှု မရှိသေးပါ",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(newCustomerTickets, key = { "new_cust_${it.id}" }) { ticket ->
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

        // Floating Action Button
        FloatingActionButton(
            onClick = onCreateNewCustomer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_new_customer"),
            containerColor = Color(0xFF2E7D32),
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Customer")
        }
    }
}
