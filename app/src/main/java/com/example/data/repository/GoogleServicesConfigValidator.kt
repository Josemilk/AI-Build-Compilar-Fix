package com.example.data.repository

import com.example.data.model.GoogleServicesValidationResult
import org.json.JSONObject

class GoogleServicesConfigValidator {

    fun validate(
        jsonContent: String,
        targetAppId: String = "com.aistudio.buildstudio.kzyrqp"
    ): GoogleServicesValidationResult {
        val errors = mutableListOf<String>()
        val detectedPackages = mutableListOf<String>()

        if (jsonContent.isBlank()) {
            return GoogleServicesValidationResult(
                isValid = false,
                targetAppId = targetAppId,
                matchingClientFound = false,
                statusMessage = "El contenido del archivo google-services.json está vacío.",
                errors = listOf("JSON no proporcionado")
            )
        }

        try {
            val root = JSONObject(jsonContent)

            // Validate project_info
            if (!root.has("project_info")) {
                errors.add("Falta la clave 'project_info' en el nivel superior del JSON.")
            }

            val projectInfo = root.optJSONObject("project_info")
            val projectId = projectInfo?.optString("project_id")
            val projectNumber = projectInfo?.optString("project_number")
            val firebaseUrl = projectInfo?.optString("firebase_url")
            val storageBucket = projectInfo?.optString("storage_bucket")

            if (projectId.isNull_or_blank()) {
                errors.add("Falta 'project_id' dentro de 'project_info'.")
            }

            // Validate client array
            val clientArray = root.optJSONArray("client")
            if (clientArray == null || clientArray.length() == 0) {
                errors.add("No se encontraron clientes registrados en la sección 'client'.")
            }

            var matchingClientFound = false
            var extractedApiKey: String? = null

            if (clientArray != null) {
                for (i in 0 until clientArray.length()) {
                    val clientObj = clientArray.optJSONObject(i) ?: continue
                    val clientInfo = clientObj.optJSONObject("client_info")
                    val androidClientInfo = clientInfo?.optJSONObject("android_client_info")
                    val packageName = androidClientInfo?.optString("package_name")

                    if (!packageName.isNullOrBlank()) {
                        detectedPackages.add(packageName)
                    }

                    if (packageName == targetAppId) {
                        matchingClientFound = true

                        // Extract API Key from api_key array
                        val apiKeys = clientObj.optJSONArray("api_key")
                        if (apiKeys != null && apiKeys.length() > 0) {
                            val firstApiKeyObj = apiKeys.optJSONObject(0)
                            extractedApiKey = firstApiKeyObj?.optString("current_key")
                        }
                    }
                }
            }

            if (!matchingClientFound) {
                errors.add("No se encontró ningún cliente en google-services.json con package_name '$targetAppId'. Paquetes detectados: $detectedPackages")
            }

            val isValid = errors.isEmpty() && matchingClientFound
            val statusMsg = if (isValid) {
                "✅ Archivo google-services.json VÁLIDO para el Application ID '$targetAppId'. Proyecto: '$projectId'."
            } else {
                "⚠️ Archivo google-services.json INVÁLIDO o no coincide con '$targetAppId'."
            }

            return GoogleServicesValidationResult(
                isValid = isValid,
                targetAppId = targetAppId,
                matchingClientFound = matchingClientFound,
                projectId = projectId,
                projectNumber = projectNumber,
                firebaseUrl = firebaseUrl,
                storageBucket = storageBucket,
                apiKey = extractedApiKey,
                detectedPackageNames = detectedPackages,
                statusMessage = statusMsg,
                errors = errors
            )

        } catch (e: Exception) {
            return GoogleServicesValidationResult(
                isValid = false,
                targetAppId = targetAppId,
                matchingClientFound = false,
                statusMessage = "Error de sintaxis JSON: ${e.message}",
                errors = listOf("Error al parsear JSON: ${e.message}")
            )
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
}
