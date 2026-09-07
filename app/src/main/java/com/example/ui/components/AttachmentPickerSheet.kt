package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttachedFile
import com.example.ui.theme.*
import java.util.UUID

data class AttachmentPreset(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint: Color,
    val file: AttachedFile
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onFileSelected: (AttachedFile) -> Unit,
    onLaunchSystemFilePicker: () -> Unit = {}
) {
    val presets = listOf(
        AttachmentPreset(
            title = "Captura de Diseño / Layout",
            description = "Especificación visual y layout",
            icon = Icons.Default.Image,
            iconTint = GeminiBlue,
            file = AttachedFile(
                id = UUID.randomUUID().toString(),
                name = "layout_spec.png",
                sizeBytes = 245000,
                extension = "png",
                isImage = true
            )
        ),
        AttachmentPreset(
            title = "Componente Kotlin Composable",
            description = "Estructura Jetpack Compose",
            icon = Icons.Default.Code,
            iconTint = GeminiBlue,
            file = AttachedFile(
                id = UUID.randomUUID().toString(),
                name = "CustomComponent.kt",
                sizeBytes = 14200,
                extension = "kt"
            )
        ),
        AttachmentPreset(
            title = "JSON API Data Schema",
            description = "REST response payload",
            icon = Icons.Default.DataArray,
            iconTint = GeminiAmber,
            file = AttachedFile(
                id = UUID.randomUUID().toString(),
                name = "api_schema.json",
                sizeBytes = 32000,
                extension = "json"
            )
        ),
        AttachmentPreset(
            title = "App Theme Colors",
            description = "Design system tokens",
            icon = Icons.Default.Palette,
            iconTint = GeminiPurple,
            file = AttachedFile(
                id = UUID.randomUUID().toString(),
                name = "theme_tokens.xml",
                sizeBytes = 8900,
                extension = "xml"
            )
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioLightSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StudioLightBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 30.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AttachFile, contentDescription = null, tint = GeminiBlue)
                Text(
                    text = "Subir Archivo al Contexto de Gemini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = StudioLightTextPrimary
                )
            }
            Text(
                text = "Selecciona cualquier archivo del dispositivo para que Gemini lo analice y genere código adaptado.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = StudioLightTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            // Primary Big Button: Open Device File Explorer
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GeminiBlue.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GeminiBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onLaunchSystemFilePicker()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GeminiBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Abrir Explorador de Archivos",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = GeminiBlue
                        )
                        Text(
                            text = "Sube imágenes, código Kotlin, JSON, XML, PDF o cualquier documento del dispositivo.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioLightTextSecondary
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GeminiBlue)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "O elige una plantilla rápida:",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = StudioLightTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = StudioLightSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFileSelected(preset.file)
                                onDismiss()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(preset.iconTint.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(preset.icon, contentDescription = null, tint = preset.iconTint, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(preset.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioLightTextPrimary)
                            Text(preset.description, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = StudioLightTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
