package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    /**
     * Search for local resources (Vets, Shops, Shelters) using gemini-3.1-flash-lite
     * with Google Search tool enabled for web grounding.
     */
    suspend fun searchNearbyPlaces(
        queryType: String, // "vets", "shops", "shelters"
        locationQuery: String // City, zip, or user coordinates
    ): List<NearbyPlace> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No Gemini API Key provided. Returning no results.")
            return@withContext emptyList()
        }

        val promptText = """
            Find 5 real and active $queryType near "$locationQuery". 
            Respond STRICTLY with a JSON array of objects. Do not include any markdown formatting, backticks, or other text outside the JSON array.
            Each object in the array MUST have the following fields:
            - name: String (The real name of the clinic/shop/shelter)
            - address: String (Complete address)
            - latitude: Double (Estimated or real latitude)
            - longitude: Double (Estimated or real longitude)
            - rating: Double (e.g. 4.7)
            - contact: String (Phone number or website)
            - description: String (A warm 1-sentence summary of what they offer)
        """.trimIndent()

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", promptText)
                        })
                    })
                })
            })
            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("google_search", JSONObject())
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("maxOutputTokens", 1024)
            })
        }

        val request = Request.Builder()
            .url("${BASE_URL}gemini-3.1-flash-lite:generateContent")
            // Key sent via header instead of URL query so it never lands in logs.
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code}")
                    return@withContext emptyList()
                }

                val bodyString = response.body?.string() ?: ""
                if (com.example.BuildConfig.DEBUG) {
                    Log.d(TAG, "Search places raw response: $bodyString")
                }

                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val textResponse = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text") ?: ""

                parsePlacesFromJson(textResponse, queryType)
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error executing nearby search API", e)
            emptyList()
        }
    }

    /**
     * Minimalist Chat with Google Search for Cat Researcher tool.
     * Uses gemini-3.1-flash-lite for speed.
     */
    suspend fun chatWithSearchStream(
        systemInstruction: String,
        history: List<Pair<String, String>>, // List of (Role, Text)
        onChunkReceived: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            onChunkReceived("Error: Gemini API Key missing. Please set it in the Secrets panel.")
            return@withContext
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                history.forEach { (role, text) ->
                    put(JSONObject().apply {
                        put("role", if (role == "user") "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", text) })
                        })
                    })
                }
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstruction) })
                })
            })
            put("tools", JSONArray().apply {
                put(JSONObject().apply {
                    put("google_search", JSONObject())
                })
            })
        }

        val request = Request.Builder()
            .url("${BASE_URL}gemini-3.1-flash-lite:streamGenerateContent?alt=sse")
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    onChunkReceived("Error: ${response.code} ${response.message}")
                    return@withContext
                }

                val source = response.body?.source() ?: return@withContext
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6)
                        try {
                            val jsonChunk = JSONObject(data)
                            val candidates = jsonChunk.optJSONArray("candidates")
                            val textChunk = candidates?.optJSONObject(0)
                                ?.optJSONObject("content")
                                ?.optJSONArray("parts")
                                ?.optJSONObject(0)
                                ?.optString("text") ?: ""

                            if (textChunk.isNotEmpty()) {
                                onChunkReceived(textChunk)
                            }
                        } catch (e: Exception) {
                            // Skip invalid JSON chunks in stream
                        }
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error in chatStream", e)
            onChunkReceived("Error: ${e.localizedMessage}")
        }
    }

    private fun parsePlacesFromJson(jsonText: String, queryType: String): List<NearbyPlace> {
        try {
            // Locate JSON array in case there are wrappers
            val startIdx = jsonText.indexOf("[")
            val endIdx = jsonText.lastIndexOf("]")
            if (startIdx == -1 || endIdx == -1) {
                Log.w(TAG, "Could not find JSON array bounds. Returning no results.")
                return emptyList()
            }
            val cleanedJson = jsonText.substring(startIdx, endIdx + 1)
            val jsonArray = JSONArray(cleanedJson)
            val places = mutableListOf<NearbyPlace>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                places.add(
                    NearbyPlace(
                        name = obj.optString("name", "Unknown Place"),
                        address = obj.optString("address", "Unknown Address"),
                        latitude = obj.optDouble("latitude", 0.0),
                        longitude = obj.optDouble("longitude", 0.0),
                        rating = obj.optDouble("rating", 0.0),
                        contact = obj.optString("contact", "No contact info"),
                        description = obj.optString("description", "")
                    )
                )
            }
            return places
        } catch (e: Exception) {
            if (com.example.BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to parse places JSON", e)
            }
            // Never fabricate businesses when parsing fails - return an honest empty result.
            return emptyList()
        }
    }

    /**
     * Generate an image using text prompts with gemini-3.1-flash-image-preview
     * or gemini-3-pro-image-preview with custom sizes.
     */
    suspend fun generateImage(
        prompt: String,
        isProModel: Boolean,
        imageSize: String // "1K", "2K", "4K"
    ): Bitmap? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No Gemini API Key provided for image generation.")
            return@withContext null
        }

        val modelName = if (isProModel) "gemini-3-pro-image-preview" else "gemini-3.1-flash-image-preview"

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("TEXT")
                    put("IMAGE")
                })
                put("imageConfig", JSONObject().apply {
                    put("aspectRatio", "1:1")
                    put("imageSize", imageSize)
                })
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL$modelName:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Image API call failed with code: ${response.code}")
                    return@withContext null
                }

                val bodyString = response.body?.string() ?: ""
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val mimeType = inlineData.optString("mimeType", "")
                            if (mimeType.startsWith("image/")) {
                                val base64Data = inlineData.optString("data", "")
                                if (base64Data.isNotEmpty()) {
                                    val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    return@withContext BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                }
                            }
                        }
                    }
                }
                null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error executing image generation API", e)
            null
        }
    }

    /**
     * Generate music using Lyria and return the file path of the downloaded audio.
     */
    suspend fun generateMusic(
        context: Context,
        prompt: String,
        useFullTrack: Boolean // true for lyria-3-pro-preview, false for lyria-3-clip-preview
    ): File? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "No Gemini API Key provided for music generation.")
            return@withContext null
        }

        val modelName = if (useFullTrack) "lyria-3-pro-preview" else "lyria-3-clip-preview"

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseModalities", JSONArray().apply {
                    put("AUDIO")
                })
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL$modelName:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Music API call failed with code: ${response.code}")
                    return@withContext null
                }

                val bodyString = response.body?.string() ?: ""
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val parts = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")

                if (parts != null) {
                    for (i in 0 until parts.length()) {
                        val part = parts.getJSONObject(i)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val base64Data = inlineData.optString("data", "")
                            if (base64Data.isNotEmpty()) {
                                val audioBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                val file = File(context.cacheDir, "lyria_temp_song.mp3")
                                FileOutputStream(file).use { fos ->
                                    fos.write(audioBytes)
                                }
                                return@withContext file
                            }
                        }
                    }
                }
                null
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error executing music generation API", e)
            null
        }
    }

    // NOTE: No fabricated place fallback exists anymore. If the API fails or
    // returns unparseable data we return an empty list so the UI can show an
    // honest "no verified results" state instead of invented businesses.
    private fun getFallbackPlaces(@Suppress("UNUSED_PARAMETER") queryType: String): List<NearbyPlace> = emptyList()

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun fetchDailyCatFact(languageCode: String = "en"): String = withContext(Dispatchers.IO) {
        val fallbackFacts = when (languageCode) {
            "ar" -> listOf(
                "تقضي القطط حوالي 70% من حياتها في النوم و15% في تنظيف فرائها! 🐾",
                "تمتلك القطط 5 أصابع في أقدامها الأمامية و4 فقط في الخلفية! 🐾",
                "تستطيع القطط تدوير آذانها 180 درجة لتحديد أدق الأصوات! 🐾",
                "شوارب القطط تساعدها في قياس المسافات ومعرفة ما إذا كان بإمكانها المرور في مكان ضيق! 🐾",
                "حاسة الشم لدى القطط أقوى بنحو 14 مرة من حاسة الشم لدى الإنسان! 🐾"
            )
            "fr" -> listOf(
                "Les chats passent environ 70% de leur vie à dormir et 15% à faire leur toilette ! 🐾",
                "Les chats ont 5 doigts sur leurs pattes avant, mais seulement 4 sur leurs pattes arrière ! 🐾",
                "Les chats peuvent pivoter leurs oreilles à 180 degrés ! 🐾",
                "Les moustaches d'un chat sont aussi larges que son corps pour l'aider à naviguer ! 🐾",
                "Le ronronnement d'un chat vibre à une fréquence qui favorise l'apaisement et la sérénité ! 🐾"
            )
            "es" -> listOf(
                "¡Los gatos pasan aproximadamente el 70% de sus vidas durmiendo y el 15% acicalándose! 🐾",
                "¡Los gatos tienen 5 dedos en sus patas delanteras, pero solo 4 en las traseras! 🐾",
                "¡Los gatos pueden girar sus orejas 180 grados para escuchar todo con gran precisión! 🐾",
                "¡Los bigotes de un gato tienen casi el mismo ancho que su cuerpo para ayudarle a medir espacios! 🐾",
                "¡El ronroneo de un gato transmite calma y reduce los niveles de estrés! 🐾"
            )
            else -> listOf(
                "Cats spend about 70% of their lives sleeping and 15% grooming! 🐾",
                "Cats have five toes on their front paws, but only four on their back paws! 🐾",
                "Cats can rotate their ears 180 degrees to pinpoint sounds with incredible accuracy! 🐾",
                "A cat's whiskers are generally about as wide as their body to help them navigate! 🐾",
                "A cat's purr vibrates at a frequency that promotes calm and wellness! 🐾"
            )
        }

        val defaultFallback = fallbackFacts.random()
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext defaultFallback
        }

        val promptText = when (languageCode) {
            "ar" -> "قدم حقيقة رائعة وممتعة وموجزة عن القطط في جملة أو جملتين باللغة العربية. اجعلها دافئة ومثيرة للاهتمام."
            "fr" -> "Fournissez un fait fascinant, chaleureux et court sur les chats (1 à 2 phrases) en français."
            "es" -> "Proporciona un dato fascinante, cálido y breve sobre los gatos (1 a 2 oraciones) en español."
            else -> "Provide a fascinating, delightful, and warm 1-2 sentence daily cat fact in English. Keep it cozy and interesting."
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", 120)
            })
        }
        val request = Request.Builder()
            .url("${BASE_URL}gemini-3.5-flash:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(requestBodyJson.toString().toRequestBody(mediaTypeJson))
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext defaultFallback
                }
                val bodyString = response.body?.string() ?: ""
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val textResponse = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!textResponse.isNullOrBlank()) {
                    textResponse.trim()
                } else {
                    defaultFallback
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily cat fact", e)
            defaultFallback
        }
    }
}

data class NearbyPlace(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val rating: Double,
    val contact: String,
    val description: String,
    val distance: Double = 0.0
)
