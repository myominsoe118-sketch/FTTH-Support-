package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.SupportTicket

val OverdueRed = Color(0xFFD32F2F)
val WarningAmber = Color(0xFFF57C00)
val FreshGreen = Color(0xFF2E7D32)

@Composable
fun AgingBadge(
    ticket: SupportTicket,
    modifier: Modifier = Modifier
) {
    val now = System.currentTimeMillis()
    val isOverdue = ticket.isOverdue(now)
    val isWarning = ticket.isWarning(now)
    val ageText = ticket.ageFormatted(now)

    val (bgColor, textColor, borderColor, icon, statusText) = when {
        isOverdue -> {
            Tuple5(
                Color(0xFFFFEBEE),
                OverdueRed,
                OverdueRed,
                Icons.Default.Warning,
                "ရက်ကြာနေပြီ! (>48h)"
            )
        }
        isWarning -> {
            Tuple5(
                Color(0xFFFFF3E0),
                WarningAmber,
                WarningAmber.copy(alpha = 0.6f),
                Icons.Default.HourglassBottom,
                "၁-၂ ရက်ကြာ (Pending)"
            )
        }
        else -> {
            Tuple5(
                Color(0xFFE8F5E9),
                FreshGreen,
                FreshGreen.copy(alpha = 0.5f),
                Icons.Default.Schedule,
                "ယနေ့အသစ်"
            )
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = statusText,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$ageText • $statusText",
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
