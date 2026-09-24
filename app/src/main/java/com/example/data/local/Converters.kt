package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MessageSender
import com.example.data.model.SupportChannel
import com.example.data.model.TicketPriority
import com.example.data.model.TicketStatus
import com.example.data.model.TicketType

class Converters {
    @TypeConverter
    fun fromSupportChannel(value: SupportChannel?): String = value?.name ?: SupportChannel.VIBER.name

    @TypeConverter
    fun toSupportChannel(value: String?): SupportChannel = try {
        SupportChannel.valueOf(value ?: SupportChannel.VIBER.name)
    } catch (e: Exception) {
        SupportChannel.VIBER
    }

    @TypeConverter
    fun fromTicketPriority(value: TicketPriority?): String = value?.name ?: TicketPriority.NORMAL.name

    @TypeConverter
    fun toTicketPriority(value: String?): TicketPriority = try {
        TicketPriority.valueOf(value ?: TicketPriority.NORMAL.name)
    } catch (e: Exception) {
        TicketPriority.NORMAL
    }

    @TypeConverter
    fun fromTicketType(value: TicketType?): String = value?.name ?: TicketType.LOS_RED_LIGHT.name

    @TypeConverter
    fun toTicketType(value: String?): TicketType = try {
        TicketType.valueOf(value ?: TicketType.LOS_RED_LIGHT.name)
    } catch (e: Exception) {
        TicketType.LOS_RED_LIGHT
    }

    @TypeConverter
    fun fromTicketStatus(value: TicketStatus?): String = value?.name ?: TicketStatus.OPEN.name

    @TypeConverter
    fun toTicketStatus(value: String?): TicketStatus = try {
        TicketStatus.valueOf(value ?: TicketStatus.OPEN.name)
    } catch (e: Exception) {
        TicketStatus.OPEN
    }

    @TypeConverter
    fun fromMessageSender(value: MessageSender?): String = value?.name ?: MessageSender.CUSTOMER.name

    @TypeConverter
    fun toMessageSender(value: String?): MessageSender = try {
        MessageSender.valueOf(value ?: MessageSender.CUSTOMER.name)
    } catch (e: Exception) {
        MessageSender.CUSTOMER
    }
}
