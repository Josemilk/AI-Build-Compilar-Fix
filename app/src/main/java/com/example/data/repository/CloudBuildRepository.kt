package com.example.data.repository

import com.example.data.api.BuildSource
import com.example.data.api.BuildStep
import com.example.data.api.CloudBuildApiClient
import com.example.data.api.CloudBuildRequest
import com.example.data.api.CloudBuildResponse
import com.example.data.api.RepoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CloudBuildRepository {

    suspend fun triggerApkBuild(
        projectId: String,
        oauthToken: String,
        repoName: String,
        branchName: String = "main"
    ): Result<CloudBuildResponse> = withContext(Dispatchers.IO) {
        try {
            val gradleStep = BuildStep(
                name = "gcr.io/cloud-builders/gradle",
                args = listOf("assembleDebug")
            )
            val request = CloudBuildRequest(
                source = BuildSource(
                    repoSource = RepoSource(
                        projectId = projectId,
                        repoName = repoName,
                        branchName = branchName
                    )
                ),
                steps = listOf(gradleStep)
            )
            val response = CloudBuildApiClient.service.triggerBuild(
                projectId = projectId,
                authorization = "Bearer $oauthToken",
                request = request
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun waitForBuild(
        projectId: String,
        oauthToken: String,
        buildId: String,
        timeoutMs: Long = 12 * 60 * 1000L,
        pollIntervalMs: Long = 10_000L,
        onProgress: (suspend (CloudBuildResponse) -> Unit)? = null
    ): Result<CloudBuildResponse> = withContext(Dispatchers.IO) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var last: CloudBuildResponse? = null
        while (System.currentTimeMillis() < deadline) {
            try {
                val build = CloudBuildApiClient.service.getBuild(
                    projectId = projectId,
                    buildId = buildId,
                    authorization = "Bearer $oauthToken"
                )
                last = build
                onProgress?.invoke(build)
                val status = (build.status ?: "").uppercase()
                if (status in listOf("SUCCESS", "FAILURE", "TIMEOUT", "CANCELLED", "EXPIRED")) {
                    return@withContext Result.success(build)
                }
            } catch (_: Exception) {
            }
            delay(pollIntervalMs)
        }
        if (last != null) Result.success(last!!)
        else Result.failure(Exception("Timeout esperando Google Cloud Build"))
    }
}
