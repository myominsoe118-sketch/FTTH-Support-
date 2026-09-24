package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChannelApiConfig(
    val telegramBotToken: String = "",
    val telegramBotName: String = "",
    val isTelegramPollingActive: Boolean = false,
    val viberAuthToken: String = "",
    val viberSenderName: String = "FTTH Support",
    val messengerPageAccessToken: String = "",
    val lastSyncStatus: String = "Not configured"
)

class ChannelSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ftth_channel_api_prefs", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<ChannelApiConfig> = _config.asStateFlow()

    private fun loadConfig(): ChannelApiConfig {
        return ChannelApiConfig(
            telegramBotToken = prefs.getString("tg_bot_token", "").orEmpty(),
            telegramBotName = prefs.getString("tg_bot_name", "").orEmpty(),
            isTelegramPollingActive = prefs.getBoolean("tg_polling_active", false),
            viberAuthToken = prefs.getString("viber_auth_token", "").orEmpty(),
            viberSenderName = prefs.getString("viber_sender_name", "FTTH Support").orEmpty(),
            messengerPageAccessToken = prefs.getString("messenger_page_token", "").orEmpty(),
            lastSyncStatus = prefs.getString("last_sync_status", "Ready").orEmpty()
        )
    }

    fun saveTelegramConfig(token: String, botName: String, pollingActive: Boolean) {
        prefs.edit()
            .putString("tg_bot_token", token.trim())
            .putString("tg_bot_name", botName.trim())
            .putBoolean("tg_polling_active", pollingActive)
            .apply()
        _config.value = loadConfig()
    }

    fun setTelegramPollingActive(active: Boolean) {
        prefs.edit().putBoolean("tg_polling_active", active).apply()
        _config.value = loadConfig()
    }

    fun saveViberConfig(token: String, senderName: String) {
        prefs.edit()
            .putString("viber_auth_token", token.trim())
            .putString("viber_sender_name", senderName.trim().ifBlank { "FTTH Support" })
            .apply()
        _config.value = loadConfig()
    }

    fun saveMessengerConfig(token: String) {
        prefs.edit()
            .putString("messenger_page_token", token.trim())
            .apply()
        _config.value = loadConfig()
    }

    fun updateLastSyncStatus(status: String) {
        prefs.edit().putString("last_sync_status", status).apply()
        _config.value = loadConfig()
    }
}
