package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GitHubUser(
    @Json(name = "login") val login: String,
    @Json(name = "id") val id: Long,
    @Json(name = "avatar_url") val avatarUrl: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "public_repos") val publicRepos: Int = 0,
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "bio") val bio: String? = null,
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubRepoItem(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "private") val isPrivate: Boolean = false,
    @Json(name = "description") val description: String? = null,
    @Json(name = "html_url") val htmlUrl: String,
    @Json(name = "default_branch") val defaultBranch: String = "main",
    @Json(name = "updated_at") val updatedAt: String? = null,
    @Json(name = "stargazers_count") val stargazersCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class GitHubCreateRepoRequest(
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String = "Exported from Google AI Studio Build",
    @Json(name = "private") val isPrivate: Boolean = false,
    @Json(name = "auto_init") val autoInit: Boolean = true
)

@JsonClass(generateAdapter = true)
data class GitHubPushContentRequest(
    @Json(name = "message") val message: String,
    @Json(name = "content") val contentBase64: String,
    @Json(name = "branch") val branch: String = "main",
    @Json(name = "sha") val sha: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubFileContentResponse(
    @Json(name = "sha") val sha: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "path") val path: String? = null,
    @Json(name = "size") val size: Long? = null
)

@JsonClass(generateAdapter = true)
data class GitHubPushResponse(
    @Json(name = "content") val content: GitHubFileContentResponse? = null,
    @Json(name = "commit") val commit: GitHubCommitInfo? = null
)

@JsonClass(generateAdapter = true)
data class GitHubCommitInfo(
    @Json(name = "sha") val sha: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubWorkflowRun(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String? = null,
    @Json(name = "head_branch") val headBranch: String? = null,
    @Json(name = "head_sha") val headSha: String? = null,
    @Json(name = "status") val status: String, // "queued", "in_progress", "completed"
    @Json(name = "conclusion") val conclusion: String? = null, // "success", "failure", "cancelled"
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class GitHubWorkflowRunsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    @Json(name = "workflow_runs") val workflowRuns: List<GitHubWorkflowRun> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubJobStep(
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "conclusion") val conclusion: String? = null,
    @Json(name = "number") val number: Int = 1
)

@JsonClass(generateAdapter = true)
data class GitHubJob(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String,
    @Json(name = "conclusion") val conclusion: String? = null,
    @Json(name = "html_url") val htmlUrl: String? = null,
    @Json(name = "steps") val steps: List<GitHubJobStep>? = null
)

@JsonClass(generateAdapter = true)
data class GitHubJobsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    @Json(name = "jobs") val jobs: List<GitHubJob> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubDispatchWorkflowRequest(
    @Json(name = "ref") val ref: String = "main"
)

interface GitHubApiService {
    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authHeader: String,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubUser

    @GET("user/repos")
    suspend fun listUserRepositories(
        @Header("Authorization") authHeader: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): List<GitHubRepoItem>

    @POST("user/repos")
    suspend fun createRepository(
        @Header("Authorization") authHeader: String,
        @Body request: GitHubCreateRepoRequest,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubRepoItem

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") branch: String = "main"
    ): GitHubFileContentResponse

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun putFileContent(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: GitHubPushContentRequest,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubPushResponse

    @GET("repos/{owner}/{repo}/actions/runs")
    suspend fun getWorkflowRuns(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("per_page") perPage: Int = 5,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubWorkflowRunsResponse

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/jobs")
    suspend fun getWorkflowRunJobs(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubJobsResponse

    @POST("repos/{owner}/{repo}/actions/workflows/{workflow_id}/dispatches")
    suspend fun dispatchWorkflow(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("workflow_id") workflowId: String,
        @Body request: GitHubDispatchWorkflowRequest
    ): retrofit2.Response<Unit>

    /** Returns a redirect to the raw log text for a job */
    @GET("repos/{owner}/{repo}/actions/jobs/{job_id}/logs")
    suspend fun getJobLogs(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("job_id") jobId: Long,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): retrofit2.Response<okhttp3.ResponseBody>

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}")
    suspend fun getWorkflowRun(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubWorkflowRun

    @GET("repos/{owner}/{repo}/actions/runs/{run_id}/artifacts")
    suspend fun listRunArtifacts(
        @Header("Authorization") authHeader: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("run_id") runId: Long,
        @Header("Accept") acceptHeader: String = "application/vnd.github.v3+json"
    ): GitHubArtifactsResponse
}

@JsonClass(generateAdapter = true)
data class GitHubArtifact(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "size_in_bytes") val sizeInBytes: Long = 0,
    @Json(name = "archive_download_url") val archiveDownloadUrl: String? = null,
    @Json(name = "expired") val expired: Boolean = false
)

@JsonClass(generateAdapter = true)
data class GitHubArtifactsResponse(
    @Json(name = "total_count") val totalCount: Int = 0,
    @Json(name = "artifacts") val artifacts: List<GitHubArtifact> = emptyList()
)

object GitHubApiClient {
    private const val BASE_URL = "https://api.github.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    val service: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}
