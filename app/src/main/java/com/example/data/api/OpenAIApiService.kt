package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenAiRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<OpenAiMessage>,
    @Json(name = "temperature") val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiResponse(
    @Json(name = "choices") val choices: List<OpenAiChoice>? = null,
    @Json(name = "usage") val usage: OpenAiUsage? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    @Json(name = "message") val message: OpenAiMessage? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiUsage(
    @Json(name = "total_tokens") val totalTokens: Int? = null
)

interface OpenAIApiService {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiRequest
    ): OpenAiResponse
}

object OpenAIApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Default OpenAI client */
    val service: OpenAIApiService by lazy {
        createService("https://api.openai.com/")
    }

    /**
     * Creates a Retrofit service for any OpenAI-compatible API
     * (OpenAI, DeepSeek, Groq, custom endpoints, etc.)
     */
    fun createService(baseUrl: String): OpenAIApiService {
        val normalized = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalized)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(OpenAIApiService::class.java)
    }
}
