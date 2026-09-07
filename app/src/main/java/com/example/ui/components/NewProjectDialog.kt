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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

data class ProjectTemplate(
    val name: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val themeColor: String,
    val defaultTitle: String,
    val description: String
)

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (name: String, category: String, themeColor: String, targetSdk: Int) -> Unit
) {
    val templates = listOf(
        ProjectTemplate("Crypto & Web3 Wallet", "Finance & Web3", Icons.Default.CurrencyBitcoin, "Purple", "CryptoPulse Wallet", "Billetera digital con cotizaciones en vivo y gráficos."),
        ProjectTemplate("Fitness & Smart Sensor", "Health & Fitness", Icons.Default.FitnessCenter, "Blue", "PulseFit AI Pro", "Monitoreo de actividad, pasos y telemetría."),
        ProjectTemplate("E-Commerce & Store", "E-Commerce", Icons.Default.ShoppingCart, "Green", "NovaShop Store", "Catálogo interactivo con carrito y checkout express."),
        ProjectTemplate("Productivity & GTD", "Productivity", Icons.Default.CheckCircle, "Blue", "ZenTask Flow Pro", "Gestor de tareas y hábitos con SQLite Room."),
        ProjectTemplate("AI Voice Assistant", "AI & Voice", Icons.Default.Mic, "Purple", "Aether Voice AI", "Asistente multimodal en tiempo real con Gemini Audio."),
        ProjectTemplate("Blank Compose App", "Custom Canvas", Icons.Default.Code, "Blue", "My Android App", "Estructura limpia Jetpack Compose M3 lista para crear.")
    )

    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    var projectName by remember { mutableStateOf(templates[0].defaultTitle) }
    var selectedTargetSdk by remember { mutableIntStateOf(36) }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AddBox, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Crear Nueva APK", fontWeight = FontWeight.ExtraBold, fontSize = 16.5.sp, color = StudioLightTextPrimary)
                            Text("Supervisado y compilado por Gemini", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiBlue)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioLightTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Nombre de la Aplicación (APK)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StudioLightTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioLightBackground,
                        unfocusedContainerColor = StudioLightBackground,
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = StudioLightBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Plantilla / Módulo Inicial", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StudioLightTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    items(templates.indices.toList()) { index ->
                        val template = templates[index]
                        val isSelected = selectedTemplateIndex == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GeminiBlue.copy(alpha = 0.08f) else StudioLightSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) GeminiBlue else StudioLightBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTemplateIndex = index
                                    projectName = template.defaultTitle
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        template.icon,
                                        contentDescription = null,
                                        tint = if (isSelected) GeminiBlue else StudioLightTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        template.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) GeminiBlue else StudioLightTextPrimary,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    template.category,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioLightTextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target SDK selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Target Android SDK", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = StudioLightTextPrimary)
                        Text("Firma Release Keystore v2+v3 automática", fontSize = 10.5.sp, color = StudioLightTextSecondary)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(36, 35, 34).forEach { sdk ->
                            val isSdkSelected = selectedTargetSdk == sdk
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSdkSelected) GeminiBlue else StudioLightSurfaceVariant,
                                modifier = Modifier.clickable { selectedTargetSdk = sdk }
                            ) {
                                Text(
                                    "API $sdk",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSdkSelected) Color.White else StudioLightTextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.Bold, color = StudioLightTextSecondary)
                    }

                    Button(
                        onClick = {
                            val sel = templates[selectedTemplateIndex]
                            onCreateProject(projectName.ifBlank { sel.defaultTitle }, sel.category, sel.themeColor, selectedTargetSdk)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Crear y Abrir APK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
