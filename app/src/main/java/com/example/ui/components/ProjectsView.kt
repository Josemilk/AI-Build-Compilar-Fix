package com.example.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudioApkProject
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@Composable
fun ProjectsView(
    uiState: StudioUiState,
    projects: List<StudioApkProject>,
    activeProjectId: String,
    onSelectProject: (StudioApkProject) -> Unit,
    onOpenNewProjectDialog: () -> Unit,
    onDownloadApk: (StudioApkProject) -> Unit,
    onDeleteProject: (StudioApkProject) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedApkId by remember { mutableStateOf<String?>(null) }
    var projectToDelete by remember { mutableStateOf<StudioApkProject?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioLightBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Header & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Mis Proyectos & APKs",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = StudioLightTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GeminiBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${projects.size} Construidas",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GeminiBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = "Toca cualquier proyecto para abrirlo y seguir editándolo en la pantalla principal",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = StudioLightTextSecondary
                )
            }

            Button(
                onClick = onOpenNewProjectDialog,
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("create_new_apk_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Nueva APK", fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Gemini Supervisor Info Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GeminiPurple.copy(alpha = 0.3f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GeminiPurple, GeminiBlue))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ecosistema Supervisado por Gemini AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = StudioLightTextPrimary
                    )
                    Text(
                        text = "Gemini audita el código Kotlin, sincroniza el emulador en streaming y firma cada APK con Release Keystore V2+V3.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = StudioLightTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Projects List
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(projects, key = { it.id }) { project ->
                val isActive = project.id == activeProjectId || project.name.equals(uiState.projectName, ignoreCase = true)
                val iconVector = when (project.iconName) {
                    "fitness" -> Icons.Default.FitnessCenter
                    "crypto" -> Icons.Default.CurrencyBitcoin
                    "shop" -> Icons.Default.ShoppingCart
                    "task" -> Icons.Default.CheckCircle
                    else -> Icons.Default.Android
                }
                val iconBg = when (project.themeColor) {
                    "Purple" -> Brush.linearGradient(listOf(GeminiPurple, GeminiBlue))
                    "Green" -> Brush.linearGradient(listOf(GeminiGreen, AndroidAccent))
                    else -> Brush.linearGradient(listOf(GeminiBlue, GeminiCyan))
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isActive) GeminiBlue.copy(alpha = 0.04f) else StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isActive) 1.5.dp else 1.dp,
                        color = if (isActive) GeminiBlue else StudioLightBorder
                    ),
                    shadowElevation = if (isActive) 3.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onSelectProject(project) }
                        .testTag("project_item_${project.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Top info row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(iconBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(iconVector, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = project.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = StudioLightTextPrimary
                                        )
                                        if (isActive) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = GeminiBlue
                                            ) {
                                                Text(
                                                    text = "ACTIVO",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = project.packageName,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = StudioLightTextMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Signed Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = GeminiGreen.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = "APK Firmada",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = GeminiGreen
                                        )
                                    }
                                }

                                // Delete Project Button
                                IconButton(
                                    onClick = { projectToDelete = project },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("delete_project_button_${project.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar proyecto",
                                        tint = GeminiRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description
                        Text(
                            text = project.description,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioLightTextSecondary,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Meta details row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioLightSurfaceVariant
                            ) {
                                Text(
                                    text = "Android 16 • API ${project.targetSdk}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioLightSurfaceVariant
                            ) {
                                Text(
                                    text = "${project.apkSizeMb} MB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = StudioLightSurfaceVariant
                            ) {
                                Text(
                                    text = project.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Abrir en Pantalla Principal / Descargar APK
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onSelectProject(project) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) GeminiBlue else StudioLightSurfaceVariant
                                )
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = if (isActive) Color.White else StudioLightTextPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isActive) "Editando en Principal" else "Abrir y Seguir Editando",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (isActive) Color.White else StudioLightTextPrimary
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(uiState.buildExportState.cloudPreviewUrl))
                                    copiedApkId = project.id
                                    onDownloadApk(project)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (copiedApkId == project.id) GeminiGreen else GeminiBlue
                                )
                            ) {
                                Icon(
                                    if (copiedApkId == project.id) Icons.Default.Check else Icons.Default.FileDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (copiedApkId == project.id) "APK Lista" else "Descargar APK",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { proj ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GeminiRed)
                    Text("Eliminar Proyecto", fontWeight = FontWeight.Bold, color = StudioLightTextPrimary)
                }
            },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar permanentemente '${proj.name}' del ecosistema y de tu cuenta en Firestore?",
                    fontSize = 13.sp,
                    color = StudioLightTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(proj)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiRed)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancelar", color = StudioLightTextSecondary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = StudioLightSurface
        )
    }
}
