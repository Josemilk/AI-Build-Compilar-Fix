package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@Composable
fun GitHubSyncDialog(
    uiState: StudioUiState,
    onDismiss: () -> Unit,
    onConnectToken: (String) -> Unit,
    onConnectOAuth: (Activity) -> Unit = {},
    onClearError: () -> Unit = {},
    onRefreshRepos: () -> Unit,
    onSelectRepo: (String) -> Unit,
    onCreateRepo: (String, String, Boolean) -> Unit,
    onPushToGitHub: (String) -> Unit,
    onTriggerGeminiCompile: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var tokenInput by remember { mutableStateOf(uiState.githubState.token) }
    var commitMessage by remember { mutableStateOf(uiState.githubState.lastCommitMessage) }
    var showNewRepoFields by remember { mutableStateOf(false) }
    var newRepoName by remember { mutableStateOf("") }
    var newRepoDesc by remember { mutableStateOf("Built with Google AI Studio & Gemini") }
    var isPrivateRepo by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(20.dp),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF24292E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("GitHub Service Module", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = StudioLightTextPrimary)
                            Text("Autenticación y Push de Archivo ZIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioLightTextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioLightTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Success Banner
                    if (uiState.githubState.successMessage != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GeminiGreen.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = uiState.githubState.successMessage,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiGreen
                                    )
                                }
                            }
                        }
                    }

                    // Error Banner with Detailed Feedback & Action
                    if (uiState.githubState.errorMessage != null) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GeminiRed.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiRed.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("github_oauth_error_banner")
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.ErrorOutline,
                                                contentDescription = "Error",
                                                tint = GeminiRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Fallo en Callback / Autenticación OAuth2",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GeminiRed
                                            )
                                        }
                                        IconButton(
                                            onClick = onClearError,
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Descartar",
                                                tint = GeminiRed,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = uiState.githubState.errorMessage,
                                        fontSize = 11.5.sp,
                                        color = StudioLightTextPrimary,
                                        lineHeight = 15.sp
                                    )

                                    if (activity != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            OutlinedButton(
                                                onClick = { activity.let { onConnectOAuth(it) } },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GeminiRed),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiRed.copy(alpha = 0.5f)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.testTag("github_oauth_retry_button")
                                            ) {
                                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reintentar Conexión OAuth2", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 1. Authentication with GitHub Token
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioLightSurfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "1. Token de Acceso Personal (PAT / OAuth)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = tokenInput,
                                        onValueChange = { tokenInput = it },
                                        placeholder = { Text("ghp_xxxxxxxxxxxx", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GeminiBlue,
                                            unfocusedBorderColor = StudioLightBorder
                                        )
                                    )
                                    Button(
                                        onClick = { onConnectToken(tokenInput) },
                                        enabled = !uiState.githubState.isLoading,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        if (uiState.githubState.isLoading) {
                                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Text("Conectar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                HorizontalDivider(color = StudioLightBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 2.dp))

                                Button(
                                    onClick = { activity?.let { onConnectOAuth(it) } },
                                    enabled = !uiState.githubState.isLoading && activity != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("github_oauth2_firebase_button")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Iniciar Sesión OAuth2 GitHub (Firebase Auth)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Connected Account Info
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioLightSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                            shadowElevation = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (!uiState.githubState.userAvatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = uiState.githubState.userAvatarUrl,
                                            contentDescription = "Avatar",
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(GeminiPurple),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                uiState.githubState.username.take(1).uppercase(),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Column {
                                        Text("@${uiState.githubState.username}", fontWeight = FontWeight.Bold, color = StudioLightTextPrimary, fontSize = 13.sp)
                                        Text("Firestore & GitHub Sync Activo", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = GeminiGreen)
                                    }
                                }
                                IconButton(onClick = onRefreshRepos, modifier = Modifier.size(30.dp)) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh repos", tint = StudioLightTextSecondary)
                                }
                            }
                        }
                    }

                    // 3. User Repositories List & Selection
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("2. Repositorios de tu Cuenta", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioLightTextPrimary)
                                TextButton(onClick = { showNewRepoFields = !showNewRepoFields }) {
                                    Text(if (showNewRepoFields) "Cancelar nuevo" else "+ Crear nuevo repo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiBlue)
                                }
                            }

                            // Form for creating new repo
                            AnimatedVisibility(visible = showNewRepoFields) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = StudioLightSurfaceVariant.copy(alpha = 0.6f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = newRepoName,
                                            onValueChange = { newRepoName = it },
                                            label = { Text("Nombre del nuevo repositorio") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = newRepoDesc,
                                            onValueChange = { newRepoDesc = it },
                                            label = { Text("Descripción") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(checked = isPrivateRepo, onCheckedChange = { isPrivateRepo = it })
                                            Text("Repositorio privado", fontSize = 12.sp)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Button(
                                                onClick = {
                                                    if (newRepoName.isNotBlank()) {
                                                        onCreateRepo(newRepoName, newRepoDesc, isPrivateRepo)
                                                        showNewRepoFields = false
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                                            ) {
                                                Text("Crear en GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Repositories items
                    if (uiState.githubState.repositories.isNotEmpty()) {
                        items(uiState.githubState.repositories.take(6)) { repo ->
                            val isSelected = repo.name.equals(uiState.githubState.repoName, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GeminiBlue.copy(alpha = 0.1f) else StudioLightSurface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GeminiBlue else StudioLightBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectRepo(repo.name) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (repo.isPrivate) Icons.Default.Lock else Icons.Default.Folder,
                                            contentDescription = null,
                                            tint = if (isSelected) GeminiBlue else StudioLightTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Column {
                                            Text(
                                                text = repo.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) GeminiBlue else StudioLightTextPrimary
                                            )
                                            if (!repo.description.isNullOrBlank()) {
                                                Text(
                                                    text = repo.description,
                                                    fontSize = 10.sp,
                                                    color = StudioLightTextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                    if (isSelected) {
                                        Badge(containerColor = GeminiBlue) {
                                            Text("Seleccionado", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StudioLightSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Repositorio activo: ${uiState.githubState.repoName} (Rama: ${uiState.githubState.branch})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioLightTextSecondary,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    // 3. Gemini GitHub Actions Autonomous Compiler Banner
                    item {
                        val actionsState = uiState.githubState.actionsCompilationState
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GeminiPurple.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GeminiPurple.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = GeminiPurple,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.AutoAwesome,
                                                    contentDescription = "Gemini AI",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = "Compilación Autónoma en GitHub Actions",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.5.sp,
                                                color = StudioLightTextPrimary
                                            )
                                            Text(
                                                text = "Gemini lee el repositorio, gestiona workflows Yaml y auto-repara errores en vivo",
                                                fontSize = 10.5.sp,
                                                color = StudioLightTextSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = onTriggerGeminiCompile,
                                    enabled = !actionsState.isCompiling,
                                    colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("gemini_github_actions_compile_button")
                                ) {
                                    if (actionsState.isCompiling) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Gemini Compilando en GitHub...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    } else {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Compilar con Gemini (GitHub Actions)", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
                                    }
                                }

                                // Live Console Log Box
                                if (actionsState.logs.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E1E2E),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "CONSOLA EN VIVO DE GEMINI ACTIONS",
                                                    fontSize = 9.5.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = GeminiCyan
                                                )
                                                if (actionsState.runStatus == "repairing") {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = GeminiRed.copy(alpha = 0.2f)
                                                    ) {
                                                        Text(
                                                            text = "AUTO-REPARANDO",
                                                            fontSize = 8.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = GeminiRed,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            actionsState.logs.takeLast(5).forEach { logLine ->
                                                Text(
                                                    text = logLine,
                                                    fontSize = 10.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = when {
                                                        logLine.contains("ERROR", ignoreCase = true) || logLine.contains("Failed", ignoreCase = true) -> GeminiRed
                                                        logLine.contains("SUCCESS", ignoreCase = true) || logLine.contains("✅") -> GeminiGreen
                                                        logLine.contains("Gemini") || logLine.contains("🤖") || logLine.contains("🧠") -> GeminiCyan
                                                        else -> Color(0xFFCDD6F4)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Commit Message
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("4. Mensaje de Commit para Exportar Archivo ZIP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioLightTextPrimary)
                            OutlinedTextField(
                                value = commitMessage,
                                onValueChange = { commitMessage = it },
                                placeholder = { Text("feat: export project from AI Studio Build") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GeminiBlue,
                                    unfocusedBorderColor = StudioLightBorder
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Push Action Button
                Button(
                    onClick = { onPushToGitHub(commitMessage) },
                    enabled = !uiState.githubState.isPushing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("push_to_github_button")
                ) {
                    if (uiState.githubState.isPushing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportando ZIP & Pushing a GitHub...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Push Proyecto ZIP a GitHub", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
