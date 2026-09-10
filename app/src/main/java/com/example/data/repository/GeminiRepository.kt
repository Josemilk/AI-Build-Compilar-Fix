package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.api.GeminiApiRequest
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiPart
import com.example.data.model.CodeSnippet
import com.example.data.model.ToolAction
import com.example.data.model.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class AgentExecutionResult(
    val replyText: String,
    val thinkingText: String?,
    val toolActions: List<ToolAction>,
    val codeSnippets: List<CodeSnippet>,
    val tokensUsed: Int = 0,
    val modelUsed: String
)

class GeminiRepository {

    suspend fun executeAgentPrompt(
        prompt: String,
        activeModelId: String,
        provider: String = "google_gemini",
        customApiKey: String? = null,
        openaiKey: String? = null,
        anthropicKey: String? = null,
        deepseekKey: String? = null,
        customEndpoint: String? = null,
        attachedFilesSummary: String = ""
    ): Result<AgentExecutionResult> = withContext(Dispatchers.IO) {
        val systemPrompt = """
You are the AI Studio Principal Architect & Android Engineer.
Your goal is to understand the user's intent to build/modify Android apps in Kotlin and Jetpack Compose.
Structure your answer clearly:
1. Provide a concise explanation of what was built or changed.
2. If code is generated, wrap it in markdown code blocks with the file name in a comment or header.
Keep it production-grade, modern Material 3, clean and reactive.
        """.trimIndent()

        val userInstruction = buildString {
            append(prompt)
            if (attachedFilesSummary.isNotBlank()) {
                append("\n\n[Attached Context Files]:\n")
                append(attachedFilesSummary)
            }
        }

        if (provider != "google_gemini") {
            val targetKey = when (provider) {
                "openai" -> openaiKey
                "anthropic" -> anthropicKey
                "deepseek" -> deepseekKey
                else -> customApiKey
            }
            Log.d("GeminiRepository", "Routing prompt dynamically to External LLM Provider: $provider (model: $activeModelId)")

            if (targetKey.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Missing API key for provider: $provider"))
            }

            try {
                var rawText = ""
                var tokens = 350
                var providerTag = ""

                when (provider) {
                    "openai", "deepseek", "custom" -> {
                        val baseUrl = when (provider) {
                            "deepseek" -> "https://api.deepseek.com/"
                            "custom" -> (customEndpoint?.takeIf { it.isNotBlank() } ?: "https://api.openai.com/")
                            else -> "https://api.openai.com/"
                        }
                        providerTag = when (provider) {
                            "openai" -> "OpenAI API"
                            "deepseek" -> "DeepSeek API"
                            else -> "Custom LLM ($baseUrl)"
                        }
                        val openAiReq = com.example.data.api.OpenAiRequest(
                            model = activeModelId,
                            messages = listOf(
                                com.example.data.api.OpenAiMessage(role = "system", content = systemPrompt),
                                com.example.data.api.OpenAiMessage(role = "user", content = userInstruction)
                            )
                        )
                        val client = com.example.data.api.OpenAIApiClient.createService(baseUrl)
                        val response = client.generateContent(
                            authorization = "Bearer $targetKey",
                            request = openAiReq
                        )
                        rawText = response.choices?.firstOrNull()?.message?.content ?: "Success"
                        tokens = response.usage?.totalTokens ?: 350
                    }
                    "anthropic" -> {
                        providerTag = "Anthropic API"
                        val anthropicReq = com.example.data.api.AnthropicRequest(
                            model = activeModelId,
                            system = systemPrompt,
                            messages = listOf(
                                com.example.data.api.AnthropicMessage(role = "user", content = userInstruction)
                            )
                        )
                        val response = com.example.data.api.AnthropicApiClient.service.generateContent(
                            apiKey = targetKey,
                            request = anthropicReq
                        )
                        rawText = response.content?.firstOrNull()?.text ?: "Success"
                        tokens = response.usage?.totalTokens ?: 350
                    }
                    else -> {
                        return@withContext Result.failure(Exception("Unsupported provider: $provider"))
                    }
                }

                val parsedCodeSnippets = extractCodeSnippets(rawText)
                val thinkingText = extractThinking(rawText)
                val cleanReply = cleanResponseText(rawText)

                // Only report real actions that actually happened (API call + parsing)
                val tools = listOf(
                    ToolAction("Called $providerTag", ToolStatus.COMPLETED),
                    ToolAction("Parsed response and extracted code snippets", ToolStatus.COMPLETED)
                )

                return@withContext Result.success(
                    AgentExecutionResult(
                        replyText = cleanReply,
                        thinkingText = thinkingText ?: "Response received from $providerTag.",
                        toolActions = tools,
                        codeSnippets = parsedCodeSnippets,
                        tokensUsed = tokens,
                        modelUsed = "$providerTag ($activeModelId)"
                    )
                )
            } catch (e: Exception) {
                Log.e("GeminiRepository", "External API call failed", e)
                return@withContext Result.failure(Exception("Error en $provider: ${e.message}"))
            }
        }

        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Missing Gemini API Key"))
        }

