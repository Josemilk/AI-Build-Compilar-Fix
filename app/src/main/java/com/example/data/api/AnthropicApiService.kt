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
data class AnthropicRequest(
    @Json(name = "model") val model: String,
    @Json(name = "messages") val messages: List<AnthropicMessage>,
    @Json(name = "system") val system: String? = null,
    @Json(name = "max_tokens") val maxTokens: Int = 4096,
    @Json(name = "temperature") val temperature: Float? = 0.7f
)

@JsonClass(generateAdapter = true)
data class AnthropicMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class AnthropicResponse(
    @Json(name = "content") val content: List<AnthropicContent>? = null,
    @Json(name = "usage") val usage: AnthropicUsage? = null
)

@JsonClass(generateAdapter = true)
data class AnthropicContent(
    @Json(name = "text") val text: String? = null,
    @Json(name = "type") val type: String? = null
)

@JsonClass(generateAdapter = true)
data class AnthropicUsage(
    @Json(name = "input_tokens") val inputTokens: Int? = null,
    @Json(name = "output_tokens") val outputTokens: Int? = null
) {
    val totalTokens: Int
        get() = (inputTokens ?: 0) + (outputTokens ?: 0)
}

interface AnthropicApiService {
    @POST("v1/messages")
    suspend fun generateContent(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Header("content-type") contentType: String = "application/json",
        @Body request: AnthropicRequest
    ): AnthropicResponse
}

object AnthropicApiClient {
    private const val BASE_URL = "https://api.anthropic.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: AnthropicApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AnthropicApiService::class.java)
    }
}
