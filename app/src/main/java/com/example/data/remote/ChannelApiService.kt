package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TelegramUpdate(
    val updateId: Long,
    val chatId: Long,
    val senderName: String,
    val username: String?,
    val text: String,
    val date: Long
)

data class BotTestResult(
    val isSuccess: Boolean,
    val botName: String = "",
    val errorMessage: String = ""
)

object ChannelApiService {
    private const val TAG = "ChannelApiService"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // ==========================================
    // 1. TELEGRAM BOT API
    // ==========================================

    /**
     * Test Telegram Bot Token using getMe
     */
    suspend fun testTelegramBot(botToken: String): BotTestResult = withContext(Dispatchers.IO) {
        if (botToken.isBlank()) {
            return@withContext BotTestResult(false, errorMessage = "Bot Token ထည့်သွင်းထားခြင်း မရှိပါ။")
        }
        val url = "https://api.telegram.org/bot${botToken.trim()}/getMe"
        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    if (json.optBoolean("ok", false)) {
                        val result = json.getJSONObject("result")
                        val firstName = result.optString("first_name", "Telegram Bot")
                        val username = result.optString("username", "")
                        return@withContext BotTestResult(
                            isSuccess = true,
                            botName = "$firstName (@$username)"
                        )
                    }
                }
                return@withContext BotTestResult(false, errorMessage = "Token မမှန်ကန်ပါ သို့မဟုတ် Error ဖြစ်နေပါသည်။ ($response)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "testTelegramBot failed", e)
            return@withContext BotTestResult(false, errorMessage = e.localizedMessage ?: "Network connection failed")
        }
    }

    /**
     * Send real message to Telegram user/chat
     */
    suspend fun sendTelegramMessage(
        botToken: String,
        chatId: String,
        text: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (botToken.isBlank() || chatId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Bot Token သို့မဟုတ် Chat ID လိုအပ်ပါသည်။"))
        }
        val url = "https://api.telegram.org/bot${botToken.trim()}/sendMessage"
        try {
            val payload = JSONObject().apply {
                put("chat_id", chatId.trim())
                put("text", text)
            }
            val requestBody = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    if (json.optBoolean("ok", false)) {
                        return@withContext Result.success(true)
                    }
                }
                return@withContext Result.failure(Exception("Telegram API Error: $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendTelegramMessage failed", e)
            return@withContext Result.failure(e)
        }
    }

    /**
     * Fetch updates from Telegram using getUpdates (Long Polling)
     */
    suspend fun getTelegramUpdates(
        botToken: String,
        offset: Long? = null
    ): List<TelegramUpdate> = withContext(Dispatchers.IO) {
        if (botToken.isBlank()) return@withContext emptyList()

        var url = "https://api.telegram.org/bot${botToken.trim()}/getUpdates?timeout=5"
        if (offset != null) {
            url += "&offset=$offset"
        }

        try {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) return@withContext emptyList()

                val json = JSONObject(body)
                if (!json.optBoolean("ok", false)) return@withContext emptyList()

                val resultsArray = json.optJSONArray("result") ?: return@withContext emptyList()
                val updates = mutableListOf<TelegramUpdate>()

                for (i in 0 until resultsArray.length()) {
                    val item = resultsArray.getJSONObject(i)
                    val updateId = item.optLong("update_id")
                    val message = item.optJSONObject("message") ?: item.optJSONObject("channel_post")
                    if (message != null) {
                        val text = message.optString("text", "").trim()
                        val chat = message.optJSONObject("chat")
                        val from = message.optJSONObject("from")
                        val chatId = chat?.optLong("id") ?: from?.optLong("id") ?: 0L
                        val firstName = from?.optString("first_name", "") ?: chat?.optString("first_name", "Customer")
                        val lastName = from?.optString("last_name", "") ?: ""
                        val username = from?.optString("username", null)
                        val date = message.optLong("date", System.currentTimeMillis() / 1000) * 1000

                        if (chatId != 0L && text.isNotBlank()) {
                            updates.add(
                                TelegramUpdate(
                                    updateId = updateId,
                                    chatId = chatId,
                                    senderName = "$firstName $lastName".trim(),
                                    username = username,
                                    text = text,
                                    date = date
                                )
                            )
                        }
                    }
                }
                return@withContext updates
            }
        } catch (e: Exception) {
            Log.e(TAG, "getTelegramUpdates error", e)
            return@withContext emptyList()
        }
    }

    // ==========================================
    // 2. VIBER BOT API
    // ==========================================

    /**
     * Send real message via Viber Bot API
     * https://chatapi.viber.com/pa/send_message
     */
    suspend fun sendViberMessage(
        viberAuthToken: String,
        receiverViberId: String,
        senderName: String,
        text: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (viberAuthToken.isBlank() || receiverViberId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Viber Auth Token သို့မဟုတ် Receiver ID မရှိပါ။"))
        }
        val url = "https://chatapi.viber.com/pa/send_message"
        try {
            val payload = JSONObject().apply {
                put("receiver", receiverViberId.trim())
                put("min_api_version", 1)
                put("sender", JSONObject().apply {
                    put("name", senderName.ifBlank { "FTTH Support" })
                })
                put("type", "text")
                put("text", text)
            }

            val request = Request.Builder()
                .url(url)
                .header("X-Viber-Auth-Token", viberAuthToken.trim())
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    if (json.optInt("status", -1) == 0) {
                        return@withContext Result.success(true)
                    }
                }
                return@withContext Result.failure(Exception("Viber API Error: $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendViberMessage error", e)
            return@withContext Result.failure(e)
        }
    }

    // ==========================================
    // 3. META MESSENGER GRAPH API
    // ==========================================

    /**
     * Send real message via Facebook Messenger Send API
     * https://graph.facebook.com/v19.0/me/messages
     */
    suspend fun sendMessengerMessage(
        pageAccessToken: String,
        recipientPsid: String,
        text: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (pageAccessToken.isBlank() || recipientPsid.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Messenger Page Access Token သို့မဟုတ် Recipient ID လိုအပ်ပါသည်။"))
        }
        val url = "https://graph.facebook.com/v19.0/me/messages?access_token=${pageAccessToken.trim()}"
        try {
            val payload = JSONObject().apply {
                put("recipient", JSONObject().apply {
                    put("id", recipientPsid.trim())
                })
                put("message", JSONObject().apply {
                    put("text", text)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(payload.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    return@withContext Result.success(true)
                }
                return@withContext Result.failure(Exception("Messenger API Error: $body"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendMessengerMessage error", e)
            return@withContext Result.failure(e)
        }
    }
}
