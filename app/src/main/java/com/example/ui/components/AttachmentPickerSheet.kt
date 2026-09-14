package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Sheet for attaching a real Android project ZIP (or any file).
 * No fake/preset files — opens the system picker only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentPickerSheet(
    onDismiss: () -> Unit,
    onFileSelected: (com.example.data.model.AttachedFile) -> Unit = {},
    onLaunchSystemFilePicker: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = StudioLightSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Subir proyecto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = StudioLightTextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = StudioLightTextSecondary)
                }
            }

            Text(
                "Selecciona un ZIP con el código Android (carpetas app/, gradle/, etc.). Se descomprime en el dispositivo y la IA puede leer y editar esos archivos.",
                fontSize = 13.sp,
                color = StudioLightTextSecondary,
                lineHeight = 18.sp
            )

            Button(
                onClick = {
                    onLaunchSystemFilePicker()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier = Modifier.width(8.dp))
                Text("Elegir archivo ZIP del dispositivo", fontWeight = FontWeight.Bold)
            }

            Text(
                "También puedes adjuntar imágenes o archivos sueltos; el ZIP de proyecto es el flujo principal para compilar.",
                fontSize = 11.sp,
                color = StudioLightTextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
