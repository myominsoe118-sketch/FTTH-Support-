package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SupportTicket
import com.example.data.model.TicketType

val TECHNICIANS = listOf(
    "ကိုကျော်ဇင် (Senior Splicer)",
    "ကိုအောင်မြင့် (FTTH Field Tech)",
    "ကိုသက်ပိုင် (Install Team Lead)",
    "ကိုတင်မောင် (NOC & Router Spec)"
)

val APPOINTMENT_DAYS = listOf(
    "ဒီကနေ့ (Today)",
    "မနက်ဖြန် (Tomorrow)",
    "သဘက်ခါ (In 2 Days)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleAppointmentDialog(
    ticket: SupportTicket,
    onDismiss: () -> Unit,
    onConfirm: (dateMillis: Long, timeSlot: String, technician: String, autoNotifyCustomer: Boolean) -> Unit
) {
    var selectedDayOption by remember { mutableStateOf(APPOINTMENT_DAYS.first()) }
    var selectedSlot by remember { mutableStateOf(TIME_SLOTS.first()) }
    var selectedTech by remember { mutableStateOf(TECHNICIANS.first()) }
    var techExpanded by remember { mutableStateOf(false) }
    var autoNotify by remember { mutableStateOf(true) }

    val isComplain = ticket.ticketType != TicketType.NEW_CUSTOMER
    val now = System.currentTimeMillis()
    val dayMillis = 24L * 60L * 60L * 1000L

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF512DA8)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isComplain) "Complain ရက်ချိန်းသတ်မှတ်ခြင်း" else "လိုင်းအသစ် တပ်ဆင်ရက်ချိန်း",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Ticket info & channel banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isComplain) Color(0xFFFFF3E0) else Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "${ticket.ticketNo} • ${ticket.customerName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isComplain) Color(0xFFBF360C) else Color(0xFF1B5E20)
                            )
                            Text(
                                text = "${ticket.ticketType.labelMm} (${ticket.township})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ChannelBadge(channel = ticket.channel)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date Selection
                Text("ရက်စွဲ (Appointment Day):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    APPOINTMENT_DAYS.forEach { day ->
                        FilterChip(
                            selected = selectedDayOption == day,
                            onClick = { selectedDayOption = day },
                            label = { Text(day, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Slots
                Text("အချိန်အပိုင်းအခြား (Time Slot):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                TIME_SLOTS.forEach { slot ->
                    FilterChip(
                        selected = selectedSlot == slot,
                        onClick = { selectedSlot = slot },
                        label = { Text(slot, fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Technician Dropdown
                Text("တာဝန်ကျ နည်းပညာရှင် (Technician):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = techExpanded,
                    onExpandedChange = { techExpanded = !techExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTech,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = techExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = techExpanded,
                        onDismissRequest = { techExpanded = false }
                    ) {
                        TECHNICIANS.forEach { tech ->
                            DropdownMenuItem(
                                text = { Text(tech) },
                                onClick = {
                                    selectedTech = tech
                                    techExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Channel Record & Auto Reply Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${ticket.channel.displayName} မှတစ်ဆင့် ရက်ချိန်းပြန်ကြားရန်",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "ရက်ချိန်းအကြောင်းကြားစာအား ${ticket.channel.displayName} ထဲသို့ auto record ထည့်သွင်းပေးမည်",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = autoNotify,
                        onCheckedChange = { autoNotify = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("မလုပ်တော့ပါ") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val computedDate = when (selectedDayOption) {
                                APPOINTMENT_DAYS[0] -> now
                                APPOINTMENT_DAYS[1] -> now + dayMillis
                                else -> now + (dayMillis * 2)
                            }
                            onConfirm(computedDate, selectedSlot, selectedTech, autoNotify)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8))
                    ) {
                        Text("ရက်ချိန်း အတည်ပြုမည်")
                    }
                }
            }
        }
    }
}
