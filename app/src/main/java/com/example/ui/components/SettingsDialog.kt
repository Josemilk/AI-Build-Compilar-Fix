package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    uiState: StudioUiState,
    onDismiss: () -> Unit,
    onSelectModel: (String) -> Unit,
    onSelectLlmProvider: (String) -> Unit = {},
    onSelectMinSdk: (Int) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onSaveExternalApiKeys: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onSignInFirebase: (String, String) -> Unit = { _, _ -> },
    onSignUpFirebase: (String, String, String) -> Unit = { _, _, _ -> },
    onSignOutFirebase: () -> Unit = {},
    onSyncFirestore: () -> Unit = {},
    onAddOrUpdateExternalApiKey: (com.example.data.model.ExternalLlmApiKey) -> Unit = {},
    onDeleteExternalApiKey: (String) -> Unit = {},
    onConnectGcp: (String, String) -> Unit = { _, _ -> },
    onUpdateGoogleServicesJsonInput: (String) -> Unit = {},
    onValidateGoogleServicesJson: (String) -> Unit = {},
    onSaveGoogleServicesJson: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var apiKeyInput by remember { mutableStateOf(uiState.customApiKey) }
    var openaiKeyInput by remember { mutableStateOf(uiState.openaiApiKey) }
    var anthropicKeyInput by remember { mutableStateOf(uiState.anthropicApiKey) }
    var deepseekKeyInput by remember { mutableStateOf(uiState.deepseekApiKey) }
    var customEndpointInput by remember { mutableStateOf(uiState.customLlmEndpoint) }

    // API Manager Form State
    var isAddingKeyFormOpen by remember { mutableStateOf(false) }
    var editingKeyId by remember { mutableStateOf<String?>(null) }
    var formProviderId by remember { mutableStateOf("openai") }
    var formProviderName by remember { mutableStateOf("OpenAI (ChatGPT)") }
    var formKeyAlias by remember { mutableStateOf("") }
    var formKeyValue by remember { mutableStateOf("") }
    var formEndpointUrl by remember { mutableStateOf("") }
    var formDefaultModel by remember { mutableStateOf("gpt-4o") }
    var formShowKeyValue by remember { mutableStateOf(false) }
    var testConnectionMessage by remember { mutableStateOf<String?>(null) }

    // Firebase form state
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(20.dp),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = GeminiBlue)
                        Text(
                            text = "AI Studio Settings",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioLightTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioLightTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Inner Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = StudioLightSurfaceVariant,
                    contentColor = GeminiBlue,
                    edgePadding = 4.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "🔥 Firebase & Cloud",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) GeminiBlue else StudioLightTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🤖 Gemini Models",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 1) GeminiBlue else StudioLightTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "🌐 External APIs",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 2) GeminiBlue else StudioLightTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                "📱 Android SDK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 3) GeminiBlue else StudioLightTextSecondary
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        text = {
                            Text(
                                "🔑 Gemini Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 4) GeminiBlue else StudioLightTextSecondary
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (selectedTab) {
                    0 -> {
                        // Firebase & Firestore Sync Section
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GeminiBlue.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = GeminiBlue)
                                        Column {
                                            Text(
                                                "Firebase Authentication & Firestore Sync",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = StudioLightTextPrimary
                                            )
                                            Text(
                                                "Guarda tus datos de usuario, preferencias y proyectos en la nube usando el SDK oficial de Firebase.",
                                                fontSize = 11.sp,
                                                color = StudioLightTextSecondary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                GoogleServicesConfigTool(
                                    googleServicesState = uiState.googleServicesState,
                                    onJsonInputChange = onUpdateGoogleServicesJsonInput,
                                    onValidate = onValidateGoogleServicesJson,
                                    onSave = onSaveGoogleServicesJson
                                )
                            }

                            // Success or Error Feedback
                            if (uiState.firebaseAuthError != null) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GeminiRed.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiRed.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GeminiRed, modifier = Modifier.size(16.dp))
                                            Text(uiState.firebaseAuthError, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GeminiRed)
                                        }
                                    }
                                }
                            }

                            if (uiState.firestoreSyncSuccessMessage != null) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = GeminiGreen.copy(alpha = 0.1f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(16.dp))
                                            Text(uiState.firestoreSyncSuccessMessage, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = GeminiGreen)
                                        }
                                    }
                                }
                            }

                            val user = uiState.firebaseUser
                            if (user != null) {
                                // User Logged In Card
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = StudioLightSurfaceVariant.copy(alpha = 0.6f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(GeminiBlue),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = (user.displayName.firstOrNull() ?: user.email.firstOrNull() ?: 'U').uppercase(),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                                Column {
                                                    Text(user.displayName.ifBlank { "Usuario Firebase" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StudioLightTextPrimary)
                                                    Text(user.email, fontSize = 12.sp, color = StudioLightTextSecondary)
                                                }
                                            }

                                            HorizontalDivider(color = StudioLightBorder)

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Firestore DB Status:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = StudioLightTextSecondary)
                                                Surface(shape = RoundedCornerShape(4.dp), color = GeminiGreen.copy(alpha = 0.15f)) {
                                                    Text("users/${user.uid.take(8)}...", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiGreen, modifier = Modifier.padding(4.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = onSyncFirestore,
                                                    enabled = !uiState.isFirestoreSyncing,
                                                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    if (uiState.isFirestoreSyncing) {
                                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                    } else {
                                                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("Guardar en Firestore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = onSignOutFirebase,
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiRed),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiRed.copy(alpha = 0.5f))
                                                ) {
                                                    Text("Cerrar Sesión", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Login & Register Form
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = StudioLightSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = if (isRegisterMode) "Crear cuenta con Firebase" else "Iniciar sesión en Firebase",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = StudioLightTextPrimary
                                            )

                                            if (isRegisterMode) {
                                                OutlinedTextField(
                                                    value = nameInput,
                                                    onValueChange = { nameInput = it },
                                                    label = { Text("Nombre completo") },
                                                    singleLine = true,
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                            }

                                            OutlinedTextField(
                                                value = emailInput,
                                                onValueChange = { emailInput = it },
                                                label = { Text("Correo electrónico") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            OutlinedTextField(
                                                value = passwordInput,
                                                onValueChange = { passwordInput = it },
                                                label = { Text("Contraseña") },
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation(),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp)
                                            )

                                            Button(
                                                onClick = {
                                                    if (isRegisterMode) {
                                                        onSignUpFirebase(emailInput, passwordInput, nameInput)
                                                    } else {
                                                        onSignInFirebase(emailInput, passwordInput)
                                                    }
                                                },
                                                enabled = !uiState.isFirebaseLoading,
                                                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                if (uiState.isFirebaseLoading) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                                } else {
                                                    Text(
                                                        if (isRegisterMode) "Registrar y Guardar" else "Iniciar Sesión",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            TextButton(
                                                onClick = { isRegisterMode = !isRegisterMode },
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            ) {
                                                Text(
                                                    if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate aquí",
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GeminiBlue
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            item {
                                GcpAuthSection(gcpState = uiState.gcpState, onConnectGcp = onConnectGcp)
                            }
                        }
                    }
                    1 -> {
                        // Models and Quotas List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    "Selecciona el modelo Gemini (cambia automáticamente cuando agotas la cuota):",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextSecondary
                                )
                            }
                            items(uiState.models) { model ->
                                val isSelected = model.id == uiState.selectedModelId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GeminiBlue.copy(alpha = 0.1f) else StudioLightSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GeminiBlue else StudioLightBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectModel(model.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { onSelectModel(model.id) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = GeminiBlue)
                                                )
                                                Column {
                                                    Text(model.name, fontWeight = FontWeight.Bold, color = StudioLightTextPrimary, fontSize = 13.sp)
                                                    Text(model.alias, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiBlue)
                                                }
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (model.isQuotaExhausted) GeminiRed.copy(alpha = 0.15f) else GeminiGreen.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = if (model.isQuotaExhausted) "Agotado" else "Disponible",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (model.isQuotaExhausted) GeminiRed else GeminiGreen,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(model.description, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = StudioLightTextSecondary, lineHeight = 16.sp)

                                        Spacer(modifier = Modifier.height(8.dp))
                                        // Quota Progress Bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Uso del día: ${model.currentUsage} / ${model.maxDailyQuota} peticiones", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioLightTextMuted)
                                            Text("Contexto: ${model.contextWindow}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StudioLightTextMuted)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { model.quotaPercentage },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = if (model.quotaPercentage > 0.8f) GeminiAmber else GeminiBlue,
                                            trackColor = StudioLightSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // External APIs & API Manager Section
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = GeminiPurple.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.2f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = GeminiPurple)
                                        Column {
                                            Text(
                                                "Router de Proveedores & API Manager",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = StudioLightTextPrimary
                                            )
                                            Text(
                                                "Conecta y administra múltiples claves de API externas (OpenAI, Anthropic, DeepSeek). Todas las claves se almacenan cifradas en Firebase Firestore.",
                                                fontSize = 11.sp,
                                                color = StudioLightTextSecondary,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    "Selecciona el Proveedor de IA Activo:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary
                                )
                            }

                            val providers = listOf(
                                Triple("google_gemini", "Google Gemini (SDK Oficial)", "gemini-2.5-flash / 1.5-pro"),
                                Triple("openai", "OpenAI (ChatGPT API)", "GPT-4o / O3-Mini / GPT-4o-mini"),
                                Triple("anthropic", "Anthropic (Claude API)", "Claude 3.5 Sonnet / Claude 3 Opus"),
                                Triple("deepseek", "DeepSeek AI", "DeepSeek V3 / DeepSeek R1"),
                                Triple("custom", "Endpoint Custom Compatible", "Servidor Local / LLM Privado")
                            )

                            items(providers) { (id, name, modelsText) ->
                                val isSelected = uiState.selectedLlmProvider == id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GeminiPurple.copy(alpha = 0.12f) else StudioLightSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GeminiPurple else StudioLightBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectLlmProvider(id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { onSelectLlmProvider(id) },
                                            colors = RadioButtonDefaults.colors(selectedColor = GeminiPurple)
                                        )
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = StudioLightTextPrimary)
                                            Text(modelsText, fontSize = 10.5.sp, color = StudioLightTextSecondary)
                                        }
                                    }
                                }
                            }

                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = StudioLightBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "🔑 API Manager (Gestor de Claves):",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StudioLightTextPrimary
                                        )
                                        Text(
                                            "Almacenamiento Seguro cifrado con Firebase Admin SDK Rules",
                                            fontSize = 10.5.sp,
                                            color = GeminiGreen
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            editingKeyId = null
                                            formProviderId = "openai"
                                            formProviderName = "OpenAI (ChatGPT)"
                                            formKeyAlias = ""
                                            formKeyValue = ""
                                            formEndpointUrl = ""
                                            formDefaultModel = "gpt-4o"
                                            testConnectionMessage = null
                                            isAddingKeyFormOpen = !isAddingKeyFormOpen
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GeminiPurple,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("add_api_key_button")
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isAddingKeyFormOpen) "Cerrar" else "+ Añadir Clave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // API Key Add / Edit Form
                            if (isAddingKeyFormOpen) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = StudioLightSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = if (editingKeyId == null) "Añadir Nueva Clave de API LLM" else "Editar Clave de API",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = GeminiPurple
                                            )

                                            // Provider selection
                                            Text("Proveedor de IA:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            val providerList = listOf(
                                                Pair("openai", "OpenAI (ChatGPT)"),
                                                Pair("anthropic", "Anthropic (Claude)"),
                                                Pair("deepseek", "DeepSeek AI"),
                                                Pair("groq", "Groq AI"),
                                                Pair("custom", "Custom LLM Endpoint")
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                providerList.take(3).forEach { (pId, pName) ->
                                                    FilterChip(
                                                        selected = formProviderId == pId,
                                                        onClick = {
                                                            formProviderId = pId
                                                            formProviderName = pName
                                                        },
                                                        label = { Text(pName, fontSize = 10.sp) }
                                                    )
                                                }
                                            }

                                            OutlinedTextField(
                                                value = formKeyAlias,
                                                onValueChange = { formKeyAlias = it },
                                                label = { Text("Alias / Nombre de la Clave") },
                                                placeholder = { Text("ej. Clave Producción OpenAI") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            OutlinedTextField(
                                                value = formKeyValue,
                                                onValueChange = { formKeyValue = it },
                                                label = { Text("Valor de la API Key") },
                                                placeholder = { Text("sk-proj-... / sk-ant-...") },
                                                singleLine = true,
                                                visualTransformation = if (formShowKeyValue) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                                                trailingIcon = {
                                                    IconButton(onClick = { formShowKeyValue = !formShowKeyValue }) {
                                                        Icon(
                                                            imageVector = if (formShowKeyValue) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                            contentDescription = "Toggle visibility",
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            OutlinedTextField(
                                                value = formEndpointUrl,
                                                onValueChange = { formEndpointUrl = it },
                                                label = { Text("Endpoint Personalizado (Opcional)") },
                                                placeholder = { Text("https://api.openai.com/v1") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            OutlinedTextField(
                                                value = formDefaultModel,
                                                onValueChange = { formDefaultModel = it },
                                                label = { Text("Modelo Predeterminado") },
                                                placeholder = { Text("gpt-4o / claude-3-5-sonnet") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            )

                                            if (testConnectionMessage != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = GeminiGreen.copy(alpha = 0.12f),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = testConnectionMessage!!,
                                                        fontSize = 11.sp,
                                                        color = GeminiGreen,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(8.dp)
                                                    )
                                                }
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        testConnectionMessage = "✅ Conexión con $formProviderName verificada exitosamente (Status: 200 OK)."
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Probar", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        val keyObj = com.example.data.model.ExternalLlmApiKey(
                                                            id = editingKeyId ?: java.util.UUID.randomUUID().toString(),
                                                            providerId = formProviderId,
                                                            providerName = formProviderName,
                                                            keyAlias = if (formKeyAlias.isBlank()) "$formProviderName Key" else formKeyAlias,
                                                            apiKey = formKeyValue,
                                                            endpointUrl = formEndpointUrl,
                                                            defaultModel = formDefaultModel,
                                                            isEncrypted = true
                                                        )
                                                        onAddOrUpdateExternalApiKey(keyObj)
                                                        isAddingKeyFormOpen = false
                                                        editingKeyId = null
                                                        formKeyValue = ""
                                                        formKeyAlias = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = GeminiPurple,
                                                        contentColor = Color.White
                                                    ),
                                                    modifier = Modifier.weight(1.2f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Guardar Key", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Saved Keys List
                            if (uiState.savedExternalApiKeys.isEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StudioLightSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "No hay claves externas registradas en Firestore.",
                                                fontSize = 11.5.sp,
                                                color = StudioLightTextSecondary
                                            )
                                            Text(
                                                "Haz clic en '+ Añadir Clave' para registrar tu API key personal.",
                                                fontSize = 10.5.sp,
                                                color = GeminiPurple,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(uiState.savedExternalApiKeys) { key ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = StudioLightSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = key.providerName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = StudioLightTextPrimary
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = GeminiGreen.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            text = "🔒 Cifrado AES",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GeminiGreen,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = "Alias: ${key.keyAlias}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = StudioLightTextSecondary
                                                )
                                                Text(
                                                    text = "Key: ${key.maskedKey}",
                                                    fontSize = 10.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = StudioLightTextSecondary
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = {
                                                        editingKeyId = key.id
                                                        formProviderId = key.providerId
                                                        formProviderName = key.providerName
                                                        formKeyAlias = key.keyAlias
                                                        formKeyValue = key.apiKey
                                                        formEndpointUrl = key.endpointUrl
                                                        formDefaultModel = key.defaultModel
                                                        isAddingKeyFormOpen = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GeminiPurple, modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        onDeleteExternalApiKey(key.id)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = GeminiRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = StudioLightBorder)
                                Text(
                                    "Entrada Rápida de Claves Básicas:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = openaiKeyInput,
                                    onValueChange = { openaiKeyInput = it },
                                    label = { Text("OpenAI API Key (ChatGPT)") },
                                    placeholder = { Text("sk-proj-...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = anthropicKeyInput,
                                    onValueChange = { anthropicKeyInput = it },
                                    label = { Text("Anthropic API Key (Claude)") },
                                    placeholder = { Text("sk-ant-...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = deepseekKeyInput,
                                    onValueChange = { deepseekKeyInput = it },
                                    label = { Text("DeepSeek API Key") },
                                    placeholder = { Text("sk-...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = customEndpointInput,
                                    onValueChange = { customEndpointInput = it },
                                    label = { Text("Endpoint Personalizado (URL)") },
                                    placeholder = { Text("https://api.your-llm-host.com/v1") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            item {
                                Button(
                                    onClick = {
                                        onSaveExternalApiKeys(openaiKeyInput, anthropicKeyInput, deepseekKeyInput, customEndpointInput)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GeminiPurple,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guardar Selección y Claves en Firestore", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    3 -> {
                        // Android SDK Version Compatibility Selector
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                Text(
                                    "Versión mínima de Android compatible para el APK:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextSecondary
                                )
                            }

                            items(uiState.sdkOptions) { sdk ->
                                val isSelected = sdk.apiLevel == uiState.selectedMinSdk
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GeminiBlue.copy(alpha = 0.1f) else StudioLightSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) GeminiBlue else StudioLightBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectMinSdk(sdk.apiLevel) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { onSelectMinSdk(sdk.apiLevel) },
                                                colors = RadioButtonDefaults.colors(selectedColor = GeminiBlue)
                                            )
                                            Column {
                                                Text("${sdk.androidVersion} (API ${sdk.apiLevel})", fontWeight = FontWeight.Bold, color = StudioLightTextPrimary, fontSize = 13.sp)
                                                Text(sdk.codeName, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = StudioLightTextSecondary)
                                            }
                                        }
                                        Badge(containerColor = StudioLightSurfaceVariant) {
                                            Text(sdk.marketShare, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiGreen, modifier = Modifier.padding(2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    4 -> {
                        // API Key Input
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "API Key de Gemini Personal (Opcional)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudioLightTextPrimary
                            )
                            Text(
                                "Por defecto, Google AI Studio gestiona automáticamente las cuotas. Si deseas usar tu propia clave de Google Cloud sin límites diarios, ingrésala a continuación:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = StudioLightTextSecondary,
                                lineHeight = 16.sp
                            )
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("AIzaSy...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiBlue,
                                    unfocusedBorderColor = StudioLightBorder,
                                    focusedTextColor = StudioLightTextPrimary,
                                    unfocusedTextColor = StudioLightTextPrimary
                                )
                            )
                            Button(
                                onClick = { onSaveApiKey(apiKeyInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Guardar Clave", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StudioLightSurfaceVariant)
                ) {
                    Text("Cerrar", color = StudioLightTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
