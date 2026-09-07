package com.example.data.model

data class AiModelInfo(
    val id: String,
    val name: String,
    val alias: String,
    val description: String,
    val maxDailyQuota: Int,
    val currentUsage: Int,
    val contextWindow: String,
    val speed: String,
    val isDefault: Boolean = false
) {
    val remainingQuota: Int get() = (maxDailyQuota - currentUsage).coerceAtLeast(0)
    val quotaPercentage: Float get() = if (maxDailyQuota > 0) currentUsage.toFloat() / maxDailyQuota else 0f
    val isQuotaExhausted: Boolean get() = currentUsage >= maxDailyQuota
}

enum class AgentWorkPhase(
    val displayName: String,
    val description: String,
    val iconName: String
) {
    WORKING("Iniciando...", "Inicializando entorno del agente Gemini", "work"),
    ANALYZING("Analizando...", "Inspeccionando arquitectura y dependencias", "search"),
    ILLUMINATING("Iluminando...", "Diseñando layout e interacciones Compose", "lightbulb"),
    BUILDING("Construyendo...", "Compilando bytecode Kotlin y recursos AAPT2", "build"),
    VERIFYING("Verificando...", "Sincronizando APK con emulador streaming", "verified"),
    COMPLETED("Completado", "Build succeeded - listo en emulador", "check")
}

data class AgentProcessingState(
    val isActive: Boolean = false,
    val phase: AgentWorkPhase = AgentWorkPhase.WORKING,
    val currentActionTitle: String = "Working...",
    val currentDetail: String = "Processing request with Gemini Architect",
    val stepHistory: List<ToolAction> = emptyList(),
    val elapsedSeconds: Int = 0
)

data class FirebaseUserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

data class UserStudioPreferences(
    val selectedModelId: String = "gemini-2.5-flash",
    val selectedLlmProvider: String = "google_gemini",
    val selectedMinSdk: Int = 24,
    val customApiKey: String = "",
    val openaiApiKey: String = "",
    val anthropicApiKey: String = "",
    val deepseekApiKey: String = "",
    val customLlmEndpoint: String = "",
    val githubUsername: String = "",
    val githubRepoName: String = "",
    val githubAccessToken: String = "",
    val isLightTheme: Boolean = true
)

data class ExternalLlmApiKey(
    val id: String = java.util.UUID.randomUUID().toString(),
    val providerId: String = "openai", // "openai", "anthropic", "deepseek", "groq", "custom"
    val providerName: String = "OpenAI (ChatGPT)",
    val keyAlias: String = "My Primary Key",
    val apiKey: String = "",
    val endpointUrl: String = "",
    val defaultModel: String = "gpt-4o",
    val isEncrypted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    val maskedKey: String
        get() {
            if (apiKey.isBlank()) return "Sin clave"
            if (apiKey.length <= 8) return "••••••••"
            return "${apiKey.take(6)}••••••••${apiKey.takeLast(4)}"
        }
}

data class ToolAction(
    val title: String,
    val status: ToolStatus = ToolStatus.COMPLETED,
    val details: String? = null,
    val executionMs: Long = 120
)

enum class ToolStatus {
    RUNNING, COMPLETED, FAILED
}

data class ApkSigningInfo(
    val isSigned: Boolean = true,
    val signatureScheme: String = "APK Signature Scheme v2 + v3",
    val keyAlias: String = "release-keystore-v3",
    val certFingerprintSha256: String = "E4:8F:B3:9C:A1:66:7D:52:84:1E:09:F3:6B:42:10:99:A5:72:01:DF:8C:33:AA:91:02:45:DE:77:80:F1:C9:AA",
    val validityYears: Int = 30,
    val signedDate: String = "01 Sept 2026"
)