        try {
            val request = GeminiApiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userInstruction))
                    )
                ),
                systemInstruction = GeminiContent(
                    role = null,
                    parts = listOf(GeminiPart(text = systemPrompt))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 4096
                )
            )

            val modelCandidates = resolveGeminiModelIds(activeModelId)
            var response: com.example.data.api.GeminiApiResponse? = null
            var usedModel = modelCandidates.first()
            var lastError: Exception? = null
            for (modelId in modelCandidates) {
                try {
                    response = GeminiApiClient.service.generateContent(
                        model = modelId,
                        apiKey = apiKey,
                        request = request
                    )
                    usedModel = modelId
                    break
                } catch (e: Exception) {
                    val detail = httpErrorMessage(e)
                    lastError = Exception(detail)
                    val msg = detail
                    // Retry only on 404 / model-not-found
                    if (msg.contains("404") || msg.contains("NOT_FOUND", ignoreCase = true) ||
                        msg.contains("is not found", ignoreCase = true) ||
                        msg.contains("not supported", ignoreCase = true)
                    ) {
                        Log.w("GeminiRepository", "Model $modelId not found ($detail), trying next…")
                        continue
                    }
                    throw Exception(detail)
                }
            }
            if (response == null) {
                val hint = when {
                    apiKey.startsWith("AQ.") ->
                        " La clave empieza por AQ. — asegúrate de que sea una API key de Google AI Studio (suele empezar por AIza…)."
                    else -> ""
                }
                return@withContext Result.failure(
                    Exception(
                        "Modelo no encontrado (404) para '$activeModelId'. " +
                        "Probados: ${modelCandidates.joinToString()}. " +
                        "Revisa la clave en Ajustes y el nombre del modelo.$hint " +
                        "Detalle: ${lastError?.message}"
                    )
                )
            }

            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Generated Android implementation successfully."

            val parsedCodeSnippets = extractCodeSnippets(rawText)
            val thinkingText = extractThinking(rawText)
            val cleanReply = cleanResponseText(rawText)
            val tokens = response.usageMetadata?.totalTokenCount ?: 350

            // Only report real actions that actually happened
            val tools = listOf(
                ToolAction("Called Google Gemini API", ToolStatus.COMPLETED),
                ToolAction("Parsed response and extracted code snippets", ToolStatus.COMPLETED)
            )

            Result.success(
                AgentExecutionResult(
                    replyText = cleanReply,
                    thinkingText = thinkingText ?: "Response received from Google Gemini.",
                    toolActions = tools,
                    codeSnippets = parsedCodeSnippets,
                    tokensUsed = tokens,
                    modelUsed = "Google Gemini ($usedModel)"
                )
            )
        } catch (e: Exception) {
            Log.e("GeminiRepository", "API call failed", e)
            Result.failure(Exception("Error en Gemini API: ${e.message}"))
        }
    }


    /**
     * Builds an ordered list of Gemini model IDs to try.
     * Google AI returns 404 when the model id is wrong or unavailable for the key.
     */
    private fun resolveGeminiModelIds(requested: String): List<String> {
        val normalized = requested.trim().ifBlank { "gemini-2.0-flash" }
        val aliases = mapOf(
            "gemini-2.5-flash-thinking" to listOf(
                "gemini-2.0-flash-thinking-exp",
                "gemini-2.0-flash-thinking-exp-01-21",
                "gemini-2.0-flash",
                "gemini-1.5-flash"
            ),
            "gemini-2.5-flash" to listOf(
                "gemini-2.5-flash",
                "gemini-2.0-flash",
                "gemini-2.0-flash-001",
                "gemini-1.5-flash",
                "gemini-1.5-flash-latest"
            ),
            "gemini-2.0-flash" to listOf(
                "gemini-2.0-flash",
                "gemini-2.0-flash-001",
                "gemini-1.5-flash",
                "gemini-1.5-flash-latest"
            ),
            "gemini-1.5-pro" to listOf(
                "gemini-1.5-pro",
                "gemini-1.5-pro-latest",
                "gemini-1.5-pro-002",
                "gemini-1.5-flash"
            ),
            "gemini-1.5-flash" to listOf(
                "gemini-1.5-flash",
                "gemini-1.5-flash-latest",
                "gemini-1.5-flash-002",
                "gemini-2.0-flash"
            )
        )
        val primary = aliases[normalized] ?: listOf(normalized, "gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro")
        // Always end with widely available fallbacks
        val fallbacks = listOf("gemini-2.0-flash", "gemini-1.5-flash", "gemini-1.5-pro", "gemini-pro")
        return (primary + fallbacks).distinct()
    }

    private fun httpErrorMessage(e: Exception): String {
        if (e is HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            return "HTTP ${e.code()}: ${body?.take(500) ?: e.message()}"
        }
        return e.message ?: e.toString()
    }

    private fun extractCodeSnippets(text: String): List<CodeSnippet> {
        val snippets = mutableListOf<CodeSnippet>()
        // Supports ```kotlin path/File.kt  or  ```path/File.kt  or plain ```kotlin
        val regex = Regex("```(?:([a-zA-Z0-9_./\\-]+))?\\s*\\n([\\s\\S]*?)```", RegexOption.MULTILINE)
        val matches = regex.findAll(text)
        var index = 1
        for (match in matches) {
            val langOrPath = match.groupValues[1].trim()
            val code = match.groupValues[2].trim()
            if (code.isBlank()) continue
            val filename = when {
                langOrPath.contains('/') || langOrPath.contains('.') -> langOrPath.substringAfterLast('/')
                langOrPath in listOf("kotlin", "kt", "java", "xml", "gradle", "kts", "yaml", "yml", "json", "properties") ->
                    "GeneratedCode_$index.${if (langOrPath == "kotlin") "kt" else langOrPath}"
                langOrPath.isNotBlank() -> langOrPath
                else -> "GeneratedCode_$index.kt"
            }
            val language = when {
                filename.endsWith(".kt") || filename.endsWith(".kts") -> "kotlin"
                filename.endsWith(".java") -> "java"
                filename.endsWith(".xml") -> "xml"
                else -> "text"
            }
            snippets.add(CodeSnippet(filename, language, code))
            index++
        }
        return snippets
    }

    /**
     * Asks the model to fix Android build failures given logs + key project files.
     * Expects code blocks with file paths so we can apply patches.
     */
    suspend fun repairBuildFailure(
        buildLogs: String,
        projectFiles: List<com.example.data.model.ProjectFile>,
        activeModelId: String,
        customApiKey: String? = null
    ): Result<AgentExecutionResult> {
        val keyFiles = projectFiles
            .filter {
                val p = it.path.lowercase()
                p.endsWith("build.gradle.kts") || p.endsWith("build.gradle") ||
                    p.endsWith("settings.gradle.kts") || p.endsWith("settings.gradle") ||
                    p.endsWith("gradle.properties") || p.endsWith("androidmanifest.xml") ||
                    p.endsWith("libs.versions.toml") || p.contains("gradle-wrapper") ||
                    p.endsWith(".kt")
            }
            .take(25)

        val filesContext = keyFiles.joinToString("\n\n") { f ->
            "### FILE: ${f.path}\n```\n${f.content.take(4000)}\n```"
        }

        val prompt = """
Android Gradle build FAILED. Fix the project so the next GitHub Actions run succeeds.

BUILD LOGS (tail):
```
${buildLogs.takeLast(10000)}
```

PROJECT FILES:
$filesContext

Instructions:
1. Explain the root cause briefly.
2. Output the FULL fixed content of every file you change inside markdown fences.
3. Use this exact fence format so the path is clear:
```path/to/File.kt
...full file content...
```
4. Prefer minimal fixes (Gradle versions, missing plugins, SDK, dependencies, syntax).
5. Do not invent secrets; use placeholders if needed.
        """.trimIndent()

        return executeAgentPrompt(
            prompt = prompt,
            activeModelId = activeModelId,
            provider = "google_gemini",
            customApiKey = customApiKey
        )
    }

    private fun extractThinking(text: String): String? {
        if (text.contains("<thought>") && text.contains("</thought>")) {
            return text.substringAfter("<thought>").substringBefore("</thought>").trim()
        }
        return null
    }

    private fun cleanResponseText(text: String): String {
        return text.replace(Regex("<thought>[\\s\\S]*?</thought>"), "").trim()
    }
}
