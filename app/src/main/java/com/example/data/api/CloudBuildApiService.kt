package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class CloudBuildRequest(
    @Json(name = "source") val source: BuildSource,
    @Json(name = "steps") val steps: List<BuildStep>
)

@JsonClass(generateAdapter = true)
data class BuildSource(
    @Json(name = "storageSource") val storageSource: StorageSource? = null,
    @Json(name = "repoSource") val repoSource: RepoSource? = null
)

@JsonClass(generateAdapter = true)
data class StorageSource(
    @Json(name = "bucket") val bucket: String,
    @Json(name = "object") val objectName: String
)

@JsonClass(generateAdapter = true)
data class RepoSource(
    @Json(name = "projectId") val projectId: String,
    @Json(name = "repoName") val repoName: String,
    @Json(name = "branchName") val branchName: String
)

@JsonClass(generateAdapter = true)
data class BuildStep(
    @Json(name = "name") val name: String,
    @Json(name = "args") val args: List<String>? = null,
    @Json(name = "env") val env: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CloudBuildResponse(
    @Json(name = "name") val name: String? = null,
    @Json(name = "metadata") val metadata: BuildMetadata? = null,
    @Json(name = "id") val id: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "logUrl") val logUrl: String? = null,
    @Json(name = "results") val results: CloudBuildResults? = null
)

@JsonClass(generateAdapter = true)
data class BuildMetadata(
    @Json(name = "build") val build: BuildDetails? = null
)

@JsonClass(generateAdapter = true)
data class BuildDetails(
    @Json(name = "id") val id: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "logUrl") val logUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class CloudBuildResults(
    @Json(name = "artifactManifest") val artifactManifest: String? = null,
    @Json(name = "images") val images: List<String>? = null
)

interface CloudBuildApiService {
    @POST("v1/projects/{projectId}/builds")
    suspend fun triggerBuild(
        @Path("projectId") projectId: String,
        @Header("Authorization") authorization: String,
        @Body request: CloudBuildRequest
    ): CloudBuildResponse

    @GET("v1/projects/{projectId}/builds/{buildId}")
    suspend fun getBuild(
        @Path("projectId") projectId: String,
        @Path("buildId") buildId: String,
        @Header("Authorization") authorization: String
    ): CloudBuildResponse
}

object CloudBuildApiClient {
    private const val BASE_URL = "https://cloudbuild.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: CloudBuildApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(CloudBuildApiService::class.java)
    }
}
