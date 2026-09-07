package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiModelInfo
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioTab
import com.example.ui.viewmodel.StudioUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioHeader(
    uiState: StudioUiState,
    onTabSelected: (StudioTab) -> Unit,
    onReloadPreview: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenExport: () -> Unit,
    onSelectModel: (String) -> Unit
) {
    var isModelMenuExpanded by remember { mutableStateOf(false) }

    val reloadTransition = rememberInfiniteTransition(label = "reload_anim")
    val reloadRotation by reloadTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioLightSurface)
            .border(width = 1.dp, color = StudioLightBorder, shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Row: Brand & Model Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Brand Logo & App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(GeminiBlue, GeminiPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Studio Logo",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "AI Studio",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = StudioLightTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GeminiBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "BUILD",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GeminiBlue,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = uiState.projectName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioLightTextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Right: Model Selector Dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .clickable { isModelMenuExpanded = true }
                        .testTag("model_selector_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        val quotaColor = if (uiState.activeModel.isQuotaExhausted) GeminiRed else GeminiGreen
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(quotaColor)
                        )
                        Text(
                            text = uiState.activeModel.name.replace("Gemini ", ""),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioLightTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Model",
                            tint = StudioLightTextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isModelMenuExpanded,
                    onDismissRequest = { isModelMenuExpanded = false },
                    modifier = Modifier
                        .background(StudioLightSurface)
                        .border(1.dp, StudioLightBorder, RoundedCornerShape(12.dp))
                ) {
                    Text(
                        text = "Modelo Gemini Activo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioLightTextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                    HorizontalDivider(color = StudioLightBorder)

                    uiState.models.forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            model.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (model.id == uiState.selectedModelId) GeminiBlue else StudioLightTextPrimary
                                        )
                                        if (model.isDefault) {
                                            Badge(containerColor = GeminiBlue.copy(alpha = 0.15f)) {
                                                Text("Default", color = GeminiBlue, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        "Cuota: ${model.remainingQuota}/${model.maxDailyQuota}",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (model.isQuotaExhausted) GeminiRed else StudioLightTextSecondary
                                    )
                                }
                            },
                            onClick = {
                                onSelectModel(model.id)
                                isModelMenuExpanded = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (model.id == uiState.selectedModelId) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (model.id == uiState.selectedModelId) GeminiBlue else StudioLightTextMuted
                                )
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Toolbar: Rueda de Ajustes, GitHub Sync, Exportar ZIP/APK, Recargar Emulador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Rueda de Ajustes (Configuración)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StudioLightSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                modifier = Modifier
                    .clickable(onClick = onOpenSettings)
                    .testTag("settings_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración y Ajustes",
                        tint = StudioLightTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Ajustes",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioLightTextPrimary
                    )
                }
            }

            // 2. GitHub Sync / Export
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF24292E).copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF24292E).copy(alpha = 0.25f)),
                modifier = Modifier
                    .clickable(onClick = onOpenGitHub)
                    .testTag("github_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "GitHub Sync",
                        tint = if (uiState.githubState.isConnected) GeminiGreen else StudioLightTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "GitHub",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioLightTextPrimary
                    )
                }
            }

            // 3. Exportar ZIP & APK
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GeminiAmber.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.5f)),
                modifier = Modifier
                    .clickable(onClick = onOpenExport)
                    .testTag("export_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Exportar ZIP & APK",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Exportar ZIP/APK",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }

            // 4. Recargar Emulador Live
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GeminiBlue.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue.copy(alpha = 0.3f)),
                modifier = Modifier
                    .clickable(onClick = onReloadPreview)
                    .testTag("reload_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Recargar Emulador",
                        tint = GeminiBlue,
                        modifier = if (uiState.isReloadingPreview) Modifier.size(15.dp).rotate(reloadRotation) else Modifier.size(15.dp)
                    )
                    Text(
                        text = "Recargar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeminiBlue
                    )
                }
            }
        }

        // Quota Warning Banner if any
        AnimatedVisibility(visible = uiState.quotaWarningMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(8.dp),
                color = GeminiAmber.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.4f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = GeminiAmber, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiState.quotaWarningMessage ?: "",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E),
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation Tabs: Chat, Preview, Código, Proyectos, Exportar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            StudioTabItem(
                label = "Chat",
                icon = Icons.Default.ChatBubbleOutline,
                isSelected = uiState.activeTab == StudioTab.CHAT,
                onClick = { onTabSelected(StudioTab.CHAT) }
            )
            StudioTabItem(
                label = "Preview",
                icon = Icons.Default.PhoneAndroid,
                isSelected = uiState.activeTab == StudioTab.PREVIEW,
                badge = "Live",
                onClick = { onTabSelected(StudioTab.PREVIEW) }
            )
            StudioTabItem(
                label = "Código",
                icon = Icons.Default.IntegrationInstructions,
                isSelected = uiState.activeTab == StudioTab.CODE,
                onClick = { onTabSelected(StudioTab.CODE) }
            )
            StudioTabItem(
                label = "Proyectos",
                icon = Icons.Default.FolderSpecial,
                isSelected = uiState.activeTab == StudioTab.PROJECTS,
                badge = "${uiState.projects.size}",
                onClick = { onTabSelected(StudioTab.PROJECTS) }
            )
            StudioTabItem(
                label = "Exportar",
                icon = Icons.Default.FileDownload,
                isSelected = uiState.activeTab == StudioTab.EXPORT,
                onClick = { onTabSelected(StudioTab.EXPORT) }
            )
        }
    }
}

@Composable
fun StudioTabItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badge: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgGradient = if (isSelected) {
        Brush.horizontalGradient(listOf(GeminiBlue.copy(alpha = 0.15f), GeminiPurple.copy(alpha = 0.12f)))
    } else {
        Brush.horizontalGradient(listOf(StudioLightSurface, StudioLightSurface))
    }
    val borderColor = if (isSelected) GeminiBlue else StudioLightBorder

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp)),
        color = Color.Transparent,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(bgGradient)
                .padding(vertical = 7.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) GeminiBlue else StudioLightTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) GeminiBlue else StudioLightTextSecondary,
                    maxLines = 1
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AndroidAccent)
                    )
                }
            }
        }
    }
}
