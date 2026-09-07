package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@Composable
fun ExportDialog(
    uiState: StudioUiState,
    onDismiss: () -> Unit,
    onStartBuildApk: () -> Unit,
    onSelectCompileTarget: (com.example.data.model.CompileTarget) -> Unit = {},
    onOpenApkUrl: (String) -> Unit = {},
    onOpenNewProjectDialog: () -> Unit = {},
    onOpenGitHubDialog: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var isZipDownloaded by remember { mutableStateOf(false) }
    var isApkDownloaded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
            shadowElevation = 10.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Exportar Proyecto", fontWeight = FontWeight.ExtraBold, fontSize = 16.5.sp, color = StudioLightTextPrimary)
                            Text("ZIP de Código • GitHub Sync • APK Firmada", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioLightTextSecondary)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioLightTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Option 1: Download Project Source Code ZIP
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GeminiPurple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FolderZip, contentDescription = null, tint = GeminiPurple, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Código Fuente (.ZIP)", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = StudioLightTextPrimary)
                                Text("${uiState.projectName.lowercase().replace(" ", "_")}.zip • ${uiState.buildExportState.zipSizeMb} MB", fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = StudioLightTextSecondary)
                            }
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(uiState.buildExportState.cloudPreviewUrl))
                                isZipDownloaded = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isZipDownloaded) GeminiGreen else GeminiPurple
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isZipDownloaded) Icons.Default.Check else Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isZipDownloaded) "Descargado" else "Descargar ZIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Export / Push to GitHub
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF24292E).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF24292E), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text("Exportar a GitHub", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = StudioLightTextPrimary)
                                Text("github.com/${uiState.githubState.username}/${uiState.githubState.repoName}", fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = StudioLightTextSecondary, maxLines = 1)
                            }
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onOpenGitHubDialog()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF24292E)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Abrir GitHub", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))


                // Option 3: Compile (GitHub Actions / Google Cloud) + Download APK
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Compilar APK",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = StudioLightTextPrimary
                        )
                        Text(
                            "Elige el backend, observa el proceso y descarga el APK al terminar",
                            fontSize = 10.5.sp,
                            color = StudioLightTextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val githubSelected = uiState.buildExportState.selectedTarget == com.example.data.model.CompileTarget.GITHUB_ACTIONS
                        val gcpSelected = uiState.buildExportState.selectedTarget == com.example.data.model.CompileTarget.GOOGLE_CLOUD_BUILD

                        // Target selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = githubSelected,
                                onClick = { onSelectCompileTarget(com.example.data.model.CompileTarget.GITHUB_ACTIONS) },
                                label = { Text("GitHub Actions", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Code, null, modifier = Modifier.size(14.dp))
                                },
                                enabled = !uiState.buildExportState.isBuilding
                            )
                            FilterChip(
                                selected = gcpSelected,
                                onClick = { onSelectCompileTarget(com.example.data.model.CompileTarget.GOOGLE_CLOUD_BUILD) },
                                label = { Text("Google Cloud", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Cloud, null, modifier = Modifier.size(14.dp))
                                },
                                enabled = !uiState.buildExportState.isBuilding
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (uiState.buildExportState.apkGenerated) "APK generada" else "Listo para compilar",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = StudioLightTextPrimary
                                )
                                Text(
                                    text = uiState.buildExportState.currentStep.ifBlank {
                                        if (githubSelected) "Usa el repo de GitHub + Actions"
                                        else "Usa GCP Project + OAuth token"
                                    },
                                    fontSize = 10.sp,
                                    color = StudioLightTextSecondary,
                                    maxLines = 2
                                )
                            }
                            Button(
                                onClick = {
                                    if (uiState.buildExportState.apkGenerated && !uiState.buildExportState.isBuilding) {
                                        val url = uiState.buildExportState.apkDownloadUrl
                                            ?: uiState.buildExportState.cloudPreviewUrl
                                        if (url.isNotBlank()) {
                                            isApkDownloaded = true
                                            onOpenApkUrl(url)
                                        }
                                    } else {
                                        onStartBuildApk()
                                    }
                                },
                                enabled = !uiState.buildExportState.isBuilding,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        isApkDownloaded -> GeminiGreen
                                        uiState.buildExportState.apkGenerated -> GeminiBlue
                                        else -> GeminiPurple
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("build_apk_button")
                            ) {
                                if (uiState.buildExportState.isBuilding) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Compilando...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else if (uiState.buildExportState.apkGenerated) {
                                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Descargar APK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Compilar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Live progress + didactic logs
                        AnimatedVisibility(visible = uiState.buildExportState.isBuilding || uiState.buildExportState.processLogs.isNotEmpty()) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                if (uiState.buildExportState.isBuilding) {
                                    LinearProgressIndicator(
                                        progress = { uiState.buildExportState.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = GeminiBlue,
                                        trackColor = StudioLightSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            "Proceso en vivo",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        uiState.buildExportState.processLogs.takeLast(12).forEach { line ->
                                            Text(
                                                text = line,
                                                fontSize = 9.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFFE2E8F0),
                                                lineHeight = 12.sp
                                            )
                                        }
                                    }
                                }
                                if (uiState.buildExportState.apkGenerated && !uiState.buildExportState.apkDownloadUrl.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "✅ Compilación terminada — pulsa Descargar APK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GeminiGreen
                                    )
                                }
                                uiState.buildExportState.lastError?.let { err ->
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(err, fontSize = 10.sp, color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenNewProjectDialog()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = GeminiBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Nueva APK", color = GeminiBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StudioLightSurfaceVariant)
                    ) {
                        Text("Cerrar", color = StudioLightTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

