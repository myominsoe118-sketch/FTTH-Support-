package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.data.model.SupportChannel
import com.example.data.model.TicketPriority
import com.example.data.model.TicketType

@Composable
fun SimulateInboundDialog(
    onDismiss: () -> Unit,
    onSimulate: (
        channel: SupportChannel,
        customerName: String,
        phone: String,
        township: String,
        plan: String,
        message: String,
        priority: TicketPriority,
        ticketType: TicketType
    ) -> Unit
) {
    var selectedChannel by remember { mutableStateOf(SupportChannel.VIBER) }
    var isComplain by remember { mutableStateOf(true) }
    var selectedPriority by remember { mutableStateOf(TicketPriority.URGENT) }
    var customerName by remember { mutableStateOf("ကိုဖြိုးဝေ (Ko Phyo Wai)") }
    var phone by remember { mutableStateOf("09401234567") }
    var township by remember { mutableStateOf("လှိုင် (Hlaing)") }
    var messageText by remember {
        mutableStateOf("မင်္ဂလာပါ Viber မှ ဆက်သွယ်ခြင်းပါ။ အခုပဲ fiber ကြိုးပြတ်ပြီး LOS မီးနီပြနေပါတယ် အရေးကြီးလို့ အမြန်လာပြင်ပေးပါ")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = null,
                            tint = Color(0xFFF57C00)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Channel အလိုက် စမ်းသပ်မက်ဆေ့ခ်ျ ပို့ရန်",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Viber, Telegram, Messenger တို့မှ Complain (သို့) အသစ်တပ်ဆင်လိုသူ မက်ဆေ့ခ်ျ ပေးပို့လာပုံ စမ်းသပ်ခြင်း",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Channels
                Text("လိုင်းရွေးချယ်ရန် (Channel):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(SupportChannel.VIBER, SupportChannel.TELEGRAM, SupportChannel.MESSENGER).forEach { ch ->
                        FilterChip(
                            selected = selectedChannel == ch,
                            onClick = {
                                selectedChannel = ch
                                messageText = if (isComplain) {
                                    "မင်္ဂလာပါ ${ch.displayName} မှ ဆက်သွယ်ခြင်းပါ။ LOS မီးနီလင်းပြီး လိုင်းပြတ်နေလို့ On-site စစ်ဆေးပေးဖို့ ရက်ချိန်းယူချင်ပါတယ်"
                                } else {
                                    "မင်္ဂလာပါ ${ch.displayName} မှ ဆက်သွယ်ခြင်းပါ။ FTTH 100Mbps လိုင်းအသစ် တပ်ဆင်လိုပါသဖြင့် တပ်ဆင်ရက်ချိန်း ပေးပါရန်"
                                }
                            },
                            label = { Text(ch.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Type: Complain vs New Customer
                Text("ရည်ရွယ်ချက် (Customer Inquiry Type):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isComplain,
                        onClick = {
                            isComplain = true
                            selectedPriority = TicketPriority.URGENT
                            messageText = "မင်္ဂလာပါ ${selectedChannel.displayName} မှ ဆက်သွယ်ခြင်းပါ။ LOS မီးနီလင်းပြီး လိုင်းပြတ်နေလို့ On-site စစ်ဆေးပေးဖို့ ရက်ချိန်းယူချင်ပါတယ်"
                        },
                        label = { Text("🚨 Complain တိုင်ကြားချက်") }
                    )
                    FilterChip(
                        selected = !isComplain,
                        onClick = {
                            isComplain = false
                            selectedPriority = TicketPriority.NORMAL
                            messageText = "မင်္ဂလာပါ ${selectedChannel.displayName} မှ ဆက်သွယ်ခြင်းပါ။ FTTH 100Mbps လိုင်းအသစ် တပ်ဆင်လိုပါသဖြင့် တပ်ဆင်ရက်ချိန်း ပေးပါရန်"
                        },
                        label = { Text("🏠 အသစ်တပ်ဆင်လိုသူ") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer အမည်") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("${selectedChannel.displayName} မက်ဆေ့ခ်ျ စာသား") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("ပိတ်မည်") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val recordPrefix = if (isComplain) "[${selectedChannel.displayName} Complain]" else "[${selectedChannel.displayName} New Order]"
                            onSimulate(
                                selectedChannel,
                                customerName,
                                phone,
                                township,
                                "FTTH 100 Mbps Pro",
                                "$recordPrefix $messageText",
                                selectedPriority,
                                if (isComplain) TicketType.LOS_RED_LIGHT else TicketType.NEW_CUSTOMER
                            )
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("${selectedChannel.displayName} မက်ဆေ့ခ်ျ ပေးပို့မည်")
                    }
                }
            }
        }
    }
}
