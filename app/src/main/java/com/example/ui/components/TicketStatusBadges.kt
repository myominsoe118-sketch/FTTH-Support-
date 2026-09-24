package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType

@Composable
fun PriorityBadge(
    priority: TicketPriority,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isUrgent = priority == TicketPriority.URGENT
    val bgColor = if (isUrgent) Color(0xFFC62828) else Color(0xFFECEFF1)
    val textColor = if (isUrgent) Color.White else Color(0xFF37474F)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isUrgent) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Urgent",
                    tint = Color(0xFFFFEB3B),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
            }
            Text(
                text = "${priority.labelMm} (${priority.labelEn})",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusBadge(
    status: TicketStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon) = when (status) {
        TicketStatus.OPEN -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), Icons.Default.NewReleases)
        TicketStatus.IN_PROGRESS -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), Icons.Default.PendingActions)
        TicketStatus.SCHEDULED -> Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), Icons.Default.Schedule)
        TicketStatus.RESOLVED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.CheckCircle)
        TicketStatus.CLOSED -> Triple(Color(0xFFECEFF1), Color(0xFF455A64), Icons.Default.FiberManualRecord)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = status.labelEn,
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${status.labelMm} (${status.labelEn})",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun TicketTypeBadge(
    type: TicketType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        TicketType.LOS_RED_LIGHT -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        TicketType.NEW_CUSTOMER -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        TicketType.SLOW_SPEED -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        TicketType.ROUTER_CONFIG -> Pair(Color(0xFFE0F7FA), Color(0xFF006064))
        TicketType.RELOCATION -> Pair(Color(0xFFF3E5F5), Color(0xFF6A1B9A))
        TicketType.BILLING -> Pair(Color(0xFFFBE9E7), Color(0xFFBF360C))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = type.labelMm,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
