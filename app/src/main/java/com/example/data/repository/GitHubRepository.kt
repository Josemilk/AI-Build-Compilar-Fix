package com.example.data.repository

import android.util.Base64
import android.util.Log
import com.example.data.api.GitHubApiClient
import com.example.data.api.GitHubCreateRepoRequest
import com.example.data.api.GitHubPushContentRequest
import com.example.data.api.GitHubPushResponse
import com.example.data.api.GitHubRepoItem
import com.example.data.api.GitHubUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class GitHubAuthResult(
    val user: GitHubUser,
    val repositories: List<GitHubRepoItem>
)

data class GitHubPushResult(
    val success: Boolean,
    val commitSha: String?,
    val commitUrl: String?,
    val repoHtmlUrl: String,
    val message: String
)

class GitHubRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val TAG = "GitHubRepository"

    private fun formatAuthHeader(token: String): String {
        val trimmed = token.trim()
        return if (trimmed.startsWith("token ", ignoreCase = true) || trimmed.startsWith("Bearer ", ignoreCase = true)) {
            trimmed
        } else {
            "Bearer $trimmed"
        }
    }

    suspend fun authenticateAndFetchUser(token: String): Result<GitHubAuthResult> = withContext(Dispatchers.IO) {
        if (token.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("GitHub token cannot be empty."))
        }

        try {
            val authHeader = formatAuthHeader(token)
            val user = GitHubApiClient.service.getAuthenticatedUser(authHeader)
            val repos = GitHubApiClient.service.listUserRepositories(authHeader)

            // Save GitHub integration metadata to real Firebase Firestore if authenticated
            auth.currentUser?.let { fbUser ->
                try {
                    val githubDoc = mapOf(
                        "username" to user.login,
                        "name" to (user.name ?: user.login),
                        "avatarUrl" to (user.avatarUrl ?: ""),
                        "publicReposCount" to user.publicRepos,
                        "htmlUrl" to (user.htmlUrl ?: ""),
                        "lastSyncTimestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("users")
                        .document(fbUser.uid)
                        .collection("integrations")
                        .document("github")
                        .set(githubDoc, SetOptions.merge())
                        .await()
                    Log.d(TAG, "GitHub integration saved to Firestore for user: ${fbUser.uid}")
                } catch (fe: Exception) {
                    Log.w(TAG, "Could not sync GitHub metadata to Firestore: ${fe.message}")
                }
            }

            Result.success(GitHubAuthResult(user = user, repositories = repos))
        } catch (e: Exception) {
            Log.e(TAG, "GitHub authentication failed", e)
            Result.failure(e)
        }
    }

    suspend fun fetchRepositories(token: String): Result<List<GitHubRepoItem>> = withContext(Dispatchers.IO) {
        try {
            val authHeader = formatAuthHeader(token)
            val repos = GitHubApiClient.service.listUserRepositories(authHeader)
            Result.success(repos)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching repositories", e)
            Result.failure(e)
        }
    }

    suspend fun createRepository(
        token: String,
        repoName: String,
        description: String = "Exported from Google AI Studio Build",
        isPrivate: Boolean = false
    ): Result<GitHubRepoItem> = withContext(Dispatchers.IO) {
        try {
            val authHeader = formatAuthHeader(token)
            val request = GitHubCreateRepoRequest(
                name = repoName,
                description = description,
                isPrivate = isPrivate,
                autoInit = true
            )
            val created = GitHubApiClient.service.createRepository(authHeader, request)
            Result.success(created)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating repository $repoName", e)
            Result.failure(e)
        }
    }

    suspend fun pushProjectZipArchive(
        token: String,
        owner: String,
        repoName: String,
        branch: String = "main",
        commitMessage: String,
        archiveBase64Content: String,
        targetFilePath: String = "project_export.zip"
    ): Result<GitHubPushResult> = withContext(Dispatchers.IO) {
        try {
            val authHeader = formatAuthHeader(token)

            // Check if file already exists in repository to get its SHA for update
            val existingSha = try {
                val existing = GitHubApiClient.service.getFileContent(
                    authHeader = authHeader,
                    owner = owner,
                    repo = repoName,
                    path = targetFilePath,
                    branch = branch
                )
                existing.sha
            } catch (e: Exception) {
                null
            }

            val request = GitHubPushContentRequest(
                message = commitMessage.ifBlank { "Export project zip archive from Google AI Studio Build" },
                contentBase64 = archiveBase64Content,
                branch = branch,
                sha = existingSha
            )

            val pushResponse: GitHubPushResponse = GitHubApiClient.service.putFileContent(
                authHeader = authHeader,
                owner = owner,
                repo = repoName,
                path = targetFilePath,
                request = request
            )

            val repoUrl = "https://github.com/$owner/$repoName"
            val commitSha = pushResponse.commit?.sha ?: "head"
            val commitUrl = pushResponse.commit?.htmlUrl ?: "$repoUrl/commit/$commitSha"

            // Log real push history to Firestore
            auth.currentUser?.let { fbUser ->
                try {
                    val logEntry = mapOf(
                        "repo" to "$owner/$repoName",
                        "branch" to branch,
                        "commitMessage" to commitMessage,
                        "commitSha" to commitSha,
                        "commitUrl" to commitUrl,
                        "timestamp" to System.currentTimeMillis()
                    )
                    firestore.collection("users")
                        .document(fbUser.uid)
                        .collection("github_commits")
                        .add(logEntry)
                        .await()
                } catch (fe: Exception) {
                    Log.w(TAG, "Could not log commit to Firestore: ${fe.message}")
                }
            }

            Result.success(
                GitHubPushResult(
                    success = true,
                    commitSha = commitSha,
                    commitUrl = commitUrl,
                    repoHtmlUrl = repoUrl,
                    message = "Project successfully pushed to GitHub repository $owner/$repoName!"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push project zip to GitHub", e)
            Result.failure(e)
        }
    }

    suspend fun fetchLatestWorkflowRun(
        token: String,
        owner: String,
        repo: String
    ): Result<com.example.data.api.GitHubWorkflowRun?> = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "token $token"
            val response = GitHubApiClient.service.getWorkflowRuns(authHeader, owner, repo, perPage = 3)
            Result.success(response.workflowRuns.firstOrNull())
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workflow runs", e)
            Result.failure(e)
        }
    }

    suspend fun getWorkflowRunJobs(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<List<com.example.data.api.GitHubJob>> = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "token $token"
            val response = GitHubApiClient.service.getWorkflowRunJobs(authHeader, owner, repo, runId)
            Result.success(response.jobs)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching workflow run jobs", e)
            Result.failure(e)
        }
    }

    suspend fun pushFileContent(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        commitMessage: String,
        sha: String? = null
    ): Result<com.example.data.api.GitHubPushResponse> = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "token $token"
            val base64Content = android.util.Base64.encodeToString(
                content.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )

            val request = com.example.data.api.GitHubPushContentRequest(
                message = commitMessage,
                contentBase64 = base64Content,
                branch = "main",
                sha = sha
            )

            val resp = GitHubApiClient.service.putFileContent(authHeader, owner, repo, path, request)
            Result.success(resp)
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing file $path to GitHub", e)
            Result.failure(e)
        }
    }

    suspend fun checkFileExists(
        token: String,
        owner: String,
        repo: String,
        path: String
    ): com.example.data.api.GitHubFileContentResponse? = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "token $token"
            GitHubApiClient.service.getFileContent(authHeader, owner, repo, path)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun dispatchWorkflow(
        token: String,
        owner: String,
        repo: String,
        workflowId: String = "android.yml",
        ref: String = "main"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authHeader = if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "token $token"
            val resp = GitHubApiClient.service.dispatchWorkflow(
                authHeader,
                owner,
                repo,
                workflowId,
                com.example.data.api.GitHubDispatchWorkflowRequest(ref = ref)
            )
            if (resp.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("HTTP ${resp.code()}: ${resp.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dispatching workflow $workflowId", e)
            Result.failure(e)
        }
    }

    /**
     * Pushes the project source tree file-by-file via the Contents API.
     * Skips binaries and very large files. Returns list of pushed paths.
     */
    suspend fun pushProjectFiles(
        token: String,
        owner: String,
        repo: String,
        branch: String,
        files: List<com.example.data.model.ProjectFile>,
        commitMessagePrefix: String = "chore: sync project from AI Studio"
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        val authHeader = formatAuthHeader(token)
        val pushed = mutableListOf<String>()
        val errors = mutableListOf<String>()

        // Prefer text source files; skip obvious non-source
        val candidates = files.filter { f ->
            val ext = f.extension.lowercase()
            ext in setOf(
                "kt", "kts", "java", "xml", "gradle", "properties", "pro", "md",
                "txt", "json", "yml", "yaml", "toml", "gitignore"
            ) || f.name == "gradlew" || f.path.contains("gradle/wrapper")
        }.take(120) // stay under rate limits for a single session

        for (file in candidates) {
            try {
                val existingSha = try {
                    GitHubApiClient.service.getFileContent(
                        authHeader, owner, repo, file.path, branch
                    ).sha
                } catch (_: Exception) {
                    null
                }
                val base64 = android.util.Base64.encodeToString(
                    file.content.toByteArray(Charsets.UTF_8),
                    android.util.Base64.NO_WRAP
                )
                val request = com.example.data.api.GitHubPushContentRequest(
                    message = "$commitMessagePrefix: ${file.path}",
                    contentBase64 = base64,
                    branch = branch,
                    sha = existingSha
                )
                GitHubApiClient.service.putFileContent(
                    authHeader, owner, repo, file.path, request
                )
                pushed.add(file.path)
                // Small delay to reduce secondary rate-limit hits
                kotlinx.coroutines.delay(200)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to push ${file.path}: ${e.message}")
                errors.add("${file.path}: ${e.message}")
            }
        }

        if (pushed.isEmpty() && errors.isNotEmpty()) {
            Result.failure(Exception("No se pudo subir ningún archivo. Ej: ${errors.first()}"))
        } else {
            Result.success(pushed)
        }
    }

    /**
     * Ensures a basic Android CI workflow exists in the repo.
     */
    suspend fun ensureAndroidWorkflow(
        token: String,
        owner: String,
        repo: String,
        branch: String = "main"
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val workflowPath = ".github/workflows/android.yml"
        val yaml = """
            name: Android CI & Build APK
            on:
              push:
                branches: [ "main", "master" ]
              workflow_dispatch:
            jobs:
              build:
                name: Build Android APK
                runs-on: ubuntu-latest
                steps:
                  - uses: actions/checkout@v4
                  - uses: actions/setup-java@v4
                    with:
                      java-version: '17'
                      distribution: 'temurin'
                      cache: 'gradle'
                  - name: Setup Gradle
                    uses: gradle/actions/setup-gradle@v3
                  - name: Grant execute permission for gradlew
                    run: chmod +x gradlew || true
                  - name: Build Debug APK
                    run: |
                      if [ -f gradlew ]; then
                        ./gradlew assembleDebug --stacktrace
                      else
                        gradle assembleDebug --stacktrace
                      fi
                  - name: Upload APK artifact
                    uses: actions/upload-artifact@v4
                    with:
                      name: app-debug-apk
                      path: |
                        **/build/outputs/apk/**/*.apk
                      if-no-files-found: warn
        """.trimIndent()

        return@withContext pushFileContent(
            token = token,
            owner = owner,
            repo = repo,
            path = workflowPath,
            content = yaml,
            commitMessage = "ci: ensure Android build workflow",
            sha = checkFileExists(token, owner, repo, workflowPath)?.sha
        ).map { }
    }

    /**
     * Polls until the latest workflow run completes or timeout.
     */
    suspend fun waitForWorkflowCompletion(
        token: String,
        owner: String,
        repo: String,
        afterTimestampMs: Long,
        timeoutMs: Long = 15 * 60 * 1000L,
        pollIntervalMs: Long = 8_000L,
        onProgress: (suspend (com.example.data.api.GitHubWorkflowRun) -> Unit)? = null
    ): Result<com.example.data.api.GitHubWorkflowRun> = withContext(Dispatchers.IO) {
        val deadline = System.currentTimeMillis() + timeoutMs
        var lastRun: com.example.data.api.GitHubWorkflowRun? = null

        while (System.currentTimeMillis() < deadline) {
            val runs = fetchLatestWorkflowRun(token, owner, repo).getOrNull()
            if (runs != null) {
                lastRun = runs
                onProgress?.invoke(runs)
                if (runs.status == "completed") {
                    return@withContext Result.success(runs)
                }
            }
            kotlinx.coroutines.delay(pollIntervalMs)
        }
        if (lastRun != null) Result.success(lastRun!!)
        else Result.failure(Exception("Timeout esperando workflow de GitHub Actions"))
    }

    /**
     * Downloads log text from the first failed job of a run (truncated for LLM context).
     */
    suspend fun fetchFailedJobLogs(
        token: String,
        owner: String,
        repo: String,
        runId: Long,
        maxChars: Int = 12_000
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authHeader = formatAuthHeader(token)
            val jobs = GitHubApiClient.service.getWorkflowRunJobs(authHeader, owner, repo, runId).jobs
            val failed = jobs.firstOrNull { it.conclusion == "failure" } ?: jobs.firstOrNull()
            if (failed == null) {
                return@withContext Result.failure(Exception("No hay jobs en el run $runId"))
            }

            val logResp = GitHubApiClient.service.getJobLogs(authHeader, owner, repo, failed.id)
            val body = logResp.body()?.string()
            if (body.isNullOrBlank()) {
                // Summarize steps if raw logs unavailable
                val stepSummary = failed.steps?.joinToString("\n") {
                    "- ${it.name}: status=${it.status}, conclusion=${it.conclusion}"
                } ?: "Sin detalle de steps"
                Result.success("Job '${failed.name}' conclusion=${failed.conclusion}\n$stepSummary")
            } else {
                val trimmed = if (body.length > maxChars) {
                    // Keep tail (errors usually at the end)
                    body.takeLast(maxChars)
                } else body
                Result.success(trimmed)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching job logs", e)
            Result.failure(e)
        }
    }

    /**
     * Lists artifacts for a completed workflow run and returns APK-related download URL + size.
     */
    suspend fun findApkArtifact(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<Pair<String, Long>> = withContext(Dispatchers.IO) {
        try {
            val authHeader = formatAuthHeader(token)
            val resp = GitHubApiClient.service.listRunArtifacts(authHeader, owner, repo, runId)
            val apkArtifact = resp.artifacts.firstOrNull {
                !it.expired && (
                    it.name.contains("apk", ignoreCase = true) ||
                        it.name.contains("app", ignoreCase = true)
                    )
            } ?: resp.artifacts.firstOrNull { !it.expired }

            if (apkArtifact?.archiveDownloadUrl != null) {
                Result.success(apkArtifact.archiveDownloadUrl!! to apkArtifact.sizeInBytes)
            } else {
                Result.failure(Exception("No hay artifacts disponibles en el run $runId"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listing artifacts", e)
            Result.failure(e)
        }
    }
}

