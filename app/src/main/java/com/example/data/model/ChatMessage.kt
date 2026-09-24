package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageSender {
    CUSTOMER,
    AGENT,
    SYSTEM
}

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ticketId: Long,
    val sender: MessageSender,
    val senderName: String,
    val channel: SupportChannel,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSentSuccess: Boolean = true
)
