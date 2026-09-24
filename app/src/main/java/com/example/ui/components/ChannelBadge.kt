package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportChannel

val ViberColor = Color(0xFF7360F2)
val TelegramColor = Color(0xFF0288D1)
val MessengerColor = Color(0xFF0084FF)
val PhoneColor = Color(0xFF00796B)

@Composable
fun ChannelBadge(
    channel: SupportChannel,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val (bgColor, icon, label) = when (channel) {
        SupportChannel.VIBER -> Triple(ViberColor, Icons.Default.Chat, "Viber")
        SupportChannel.TELEGRAM -> Triple(TelegramColor, Icons.Default.Send, "Telegram")
        SupportChannel.MESSENGER -> Triple(MessengerColor, Icons.Default.Forum, "Messenger")
        SupportChannel.PHONE -> Triple(PhoneColor, Icons.Default.Call, "Phone")
        SupportChannel.ALL -> Triple(Color(0xFF546E7A), Icons.Default.Forum, "All")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
            if (showLabel) {
                Text(
                    text = " $label",
                    color = bgColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
