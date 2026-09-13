package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiApiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @Json(name = "generationConfig") val generationConfig: GeminiGenerationConfig? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "role") val role: String? = "user",
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "temperature") val temperature: Float? = 0.7f,
    @Json(name = "topP") val topP: Float? = 0.95f,
    @Json(name = "topK") val topK: Int? = 40,
    @Json(name = "maxOutputTokens") val maxOutputTokens: Int? = 4096
)

@JsonClass(generateAdapter = true)
data class GeminiApiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null,
    @Json(name = "usageMetadata") val usageMetadata: GeminiUsageMetadata? = null,
    @Json(name = "modelVersion") val modelVersion: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent? = null,
    @Json(name = "finishReason") val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiUsageMetadata(
    @Json(name = "promptTokenCount") val promptTokenCount: Int? = null,
    @Json(name = "candidatesTokenCount") val candidatesTokenCount: Int? = null,
    @Json(name = "totalTokenCount") val totalTokenCount: Int? = null
)

interface GeminiApiService {
    @POST
    suspend fun generateContent(
        @Url url: String,
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>,
        @Body request: GeminiApiRequest
    ): GeminiApiResponse
}

object GeminiApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
        redactHeader("x-goog-api-key")
        redactHeader("Authorization")
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Auth keys from AI Studio start with "AQ." and should be sent as
     * `x-goog-api-key` (not only `?key=`). Older AIza keys still accept the query param.
     */
    suspend fun generateContent(
        model: String,
        apiKey: String,
        request: GeminiApiRequest
    ): GeminiApiResponse {
        val strategies = buildList {
            // Official: x-goog-api-key header (works for AQ. auth keys and AIza)
            add(mapOf("x-goog-api-key" to apiKey) to emptyMap<String, String>())
            if (apiKey.startsWith("AQ.")) {
                add(
                    mapOf(
                        "x-goog-api-key" to apiKey,
                        "Authorization" to "Bearer $apiKey"
                    ) to emptyMap()
                )
            } else {
                // Legacy AIza keys often used ?key=
                add(mapOf("x-goog-api-key" to apiKey) to mapOf("key" to apiKey))
                add(emptyMap<String, String>() to mapOf("key" to apiKey))
            }
        }

        var lastError: Exception? = null
        val url = BASE_URL + "v1beta/models/$model:generateContent"
        for ((headers, queries) in strategies) {
            try {
                return service.generateContent(url, headers, queries, request)
            } catch (e: HttpException) {
                val body = try { e.response()?.errorBody()?.string().orEmpty() } catch (_: Exception) { "" }
                lastError = Exception("HTTP ${e.code()}: ${body.take(400)}")
                val retryable = e.code() in listOf(401, 403) ||
                    body.contains("ACCESS_TOKEN_TYPE_UNSUPPORTED", ignoreCase = true) ||
                    body.contains("UNAUTHENTICATED", ignoreCase = true) ||
                    body.contains("API key not valid", ignoreCase = true)
                if (!retryable) throw lastError!!
            }
        }
        throw lastError ?: Exception("Gemini request failed")
    }
}
