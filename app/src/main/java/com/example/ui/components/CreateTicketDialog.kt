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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SupportChannel
import com.example.data.model.TicketPriority
import com.example.data.model.TicketType

val YANGON_TOWNSHIPS = listOf(
    "လှိုင် (Hlaing)",
    "ကမာရွတ် (Kamayut)",
    "စမ်းချောင်း (Sanchaung)",
    "ဗဟန်း (Bahan)",
    "ရန်ကင်း (Yankin)",
    "မရမ်းကုန်း (Mayangone)",
    "အင်းစိန် (Insein)",
    "တောင်ဥက္ကလာ (S. Okkalapa)",
    "မြောက်ဥက္ကလာ (N. Okkalapa)",
    "ဒဂုံမြို့သစ်မြောက်ပိုင်း (N. Dagon)"
)

val FTTH_PLANS = listOf(
    "FTTH 50 Mbps Home (28,000 Ks)",
    "FTTH 100 Mbps Pro (45,000 Ks)",
    "FTTH 200 Mbps Ultra (75,000 Ks)",
    "FTTH 300 Mbps Enterprise"
)

val TIME_SLOTS = listOf(
    "09:00 AM - 12:00 PM (နံနက်ပိုင်း)",
    "01:00 PM - 04:00 PM (မွန်းလွဲပိုင်း)",
    "04:00 PM - 06:00 PM (ညနေပိုင်း)"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTicketDialog(
    initialType: TicketType = TicketType.LOS_RED_LIGHT,
    initialChannel: SupportChannel = SupportChannel.VIBER,
    onDismiss: () -> Unit,
    onCreate: (
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
    ) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedTownship by remember { mutableStateOf(YANGON_TOWNSHIPS.first()) }
    var townshipExpanded by remember { mutableStateOf(false) }

    var selectedPlan by remember { mutableStateOf(FTTH_PLANS.first()) }
    var planExpanded by remember { mutableStateOf(false) }

    var selectedChannel by remember { mutableStateOf(initialChannel) }
    var selectedPriority by remember { mutableStateOf(TicketPriority.NORMAL) }
    var selectedTicketType by remember { mutableStateOf(initialType) }

    var initialMessage by remember { mutableStateOf("") }
    var needAppointment by remember { mutableStateOf(initialType == TicketType.NEW_CUSTOMER) }
    var selectedSlot by remember { mutableStateOf(TIME_SLOTS.first()) }

    val isComplain = selectedTicketType != TicketType.NEW_CUSTOMER

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isComplain) "Complain Ticket မှတ်တမ်းတင်ရန်" else "ဖောက်သည်အသစ် တပ်ဆင်ခွင့် ဖွင့်ရန်",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${selectedChannel.displayName} မှ တိုက်ရိုက်ရက်ချိန်းနှင့် Record ဖြည့်သွင်းခြင်း",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Direct Channel Selector
                Text(
                    text = "ဆက်သွယ်လာသော Channel (Viber, Telegram, Messenger):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        SupportChannel.VIBER,
                        SupportChannel.TELEGRAM,
                        SupportChannel.MESSENGER,
                        SupportChannel.PHONE
                    ).forEach { channel ->
                        FilterChip(
                            selected = selectedChannel == channel,
                            onClick = { selectedChannel = channel },
                            label = { Text(channel.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Ticket Category Selection
                Text(
                    text = "လုပ်ငန်းစဉ် အမျိုးအစား (Category):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTicketType == TicketType.NEW_CUSTOMER,
                        onClick = {
                            selectedTicketType = TicketType.NEW_CUSTOMER
                            needAppointment = true
                        },
                        label = { Text("🏠 အသစ်တပ်ဆင်") },
                        modifier = Modifier.testTag("type_new_customer")
                    )
                    FilterChip(
                        selected = selectedTicketType == TicketType.LOS_RED_LIGHT,
                        onClick = { selectedTicketType = TicketType.LOS_RED_LIGHT },
                        label = { Text("🚨 Complain: LOS မီးနီ") },
                        modifier = Modifier.testTag("type_los")
                    )
                    FilterChip(
                        selected = selectedTicketType == TicketType.SLOW_SPEED,
                        onClick = { selectedTicketType = TicketType.SLOW_SPEED },
                        label = { Text("⚡ Complain: လိုင်းနှေး") },
                        modifier = Modifier.testTag("type_slow")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority Selection
                Text(
                    text = "ဦးစားပေးအဆင့် (Priority):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedPriority == TicketPriority.URGENT,
                        onClick = { selectedPriority = TicketPriority.URGENT },
                        label = { Text("⚡ အရေးကြီး (Urgent)", color = if (selectedPriority == TicketPriority.URGENT) Color.Red else Color.Unspecified) },
                        modifier = Modifier.testTag("priority_urgent")
                    )
                    FilterChip(
                        selected = selectedPriority == TicketPriority.NORMAL,
                        onClick = { selectedPriority = TicketPriority.NORMAL },
                        label = { Text("သာမန် (Normal)") },
                        modifier = Modifier.testTag("priority_normal")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Customer Info Fields
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("ဖောက်သည်အမည် (${selectedChannel.displayName} User)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_customer_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("ဖုန်းနံပါတ် (Phone Number)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_phone"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Township Dropdown
                ExposedDropdownMenuBox(
                    expanded = townshipExpanded,
                    onExpandedChange = { townshipExpanded = !townshipExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTownship,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("မြို့နယ် (Township)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = townshipExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = townshipExpanded,
                        onDismissRequest = { townshipExpanded = false }
                    ) {
                        YANGON_TOWNSHIPS.forEach { township ->
                            DropdownMenuItem(
                                text = { Text(township) },
                                onClick = {
                                    selectedTownship = township
                                    townshipExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("နေရပ်လိပ်စာ (Street / Building)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Plan Dropdown
                ExposedDropdownMenuBox(
                    expanded = planExpanded,
                    onExpandedChange = { planExpanded = !planExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPlan,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("FTTH Plan") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = planExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = planExpanded,
                        onDismissRequest = { planExpanded = false }
                    ) {
                        FTTH_PLANS.forEach { plan ->
                            DropdownMenuItem(
                                text = { Text(plan) },
                                onClick = {
                                    selectedPlan = plan
                                    planExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Complain / Message Detail Field with Channel tag reminder
                OutlinedTextField(
                    value = initialMessage,
                    onValueChange = { initialMessage = it },
                    label = {
                        Text(
                            if (isComplain) "${selectedChannel.displayName} Complain Record အကြောင်းအရာ"
                            else "${selectedChannel.displayName} မှတ်ချက်"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Appointment Section (Crucial for both Complain and New Install)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (needAppointment) Color(0xFFEDE7F6) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = if (isComplain) "🔧 ပြင်ဆင်ရေး On-site ရက်ချိန်းသတ်မှတ်မည်" else "🏠 လိုင်းသစ် တပ်ဆင်ရက်ချိန်း သတ်မှတ်မည်",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF512DA8)
                                )
                                Text(
                                    text = "${selectedChannel.displayName} customer ထံ ရက်ချိန်းအကြောင်းကြားချက် တွဲတင်ပေးမည်",
                                    fontSize = 10.sp,
                                    color = Color(0xFF512DA8)
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = needAppointment,
                                onCheckedChange = { needAppointment = it }
                            )
                        }

                        if (needAppointment) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "အချိန်အပိုင်းအခြား (Time Slot):",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            TIME_SLOTS.forEach { slot ->
                                FilterChip(
                                    selected = selectedSlot == slot,
                                    onClick = { selectedSlot = slot },
                                    label = { Text(slot, fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("မလုပ်တော့ပါ (Cancel)")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && phone.isNotBlank()) {
                                val channelPrefix = "[${selectedChannel.displayName} Record]"
                                val finalMessage = if (initialMessage.isNotBlank()) {
                                    "$channelPrefix $initialMessage"
                                } else if (isComplain) {
                                    "$channelPrefix ဖောက်သည်ထံမှ ${selectedTicketType.labelMm} တိုင်ကြားချက်ကို လက်ခံမှတ်တမ်းတင်ထားပါသည်"
                                } else {
                                    "$channelPrefix FTTH လိုင်းအသစ် တပ်ဆင်ရန် လျှောက်ထားမှုကို လက်ခံမှတ်တမ်းတင်ပါသည်"
                                }

                                onCreate(
                                    customerName.trim(),
                                    phone.trim(),
                                    if (address.isBlank()) "လမ်းသစ်၊ $selectedTownship" else address.trim(),
                                    selectedTownship,
                                    selectedPlan.split(" (").first(),
                                    selectedChannel,
                                    selectedPriority,
                                    selectedTicketType,
                                    finalMessage,
                                    if (selectedTicketType == TicketType.LOS_RED_LIGHT) -34.5 else -21.0,
                                    "SP-${selectedTownship.take(3).uppercase()}-01 / Port ${(1..8).random()}",
                                    if (needAppointment) System.currentTimeMillis() else null,
                                    if (needAppointment) selectedSlot else null
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("submit_ticket_button"),
                        enabled = customerName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("အတည်ပြုဖွင့်မည် (Save Record)")
                    }
                }
            }
        }
    }
}
