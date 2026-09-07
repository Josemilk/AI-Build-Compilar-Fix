package com.example.data.model

data class GoogleServicesValidationResult(
    val isValid: Boolean,
    val targetAppId: String,
    val matchingClientFound: Boolean,
    val projectId: String? = null,
    val projectNumber: String? = null,
    val firebaseUrl: String? = null,
    val storageBucket: String? = null,
    val apiKey: String? = null,
    val detectedPackageNames: List<String> = emptyList(),
    val statusMessage: String = "",
    val errors: List<String> = emptyList()
)

data class GoogleServicesState(
    val appId: String = "com.aistudio.buildstudio.kzyrqp",
    val jsonInput: String = "",
    val validationResult: GoogleServicesValidationResult? = null,
    val isSaved: Boolean = false,
    val saveMessage: String? = null
)
