package com.example.data.repository

import android.util.Log
import com.example.data.model.ExternalLlmApiKey
import com.example.data.model.FirebaseUserProfile
import com.example.data.model.UserStudioPreferences
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.OAuthCredential
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseStudioRepository(
    private val authProvider: () -> FirebaseAuth? = {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    },
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }
) {
    companion object {
        private const val TAG = "FirebaseStudioRepo"
        private const val COLLECTION_USERS = "users"
        private const val SUBCOLLECTION_STUDIO_DATA = "studio_data"
        private const val DOC_PREFERENCES = "preferences"
        private const val SUBCOLLECTION_PROJECTS = "projects"
    }

    private val auth: FirebaseAuth?
        get() = authProvider()

    private val firestore: FirebaseFirestore?
        get() = firestoreProvider()

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun getAuthStateFlow(): Flow<FirebaseUserProfile?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                trySend(
                    FirebaseUserProfile(
                        uid = user.uid,
                        email = user.email ?: "developer@aistudio.google",
                        displayName = user.displayName ?: user.email?.substringBefore("@") ?: "AI Studio Developer"
                    )
                )
            } else {
                trySend(null)
            }
        }
        currentAuth.addAuthStateListener(listener)
        awaitClose { currentAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUserProfile> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase Auth no está inicializado."))
        return try {
            val authResult = currentAuth.signInWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user ?: throw Exception("User is null after successful authentication")
            val profile = FirebaseUserProfile(
                uid = user.uid,
                email = user.email ?: email,
                displayName = user.displayName ?: email.substringBefore("@")
            )
            Result.success(profile)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "FirebaseAuthException during signIn", e)
            val friendlyMsg = when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
                "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
                "ERROR_USER_NOT_FOUND" -> "No existe una cuenta registrada con este correo."
                "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
                else -> e.localizedMessage ?: "Error al iniciar sesión con Firebase Auth."
            }
            Result.failure(Exception(friendlyMsg))
        } catch (e: Exception) {
            Log.e(TAG, "General exception during signIn", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<FirebaseUserProfile> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase Auth no está inicializado."))
        return try {
            val authResult = currentAuth.createUserWithEmailAndPassword(email.trim(), password).awaitTask()
            val user = authResult.user ?: throw Exception("User is null after signup")
            
            // Set display name on Firebase Auth profile
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).awaitTask()
            }

            val profile = FirebaseUserProfile(
                uid = user.uid,
                email = user.email ?: email,
                displayName = displayName.ifBlank { email.substringBefore("@") }
            )

            // Save initial preferences in Firestore
            saveUserPreferences(profile.uid, UserStudioPreferences())

            Result.success(profile)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "FirebaseAuthException during signUp", e)
            val friendlyMsg = when (e.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo electrónico ya está registrado. Por favor inicia sesión."
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil (mínimo 6 caracteres)."
                "ERROR_INVALID_EMAIL" -> "El formato del correo electrónico no es válido."
                else -> e.localizedMessage ?: "Error al registrar usuario en Firebase Auth."
            }
            Result.failure(Exception(friendlyMsg))
        } catch (e: Exception) {
            Log.e(TAG, "General exception during signUp", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
        }
    }

    suspend fun saveUserPreferences(uid: String, preferences: UserStudioPreferences): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val dataMap = hashMapOf(
                "selectedModelId" to preferences.selectedModelId,
                "selectedLlmProvider" to preferences.selectedLlmProvider,
                "selectedMinSdk" to preferences.selectedMinSdk,
                "customApiKey" to preferences.customApiKey,
                "openaiApiKey" to preferences.openaiApiKey,
                "anthropicApiKey" to preferences.anthropicApiKey,
                "deepseekApiKey" to preferences.deepseekApiKey,
                "customLlmEndpoint" to preferences.customLlmEndpoint,
                "githubUsername" to preferences.githubUsername,
                "githubRepoName" to preferences.githubRepoName,
                "githubAccessToken" to preferences.githubAccessToken,
                "isLightTheme" to preferences.isLightTheme,
                "updatedAt" to System.currentTimeMillis()
            )

            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_STUDIO_DATA)
                .document(DOC_PREFERENCES)
                .set(dataMap, SetOptions.merge())
                .awaitTask()

            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, "FirebaseFirestoreException saving preferences", e)
            Result.failure(Exception("Firestore error: ${e.localizedMessage}"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception saving preferences", e)
            Result.failure(e)
        }
    }

    suspend fun getUserPreferences(uid: String): Result<UserStudioPreferences> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val snapshot = currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_STUDIO_DATA)
                .document(DOC_PREFERENCES)
                .get()
                .awaitTask()

            if (snapshot.exists()) {
                val modelId = snapshot.getString("selectedModelId") ?: "gemini-2.5-flash"
                val llmProvider = snapshot.getString("selectedLlmProvider") ?: "google_gemini"
                val minSdk = snapshot.getLong("selectedMinSdk")?.toInt() ?: 24
                val apiKey = snapshot.getString("customApiKey") ?: ""
                val openaiKey = snapshot.getString("openaiApiKey") ?: ""
                val anthropicKey = snapshot.getString("anthropicApiKey") ?: ""
                val deepseekKey = snapshot.getString("deepseekApiKey") ?: ""
                val customEndpoint = snapshot.getString("customLlmEndpoint") ?: ""
                val ghUser = snapshot.getString("githubUsername") ?: ""
                val ghRepo = snapshot.getString("githubRepoName") ?: ""
                val ghToken = snapshot.getString("githubAccessToken") ?: ""
                val isLight = snapshot.getBoolean("isLightTheme") ?: true

                Result.success(
                    UserStudioPreferences(
                        selectedModelId = modelId,
                        selectedLlmProvider = llmProvider,
                        selectedMinSdk = minSdk,
                        customApiKey = apiKey,
                        openaiApiKey = openaiKey,
                        anthropicApiKey = anthropicKey,
                        deepseekApiKey = deepseekKey,
                        customLlmEndpoint = customEndpoint,
                        githubUsername = ghUser,
                        githubRepoName = ghRepo,
                        githubAccessToken = ghToken,
                        isLightTheme = isLight
                    )
                )
            } else {
                Result.success(UserStudioPreferences())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user preferences from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun saveExternalApiKey(uid: String, apiKeyObj: ExternalLlmApiKey): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            // Simple Base64 encoding for storage (not cryptographic encryption).
            // For production, prefer EncryptedSharedPreferences or Android Keystore + AES.
            val rawKeyBytes = apiKeyObj.apiKey.toByteArray(Charsets.UTF_8)
            val encodedKeyString = android.util.Base64.encodeToString(rawKeyBytes, android.util.Base64.NO_WRAP)

            val dataMap = hashMapOf(
                "id" to apiKeyObj.id,
                "providerId" to apiKeyObj.providerId,
                "providerName" to apiKeyObj.providerName,
                "keyAlias" to apiKeyObj.keyAlias,
                "encryptedKey" to encodedKeyString,
                "endpointUrl" to apiKeyObj.endpointUrl,
                "defaultModel" to apiKeyObj.defaultModel,
                "isEncrypted" to false,
                "encoding" to "Base64",
                "createdAt" to apiKeyObj.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("external_api_keys")
                .document(apiKeyObj.id)
                .set(dataMap, SetOptions.merge())
                .awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving external API key in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun getExternalApiKeys(uid: String): Result<List<ExternalLlmApiKey>> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val snapshot = currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("external_api_keys")
                .get()
                .awaitTask()

            val keysList = mutableListOf<ExternalLlmApiKey>()
            for (doc in snapshot.documents) {
                if (doc.exists()) {
                    val id = doc.getString("id") ?: doc.id
                    val pId = doc.getString("providerId") ?: "openai"
                    val pName = doc.getString("providerName") ?: "OpenAI (ChatGPT)"
                    val alias = doc.getString("keyAlias") ?: "Primary Key"
                    val encKey = doc.getString("encryptedKey") ?: ""
                    val endpoint = doc.getString("endpointUrl") ?: ""
                    val model = doc.getString("defaultModel") ?: "gpt-4o"
                    val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    val decryptedKey = if (encKey.isNotBlank()) {
                        try {
                            String(android.util.Base64.decode(encKey, android.util.Base64.NO_WRAP), Charsets.UTF_8)
                        } catch (_: Exception) {
                            encKey
                        }
                    } else ""

                    keysList.add(
                        ExternalLlmApiKey(
                            id = id,
                            providerId = pId,
                            providerName = pName,
                            keyAlias = alias,
                            apiKey = decryptedKey,
                            endpointUrl = endpoint,
                            defaultModel = model,
                            isEncrypted = true,
                            createdAt = createdAt
                        )
                    )
                }
            }
            Result.success(keysList)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading external API keys from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteExternalApiKey(uid: String, keyId: String): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("external_api_keys")
                .document(keyId)
                .delete()
                .awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting external API key from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun recordLlmUsage(
        uid: String,
        provider: String,
        modelId: String,
        tokensUsed: Int
    ): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val docRef = currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("usage_quotas")
                .document(dateStr)

            val dataMap = hashMapOf<String, Any>(
                "lastUpdated" to System.currentTimeMillis(),
                "totalRequests" to com.google.firebase.firestore.FieldValue.increment(1),
                "totalTokens" to com.google.firebase.firestore.FieldValue.increment(tokensUsed.toLong()),
                "modelRequests.$modelId" to com.google.firebase.firestore.FieldValue.increment(1)
            )

            docRef.set(dataMap, SetOptions.merge()).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error recording LLM usage in Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun getLlmUsageQuotas(uid: String): Result<Map<String, Int>> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val snapshot = currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("usage_quotas")
                .document(dateStr)
                .get()
                .awaitTask()

            val usageMap = mutableMapOf<String, Int>()
            if (snapshot.exists()) {
                val modelReqs = snapshot.get("modelRequests") as? Map<*, *>
                modelReqs?.forEach { (k, v) ->
                    if (k is String && v is Number) {
                        usageMap[k] = v.toInt()
                    }
                }
            }
            Result.success(usageMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading LLM usage quotas from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun saveGitHubOAuthToken(uid: String, accessToken: String, username: String? = null): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val dataMap = hashMapOf<String, Any>(
                "githubAccessToken" to accessToken,
                "updatedAt" to System.currentTimeMillis()
            )
            if (!username.isNullOrBlank()) {
                dataMap["githubUsername"] = username
            }

            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_STUDIO_DATA)
                .document(DOC_PREFERENCES)
                .set(dataMap, SetOptions.merge())
                .awaitTask()

            val secureTokenMap = hashMapOf(
                "accessToken" to accessToken,
                "provider" to "github.com",
                "linkedAt" to System.currentTimeMillis()
            )
            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection("secure_tokens")
                .document("github")
                .set(secureTokenMap, SetOptions.merge())
                .awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving GitHub OAuth token to Firestore", e)
            Result.failure(e)
        }
    }

    fun parseGitHubOAuthError(throwable: Throwable): String {
        if (throwable is FirebaseAuthException) {
            return when (throwable.errorCode) {
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL",
                "ERROR_CREDENTIAL_ALREADY_IN_USE" ->
                    "⚠️ Esta cuenta de GitHub ya está vinculada a otro usuario o correo en Firebase Auth. Inicia sesión con el método original o usa otra cuenta de GitHub."
                "ERROR_WEB_CONTEXT_CANCELLED",
                "ERROR_WEB_CONTEXT_ALREADY_PRESENTED" ->
                    "🚫 La ventana de inicio de sesión de GitHub fue cancelada o cerrada antes de completar la autorización OAuth2."
                "ERROR_OPERATION_NOT_ALLOWED" ->
                    "🔒 El proveedor OAuth2 de GitHub no está activado en Firebase Authentication. Habilítalo en Firebase Console > Authentication > Sign-in method."
                "ERROR_INVALID_CREDENTIAL",
                "ERROR_INVALID_CUSTOM_TOKEN" ->
                    "🔑 La credencial devuelta por GitHub OAuth no es válida o caducó. Por favor reintenta la autenticación."
                "ERROR_USER_DISABLED" ->
                    "🚫 La cuenta de usuario asociada ha sido deshabilitada en Firebase Authentication."
                "ERROR_NETWORK_REQUEST_FAILED" ->
                    "🌐 Fallo de red: No se pudo establecer conexión con los servidores de Firebase Auth / GitHub OAuth."
                else ->
                    "❌ Error de Firebase Auth [${throwable.errorCode}]: ${throwable.localizedMessage ?: throwable.message}"
            }
        }

        val msg = throwable.localizedMessage ?: throwable.message ?: ""
        return when {
            msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true) ->
                "🌐 Fallo de conexión a Internet durante la devolución de llamada (callback) de OAuth2."
            msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true) ->
                "🚫 Flujo de autenticación OAuth2 cancelado por el usuario."
            msg.contains("Firebase Auth no está inicializado", ignoreCase = true) ->
                "⚠️ Firebase Auth no se encuentra inicializado en el dispositivo."
            else -> "❌ Error en callback de GitHub OAuth2: $msg"
        }
    }

    suspend fun linkWithGitHubOAuth(activity: android.app.Activity): Result<String> {
        val currentAuth = auth ?: return Result.failure(Exception(parseGitHubOAuthError(Exception("Firebase Auth no está inicializado."))))
        val provider = OAuthProvider.newBuilder("github.com")
        provider.scopes = listOf("repo", "user", "read:user")

        return try {
            val pendingResultTask = currentAuth.pendingAuthResult
            val authResult = if (pendingResultTask != null) {
                pendingResultTask.awaitTask()
            } else {
                val currentUser = currentAuth.currentUser
                if (currentUser != null) {
                    currentUser.startActivityForLinkWithProvider(activity, provider.build()).awaitTask()
                } else {
                    currentAuth.startActivityForSignInWithProvider(activity, provider.build()).awaitTask()
                }
            }

            val credential = authResult.credential as? OAuthCredential
            val accessToken = credential?.accessToken ?: ""
            val user = authResult.user

            if (user != null && accessToken.isNotBlank()) {
                saveGitHubOAuthToken(user.uid, accessToken)
            }

            if (accessToken.isBlank()) {
                val noTokenErr = Exception("La redirección OAuth2 de GitHub finalizó pero no devolvió un token de acceso válido.")
                return Result.failure(Exception(parseGitHubOAuthError(noTokenErr)))
            }

            Result.success(accessToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error in linkWithGitHubOAuth", e)
            val parsedMsg = parseGitHubOAuthError(e)
            Result.failure(Exception(parsedMsg, e))
        }
    }

    suspend fun saveProjectToFirestore(
        uid: String,
        projectName: String,
        filesCount: Int,
        activeModel: String,
        summary: String
    ): Result<Unit> {
        val currentFirestore = firestore ?: return Result.failure(Exception("Firestore no está inicializado."))
        return try {
            val projectData = hashMapOf(
                "projectName" to projectName,
                "filesCount" to filesCount,
                "activeModel" to activeModel,
                "summary" to summary,
                "lastModified" to System.currentTimeMillis()
            )

            val projectId = projectName.lowercase().replace(" ", "_")
            currentFirestore.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_PROJECTS)
                .document(projectId)
                .set(projectData, SetOptions.merge())
                .awaitTask()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving project snapshot to Firestore", e)
            Result.failure(e)
        }
    }
}

// Coroutine Task await helper
suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}
