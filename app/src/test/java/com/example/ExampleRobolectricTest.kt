package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SupportChannel
import com.example.data.model.SupportTicket
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches FTTH OmniSupport`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FTTH OmniSupport", appName)
    }

    @Test
    fun `ticket aging logic identifies overdue vs fresh tickets`() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - (3L * 24 * 60 * 60 * 1000)
        val twoHoursAgo = now - (2L * 60 * 60 * 1000)

        val overdueTicket = SupportTicket(
            ticketNo = "FTTH-9901",
            customerName = "U Min Thu",
            customerPhone = "09450000000",
            customerAddress = "Hlaing",
            township = "Hlaing",
            servicePlan = "FTTH 100 Mbps",
            channel = SupportChannel.VIBER,
            priority = TicketPriority.URGENT,
            ticketType = TicketType.LOS_RED_LIGHT,
            status = TicketStatus.OPEN,
            createdAt = threeDaysAgo
        )

        val freshTicket = SupportTicket(
            ticketNo = "FTTH-9902",
            customerName = "Daw Nwe Nwe",
            customerPhone = "09790000000",
            customerAddress = "Kamayut",
            township = "Kamayut",
            servicePlan = "FTTH 50 Mbps",
            channel = SupportChannel.TELEGRAM,
            priority = TicketPriority.NORMAL,
            ticketType = TicketType.NEW_CUSTOMER,
            status = TicketStatus.OPEN,
            createdAt = twoHoursAgo
        )

        assertTrue(overdueTicket.isOverdue(now))
        assertFalse(freshTicket.isOverdue(now))
        assertEquals(3L, overdueTicket.ageDays(now))
        assertEquals(2L, freshTicket.ageHours(now))
    }
}