data class StudioApkProject(
    val id: String,
    val name: String,
    val packageName: String,
    val description: String,
    val category: String,
    val iconName: String = "android",
    val themeColor: String = "Blue",
    val targetSdk: Int = 36,
    val minSdk: Int = 24,
    val version: String = "1.0.0 (Build 1)",
    val apkSizeMb: Float = 14.8f,
    val lastUpdated: String = "Reciente",
    val isBuilt: Boolean = true,
    val signingInfo: ApkSigningInfo = ApkSigningInfo(),
    val dynamicAppName: String,
    val dynamicCounter: Int = 8240,
    val dynamicGoal: Int = 10000,
    val dynamicItemsCount: Int = 4,
    val files: List<ProjectFile> = emptyList(),
    val supervisorNotes: String = "Supervisado y optimizado por Gemini Architect."
)

enum class UploadStatus {
    PENDING,
    UPLOADING,
    SUCCESS,
    ERROR
}

data class AttachedFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val extension: String,
    val isImage: Boolean = false,
    val previewText: String? = null,
    val uriString: String? = null,
    /** Upload progress 0f..1f while uploading */
    val uploadProgress: Float = 0f,
    val uploadStatus: UploadStatus = UploadStatus.PENDING,
    /** Public download URL after successful upload to Firebase Storage */
    val downloadUrl: String? = null,
    val uploadError: String? = null
)

data class CodeSnippet(
    val filename: String,
    val language: String,
    val code: String
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val thinkingText: String? = null,
    val isThinkingExpanded: Boolean = false,
    val toolActions: List<ToolAction> = emptyList(),
    val attachedFiles: List<AttachedFile> = emptyList(),
    val codeSnippets: List<CodeSnippet> = emptyList(),
    val buildStatus: String? = null,
    val isStreaming: Boolean = false,
    val modelUsed: String? = null
)

data class ProjectFile(
    val path: String,
    val name: String,
    val extension: String,
    val content: String,
    val isModified: Boolean = false
)

enum class DeviceSkin {
    PIXEL_9_PRO,
    TABLET,
    COMPACT
}

data class AndroidSdkOption(
    val apiLevel: Int,
    val androidVersion: String,
    val codeName: String,
    val marketShare: String
)

data class GitHubActionsCompilationState(
    val isCompiling: Boolean = false,
    val statusMessage: String = "",
    val workflowFileName: String = ".github/workflows/android.yml",
    val runId: Long? = null,
    val runStatus: String = "idle", // "queued", "in_progress", "completed", "repairing"
    val runConclusion: String? = null, // "success", "failure"
    val htmlUrl: String? = null,
    val logs: List<String> = emptyList(),
    val repairAttempts: Int = 0,
    val isSuccessful: Boolean = false
)

data class GcpState(
    val projectId: String = "",
    val oauthToken: String = "",
    val isConnected: Boolean = false,
    val statusMessage: String? = null
)

data class GitHubState(
    val isConnected: Boolean = false,
    val token: String = "",
    val username: String = "",
    val userAvatarUrl: String? = null,
    val repoName: String = "",
    val branch: String = "main",
    val lastCommitHash: String = "",
    val lastCommitMessage: String = "",
    val isPushing: Boolean = false,
    val isLoading: Boolean = false,
    val repositories: List<com.example.data.api.GitHubRepoItem> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val commitUrl: String? = null,
    val actionsCompilationState: GitHubActionsCompilationState = GitHubActionsCompilationState()
)

enum class CompileTarget {
    GITHUB_ACTIONS,
    GOOGLE_CLOUD_BUILD
}

data class BuildExportState(
    val isBuilding: Boolean = false,
    val progress: Float = 0f,
    val currentStep: String = "",
    val apkGenerated: Boolean = false,
    val isSignedRelease: Boolean = false,
    val signingInfo: ApkSigningInfo = ApkSigningInfo(),
    val apkSizeMb: Float = 0f,
    val zipGenerated: Boolean = false,
    val zipSizeMb: Float = 0f,
    val cloudBuildStatus: String = "",
    val cloudPreviewUrl: String = "",
    val selectedTarget: CompileTarget = CompileTarget.GITHUB_ACTIONS,
    val processLogs: List<String> = emptyList(),
    val apkDownloadUrl: String? = null,
    val buildSucceeded: Boolean = false,
    val lastError: String? = null
)
