package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SupportChannel

fun getCannedRepliesForChannel(channel: SupportChannel, isComplain: Boolean): List<Pair<String, String>> {
    val chName = channel.displayName
    return if (isComplain) {
        listOf(
            "🚨 $chName မှတ်တမ်းတင်" to "[$chName Record] ဖောက်သည်ထံမှ Complain တိုင်ကြားမှုကို လက်ခံရရှိပြီး Ticket ဖွင့်လှစ်စစ်ဆေးနေပါပြီ။",
            "📅 နည်းပညာရှင်ရက်ချိန်း" to "[$chName Record] On-site လာရောက်စစ်ဆေးပြင်ဆင်ပေးရန် ရက်ချိန်းသတ်မှတ်ပြီး ဖြစ်ပါသည်။",
            "🔄 Router Restart" to "[$chName Support] Router power အား ၅ မိနစ်ခန့်ပိတ်ပြီး ပြန်လည်စမ်းသပ်ပေးပါရန် မေတ္တာရပ်ခံအပ်ပါသည်။",
            "⚡ LOS မီးနီစစ်ဆေး" to "[$chName NOC] Optical fiber မီးနီပြသနေမှုအတွက် Field Splicer အဖွဲ့ စေလွှတ်ပေးထားပါသည်။",
            "✅ ဖြေရှင်းပြီးစီးကြောင်း" to "[$chName Support] အင်တာနက်လိုင်း ချို့ယွင်းချက်အား ဖြေရှင်းစစ်ဆေးပြီးပါပြီ။ ပုံမှန်အသုံးပြုနိုင်ပါပြီခင်ဗျာ။"
        )
    } else {
        listOf(
            "🏠 $chName လိုင်းအသစ်လက်ခံ" to "[$chName Record] FTTH လိုင်းအသစ် တပ်ဆင်ရန် လျှောက်ထားမှုကို လက်ခံရရှိပြီး တပ်ဆင်ရေးအဖွဲ့နှင့် ချိတ်ဆက်ပေးပါမည်။",
            "📅 တပ်ဆင်ရက်ချိန်း" to "[$chName Install] လိုင်းအသစ်တပ်ဆင်ရန် ရက်ချိန်းသတ်မှတ်ပြီးပါပြီ။ နည်းပညာရှင်မှ မလာရောက်မီ ဆက်သွယ်ပါမည်။",
            "📍 Coverage စစ်ဆေး" to "[$chName Sales] လျှောက်ထားသော လိပ်စာရှိ Splitter box port လွတ်ရှိမှုအား စစ်ဆေးအတည်ပြုပေးပါမည်။",
            "💳 အခွန်နှင့် ဘေလ်" to "[$chName Billing] ပထမလ ဝန်ဆောင်ခနှင့် တပ်ဆင်ခ ပေးသွင်းမှုအား မှတ်တမ်းတင်ပြီးပါပြီ။ ကျေးဇူးတင်ပါသည်။"
        )
    }
}

@Composable
fun CannedRepliesRow(
    channel: SupportChannel = SupportChannel.VIBER,
    isComplain: Boolean = true,
    onSelectReply: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val replies = getCannedRepliesForChannel(channel, isComplain)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        replies.forEach { (label, replyText) ->
            AssistChip(
                onClick = { onSelectReply(replyText) },
                label = { Text(text = label, fontSize = 12.sp) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
