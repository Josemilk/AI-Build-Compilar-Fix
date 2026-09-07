package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectFile
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@Composable
fun CodeEditorView(
    uiState: StudioUiState,
    onSelectFile: (Int) -> Unit,
    onUpdateFileContent: (Int, String) -> Unit,
    onApplyAndRecompile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val currentFile = uiState.currentFile
    var isCopied by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBackground)
            .padding(12.dp)
    ) {
        // File Tabs Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurface)
                .border(1.dp, StudioBorder, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(uiState.projectFiles) { index, file ->
                val isSelected = index == uiState.activeFileIndex
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSelected) StudioSurfaceCard else Color.Transparent,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GeminiBlue) else null,
                    modifier = Modifier.clickable { onSelectFile(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = when (file.extension) {
                                "kt" -> Icons.Default.Code
                                "xml" -> Icons.Default.Description
                                "kts" -> Icons.Default.Build
                                else -> Icons.Default.InsertDriveFile
                            },
                            contentDescription = null,
                            tint = if (isSelected) GeminiCyan else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = file.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                        if (file.isModified) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GeminiAmber)
                            )
                        }
                    }
                }
            }
        }

        // Action Toolbar (File path, Line count, Copy, Apply)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF131B2B),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentFile?.path ?: "",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy
                    TextButton(
                        onClick = {
                            currentFile?.content?.let { clipboardManager.setText(AnnotatedString(it)) }
                            isCopied = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (isCopied) GeminiGreen else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isCopied) "Copied" else "Copy", fontSize = 11.sp, color = if (isCopied) GeminiGreen else TextSecondary)
                    }

                    // Apply and Recompile button
                    Button(
                        onClick = onApplyAndRecompile,
                        colors = ButtonDefaults.buttonColors(containerColor = GeminiBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("apply_recompile_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Apply & Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Code Editor Canvas with Line Numbers
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            color = Color(0xFF0D1117),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
            shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
        ) {
            val content = currentFile?.content ?: ""
            val lines = content.lines()
            val verticalScroll = rememberScrollState()
            val horizontalScroll = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Line Numbers Column
                Column(
                    modifier = Modifier
                        .background(Color(0xFF090D14))
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    lines.forEachIndexed { idx, _ ->
                        Text(
                            text = "${idx + 1}",
                            color = Color(0xFF4B5563),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                VerticalDivider(color = StudioBorder)

                // Editable Code Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(12.dp)
                ) {
                    BasicTextField(
                        value = content,
                        onValueChange = { newText ->
                            onUpdateFileContent(uiState.activeFileIndex, newText)
                        },
                        textStyle = TextStyle(
                            color = TextCode,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(GeminiCyan),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
