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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.BotTestResult
import com.example.ui.SupportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelIntegrationDialog(
    viewModel: SupportViewModel,
    onDismiss: () -> Unit
) {
    val config by (viewModel.channelConfig?.collectAsState()
        ?: remember { mutableStateOf(null) })

    var selectedTab by remember { mutableIntStateOf(0) }

    // Telegram state
    var tgTokenInput by remember(config?.telegramBotToken) {
        mutableStateOf(config?.telegramBotToken.orEmpty())
    }
    var tgTesting by remember { mutableStateOf(false) }
    var tgStatusMsg by remember { mutableStateOf<String?>(null) }
    var tgSuccess by remember { mutableStateOf(config?.telegramBotName?.isNotBlank() == true) }
    var syncingNow by remember { mutableStateOf(false) }
    var syncResultMsg by remember { mutableStateOf<String?>(null) }

    // Viber state
    var viberTokenInput by remember(config?.viberAuthToken) {
        mutableStateOf(config?.viberAuthToken.orEmpty())
    }
    var viberSenderInput by remember(config?.viberSenderName) {
        mutableStateOf(config?.viberSenderName ?: "FTTH Support")
    }
    var viberSaved by remember { mutableStateOf(false) }

    // Messenger state
    var messengerTokenInput by remember(config?.messengerPageAccessToken) {
        mutableStateOf(config?.messengerPageAccessToken.orEmpty())
    }
    var messengerSaved by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF00897B), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "တကယ့် App များနှင့် ချိတ်ဆက်မှု",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Viber • Telegram • Messenger • Python Webhook",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_integration")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Telegram / Viber / Messenger / Web App Server
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Telegram", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Viber", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Messenger", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Python Web", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (selectedTab) {
                        // ==========================================
                        // 0. TELEGRAM BOT INTEGRATION
                        // ==========================================
                        0 -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF0288D1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Telegram Bot Token ထည့်သွင်းထားပါက Customer များ Telegram Bot သို့ ပေးပို့သော စာများသည် Inbox သို့ တိုက်ရိုက်ရောက်ရှိပြီး၊ ပြန်လည်ဖြေကြားပါက Customer ၏ Telegram ထံ တိုက်ရိုက် ပေးပို့နိုင်ပါသည်။",
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF01579B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Telegram Bot Token",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = tgTokenInput,
                                onValueChange = {
                                    tgTokenInput = it
                                    tgStatusMsg = null
                                },
                                placeholder = { Text("ဥပမာ: 7123456789:AAHk3... (From @BotFather)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_telegram_token")
                            )

                            if (tgStatusMsg != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = tgStatusMsg.orEmpty(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (tgSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        tgTesting = true
                                        tgStatusMsg = null
                                        viewModel.saveTelegramToken(tgTokenInput) { result ->
                                            tgTesting = false
                                            tgSuccess = result.isSuccess
                                            tgStatusMsg = if (result.isSuccess) {
                                                "✅ ချိတ်ဆက်အောင်မြင်ပါသည်: ${result.botName}"
                                            } else {
                                                "❌ ချိတ်ဆက်မရပါ: ${result.errorMessage}"
                                            }
                                        }
                                    },
                                    enabled = !tgTesting && tgTokenInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF29B6F6)),
                                    modifier = Modifier.weight(1f).testTag("btn_test_telegram")
                                ) {
                                    if (tgTesting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("စစ်ဆေးနေသည်...", fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("စစ်ဆေးချိတ်ဆက်မည်", fontSize = 12.sp)
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        syncingNow = true
                                        viewModel.triggerManualTelegramSync { count ->
                                            syncingNow = false
                                            syncResultMsg = if (count > 0) "မက်ဆေ့ခ်ျအသစ် $count စောင် လက်ခံရရှိပါသည်!" else "မက်ဆေ့ခ်ျအသစ် မရှိသေးပါ။"
                                        }
                                    },
                                    enabled = !syncingNow && tgTokenInput.isNotBlank(),
                                    modifier = Modifier.weight(1f).testTag("btn_sync_telegram")
                                ) {
                                    if (syncingNow) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("မက်ဆေ့ခ်ျစစ်မည်", fontSize = 12.sp)
                                }
                            }

                            if (syncResultMsg != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = syncResultMsg.orEmpty(),
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF0288D1),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(10.dp))

                            // Auto Polling Switch
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Live Auto-Sync (အလိုအလျောက်ရယူခြင်း)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Customer များ စာပို့ပါက ချက်ချင်း Inbox ထဲသို့ အလိုအလျောက် ဝင်ရောက်စေရန် ဖွင့်ထားပါ။",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = config?.isTelegramPollingActive == true,
                                    onCheckedChange = { viewModel.setTelegramPolling(it) },
                                    modifier = Modifier.testTag("switch_telegram_polling")
                                )
                            }
                        }

                        // ==========================================
                        // 1. VIBER BOT INTEGRATION
                        // ==========================================
                        1 -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF7360F2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Viber Partners Admin Panel မှ Bot Create ပြုလုပ်ပြီး ရရှိလာသော 'X-Viber-Auth-Token' ကို ထည့်သွင်းပါ။ Customer ၏ Viber ID သို့ တိုက်ရိုက် Reply ပြန်ပို့နိုင်ပါသည်။",
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF4A148C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Viber Auth Token", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = viberTokenInput,
                                onValueChange = {
                                    viberTokenInput = it
                                    viberSaved = false
                                },
                                placeholder = { Text("ဥပမာ: 50f28e21a4f... (Viber Bot Token)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_viber_token")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Sender Name (Display Name)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = viberSenderInput,
                                onValueChange = {
                                    viberSenderInput = it
                                    viberSaved = false
                                },
                                placeholder = { Text("ဥပမာ: FTTH Fiber Customer Desk", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.saveViberToken(viberTokenInput, viberSenderInput)
                                    viberSaved = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7360F2)),
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_viber")
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (viberSaved) "သိမ်းဆည်းပြီးပါပြီ ✅" else "Viber Credentials သိမ်းဆည်းမည်", fontSize = 12.sp)
                            }
                        }

                        // ==========================================
                        // 2. FACEBOOK MESSENGER INTEGRATION
                        // ==========================================
                        2 -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF0084FF),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Facebook Developer Portal ရှိ သင်၏ ISP Page မှ ရရှိသော 'Page Access Token' ကို ထည့်သွင်းပါ။ Customer ၏ Messenger PSID သို့ တိုက်ရိုက် Outbound စာပို့နိုင်ပါသည်။",
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFF0D47A1)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text("Page Access Token", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = messengerTokenInput,
                                onValueChange = {
                                    messengerTokenInput = it
                                    messengerSaved = false
                                },
                                placeholder = { Text("ဥပမာ: EAAG... (Facebook Page Access Token)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_messenger_token")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.saveMessengerToken(messengerTokenInput)
                                    messengerSaved = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0084FF)),
                                modifier = Modifier.fillMaxWidth().testTag("btn_save_messenger")
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (messengerSaved) "သိမ်းဆည်းပြီးပါပြီ ✅" else "Messenger Token သိမ်းဆည်းမည်", fontSize = 12.sp)
                            }
                        }

                        // ==========================================
                        // 3. PYTHON WEB APP & WEBHOOK ARCHITECTURE
                        // ==========================================
                        3 -> {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color(0xFFE65100))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Python FastAPI Web App (24/7 Webhook Server)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "သင်လိုချင်သည့် Python Web App ကို ဤ Project ၏ /backend_server/ folder တွင် အပြည့်အစုံ ရေးသားထည့်သွင်းပေးထားပါသည်။\n• Viber Webhook (/webhook/viber)\n• Telegram Webhook (/webhook/telegram)\n• Messenger Webhook (/webhook/messenger)\n• HTML/JS Web Dashboard (Browser မှ တိုက်ရိုက်သုံးနိုင်)",
                                        fontSize = 11.5.sp,
                                        lineHeight = 16.sp,
                                        color = Color(0xFFBF360C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("အသုံးပြုပုံ အဆင့်ဆင့် (How to Run Python Web App):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF263238), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "1. cd backend_server\n2. pip install -r requirements.txt\n3. python main.py\n4. Open browser: http://localhost:8000",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF80CBC4)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ပိတ်မည် (Close)")
                }
            }
        }
    }
}
