package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GroqClient {

    private suspend inline fun <T> retryOnRateLimit(maxRetries: Int = 3, block: suspend () -> T): T {
        var lastException: Exception? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: java.io.IOException) {
                lastException = e
                val statusCode = extractStatusCode(e)
                if (statusCode == 429 || statusCode == 503) {
                    if (attempt < maxRetries) {
                        val delayMs = 1000L * (1 shl attempt)
                        Log.w(TAG, "Rate limited ($statusCode), retrying in ${delayMs}ms (attempt ${attempt + 1}/$maxRetries)")
                        kotlinx.coroutines.delay(delayMs)
                        return@repeat
                    }
                }
                throw e
            }
        }
        throw lastException ?: java.io.IOException("Max retries exceeded")
    }

    private fun extractStatusCode(e: Exception): Int {
        val message = e.message ?: return 0
        val regex = Regex("""HTTP\s+(\d{3})""")
        return regex.find(message)?.groupValues?.get(1)?.toIntOrNull() ?: 0
    }
    private const val TAG = "GroqClient"
    private const val BASE_URL = "https://api.groq.com/openai/v1/chat/completions"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Chat with Groq Llama 3 model using OpenAI-compatible streaming.
     */
    suspend fun chatStream(
        systemInstruction: String,
        history: List<Pair<String, String>>, // List of (Role, Text)
        onChunkReceived: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GROQ_API_KEY") {
            onChunkReceived("Error: Groq API Key missing. Please set it in the Secrets panel.")
            return@withContext
        }

        val messages = JSONArray()
        // Add system instruction
        messages.put(JSONObject().apply {
            put("role", "system")
            put("content", systemInstruction)
        })
        
        // Add history
        history.forEach { (role, text) ->
            messages.put(JSONObject().apply {
                put("role", if (role == "user") "user" else "assistant")
                put("content", text)
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("model", "qwen/qwen3.8-27b")
            put("messages", messages)
            put("stream", true)
            put("max_tokens", 700)
        }

        val request = Request.Builder()
            .url(BASE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()

        try {
            retryOnRateLimit {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Groq API call failed: ${response.code}")
                        throw RuntimeException("API error ${response.code}: ${response.message}")
                    }

                    val source = response.body?.source() ?: return@withContext
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: break
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6).trim()
                            if (data == "[DONE]") break

                            try {
                                val jsonChunk = JSONObject(data)
                                val choices = jsonChunk.optJSONArray("choices")
                                val delta = choices?.optJSONObject(0)?.optJSONObject("delta")
                                val content = delta?.optString("content") ?: ""

                                if (content.isNotEmpty()) {
                                    onChunkReceived(content)
                                }
                            } catch (e: Exception) {
                                // Skip invalid JSON chunks in stream
                            }
                        }
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in Groq chatStream", e)
            throw e
        }
    }
}
