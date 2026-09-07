package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GoogleServicesState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleServicesConfigTool(
    googleServicesState: GoogleServicesState,
    onJsonInputChange: (String) -> Unit,
    onValidate: (String) -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StudioSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GeminiBlue.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = GeminiBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Validador de google-services.json (Firebase)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = StudioLightTextPrimary
                    )
                    Text(
                        text = "Comprueba y vincula la configuración oficial de Firebase para tu paquete Android.",
                        fontSize = 11.sp,
                        color = StudioLightTextSecondary
                    )
                }
            }

            // Application ID Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = StudioBackground,
                border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAmber.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Application ID de tu APK:",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioLightTextSecondary
                        )
                        Text(
                            text = googleServicesState.appId,
                            fontSize = 12.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GeminiAmber
                        )
                    }

                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(googleServicesState.appId))
                            copiedNotice = true
                        }
                    ) {
                        Icon(
                            imageVector = if (copiedNotice) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copiar ID",
                            tint = if (copiedNotice) GeminiGreen else StudioLightTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (copiedNotice) {
                Text(
                    text = "¡ID copiado al portapapeles! Úsalo al registrar la app en Firebase Console.",
                    fontSize = 10.5.sp,
                    color = GeminiGreen,
                    fontWeight = FontWeight.Medium
                )
            }

            // Textarea for pasting google-services.json
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Pega aquí el contenido de google-services.json:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioLightTextPrimary
                )

                OutlinedTextField(
                    value = googleServicesState.jsonInput,
                    onValueChange = onJsonInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = {
                        Text(
                            "{\n  \"project_info\": { \"project_id\": \"...\" },\n  \"client\": [ ... ]\n}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = StudioLightTextSecondary.copy(alpha = 0.5f)
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioLightTextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = StudioLightBorder
                    )
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onValidate(googleServicesState.jsonInput) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Validar JSON", fontSize = 12.sp)
                }

                Button(
                    onClick = { onSave(googleServicesState.jsonInput) },
                    enabled = googleServicesState.validationResult?.isValid == true,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar en Proyecto", fontSize = 12.sp)
                }
            }

            // Save status feedback message
            if (googleServicesState.saveMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (googleServicesState.isSaved) GeminiGreen.copy(alpha = 0.1f) else GeminiAmber.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (googleServicesState.isSaved) GeminiGreen.copy(alpha = 0.3f) else GeminiAmber.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = googleServicesState.saveMessage,
                        fontSize = 11.sp,
                        color = if (googleServicesState.isSaved) GeminiGreen else GeminiAmber,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Validation Results Detailed Card
            val result = googleServicesState.validationResult
            if (result != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (result.isValid) GeminiGreen.copy(alpha = 0.06f) else GeminiRed.copy(alpha = 0.06f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (result.isValid) GeminiGreen.copy(alpha = 0.3f) else GeminiRed.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (result.isValid) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.isValid) GeminiGreen else GeminiRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = result.statusMessage,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = if (result.isValid) GeminiGreen else GeminiRed
                            )
                        }

                        Divider(color = StudioLightBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

                        if (result.projectId != null) {
                            Text(
                                text = "• Project ID: ${result.projectId}",
                                fontSize = 11.sp,
                                color = StudioLightTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (result.projectNumber != null) {
                            Text(
                                text = "• Project Number: ${result.projectNumber}",
                                fontSize = 11.sp,
                                color = StudioLightTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (result.storageBucket != null) {
                            Text(
                                text = "• Storage Bucket: ${result.storageBucket}",
                                fontSize = 11.sp,
                                color = StudioLightTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        if (result.apiKey != null) {
                            Text(
                                text = "• API Key detectada: ${result.apiKey.take(8)}••••••••",
                                fontSize = 11.sp,
                                color = StudioLightTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (result.detectedPackageNames.isNotEmpty()) {
                            Text(
                                text = "• Paquetes en JSON: ${result.detectedPackageNames.joinToString(", ")}",
                                fontSize = 10.5.sp,
                                color = StudioLightTextSecondary
                            )
                        }

                        if (result.errors.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Detalles de errores:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiRed
                                )
                                result.errors.forEach { err ->
                                    Text(
                                        text = " - $err",
                                        fontSize = 10.5.sp,
                                        color = GeminiRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
