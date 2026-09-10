package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPanel(
    uiState: StudioUiState,
    onSendPrompt: (String) -> Unit,
    onToggleThinking: (String) -> Unit,
    onOpenAttachmentPicker: () -> Unit,
    onRemoveAttachment: (String) -> Unit,
    onQuickPromptSelected: (String) -> Unit,
    onViewInEmulator: () -> Unit,
    onAttachFile: (AttachedFile) -> Unit = {},
    onClearChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var promptInput by remember { mutableStateOf("") }
    var chatMenuExpanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // System File Picker Launcher for device explorer
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            var fileName = "dispositivo_archivo_${System.currentTimeMillis() % 1000}"
            var fileSize = 15000L
            try {
                context.contentResolver.query(selectedUri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }
            } catch (_: Exception) {}

            val ext = fileName.substringAfterLast('.', "bin").lowercase()
            val isImg = ext in listOf("png", "jpg", "jpeg", "webp", "gif", "svg")
            onAttachFile(
                AttachedFile(
                    id = UUID.randomUUID().toString(),
                    name = fileName,
                    sizeBytes = fileSize,
                    extension = ext,
                    isImage = isImg
                )
            )
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.isAgentGenerating) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioLightBackground)
    ) {

        // Chat toolbar: model + overflow menu (clear chat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Chat IA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = StudioLightTextPrimary
                )
                Text(
                    text = uiState.activeModel.name,
                    fontSize = 11.sp,
                    color = StudioLightTextSecondary
                )
            }
            Box {
                IconButton(onClick = { chatMenuExpanded = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Menú del chat",
                        tint = StudioLightTextPrimary
                    )
                }
                DropdownMenu(
                    expanded = chatMenuExpanded,
                    onDismissRequest = { chatMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Vaciar chat") },
                        onClick = {
                            chatMenuExpanded = false
                            onClearChat()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
        HorizontalDivider(color = StudioLightBorder.copy(alpha = 0.6f))

        // Componente de interfaz de estado sobre la interfaz de chat en tiempo real
        AnimatedVisibility(
            visible = uiState.isAgentGenerating || uiState.agentProcessingState.isActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            GeminiLiveProcessingBar(
                processingState = uiState.agentProcessingState,
                activeModelName = uiState.activeModel.name
            )
        }

        // Chat messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 14.dp)
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    onToggleThinking = { onToggleThinking(message.id) },
                    onViewInEmulator = onViewInEmulator
                )
            }

            // Real-time animated Google AI Studio Processing Status Indicator
            if (uiState.isAgentGenerating || uiState.agentProcessingState.isActive) {
                item {
                    AiProcessingStatusIndicator(
                        processingState = uiState.agentProcessingState,
                        activeModelName = uiState.activeModel.name
                    )
                }
            }
        }

        // Attached files preview strip
        if (uiState.attachedFiles.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StudioSurface)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.attachedFiles, key = { it.id }) { file ->
                    val borderColor = when (file.uploadStatus) {
                        UploadStatus.SUCCESS -> Color(0xFF22C55E)
                        UploadStatus.ERROR -> Color(0xFFEF4444)
                        UploadStatus.UPLOADING -> GeminiBlue
                        else -> GeminiBlue.copy(alpha = 0.4f)
                    }
                    val bgColor = when (file.uploadStatus) {
                        UploadStatus.SUCCESS -> Color(0xFF22C55E).copy(alpha = 0.12f)
                        UploadStatus.ERROR -> Color(0xFFEF4444).copy(alpha = 0.12f)
                        else -> GeminiBlue.copy(alpha = 0.15f)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = bgColor,
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (file.uploadStatus) {
                                    UploadStatus.UPLOADING -> {
                                        CircularProgressIndicator(
                                            progress = { file.uploadProgress },
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = GeminiCyan
                                        )
                                    }
                                    UploadStatus.SUCCESS -> {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Uploaded",
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    UploadStatus.ERROR -> {
                                        Icon(
                                            imageVector = Icons.Default.Error,
                                            contentDescription = "Error",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = if (file.isImage) Icons.Default.Image else Icons.Default.InsertDriveFile,
                                            contentDescription = null,
                                            tint = GeminiCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = file.name,
                                        fontSize = 11.sp,
                                        color = TextPrimary,
                                        maxLines = 1
                                    )
                                    when (file.uploadStatus) {
                                        UploadStatus.UPLOADING -> {
                                            Text(
                                                text = "Subiendo… ${(file.uploadProgress * 100).toInt()}%",
                                                fontSize = 9.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        UploadStatus.SUCCESS -> {
                                            Text(
                                                text = "Subido correctamente",
                                                fontSize = 9.sp,
                                                color = Color(0xFF22C55E)
                                            )
                                        }
                                        UploadStatus.ERROR -> {
                                            Text(
                                                text = file.uploadError ?: "Error al subir",
                                                fontSize = 9.sp,
                                                color = Color(0xFFEF4444),
                                                maxLines = 1
                                            )
                                        }
                                        else -> {}
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = TextSecondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { onRemoveAttachment(file.id) }
                                )
                            }
                            if (file.uploadStatus == UploadStatus.UPLOADING) {
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { file.uploadProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = GeminiCyan,
                                    trackColor = GeminiBlue.copy(alpha = 0.2f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Attach button (+)
                IconButton(
                    onClick = onOpenAttachmentPicker,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(StudioLightSurfaceVariant)
                        .testTag("attach_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach Files or Assets",
                        tint = GeminiBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Text Input
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = {
                        Text(
                            "Escribe a Gemini lo que deseas construir o modificar...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioLightTextMuted
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prompt_input_field"),
                    maxLines = 4,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioLightBackground,
                        unfocusedContainerColor = StudioLightBackground,
                        focusedBorderColor = GeminiBlue,
                        unfocusedBorderColor = StudioLightBorder,
                        focusedTextColor = StudioLightTextPrimary,
                        unfocusedTextColor = StudioLightTextPrimary
                    )
                )

                // Send Button
                val isSendEnabled = promptInput.isNotBlank() && !uiState.isAgentGenerating
                IconButton(
                    onClick = {
                        if (isSendEnabled) {
                            val textToSend = promptInput
                            promptInput = ""
                            onSendPrompt(textToSend)
                        }
                    },
                    enabled = isSendEnabled,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSendEnabled) Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))
                            else Brush.linearGradient(listOf(StudioLightSurfaceVariant, StudioLightSurfaceVariant))
                        )
                        .testTag("send_prompt_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send prompt",
                        tint = if (isSendEnabled) Color.White else StudioLightTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onToggleThinking: () -> Unit,
    onViewInEmulator: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    if (message.isUser) {
        // User bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp).copy(bottomEnd = CornerSize(4.dp)),
                color = GeminiBlue,
                modifier = Modifier.widthIn(max = 320.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Attached file badges in user message
                    if (message.attachedFiles.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            message.attachedFiles.forEach { f ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        "📎 ${f.name}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    } else {
        // AI Architect Bubble
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header with Gemini Icon & Model badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "Gemini Architect",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = StudioLightTextPrimary
                    )
                    if (message.modelUsed != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = GeminiBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = message.modelUsed.replace("gemini-", "").replace("-preview", ""),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Thinking accordion
                if (!message.thinkingText.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, StudioLightBorder, RoundedCornerShape(12.dp)),
                        color = StudioLightSurface,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = onToggleThinking),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Psychology,
                                        contentDescription = null,
                                        tint = GeminiPurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Thought Process & Architectural Plan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioLightTextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = if (message.isThinkingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = StudioLightTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(visible = message.isThinkingExpanded) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    HorizontalDivider(color = StudioLightBorderLight)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = message.thinkingText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = StudioLightTextSecondary,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Tool Actions execution progress logs
                if (message.toolActions.isNotEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = StudioLightSurfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            message.toolActions.forEach { tool ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GeminiGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = tool.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioLightTextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Explanation text
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SelectionContainer {
                        Text(
                            text = message.text,
                            color = StudioLightTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Code Snippets generated with Copy & Preview action
                message.codeSnippets.forEach { snippet ->
                    CodeSnippetCard(
                        snippet = snippet,
                        onCopy = { clipboardManager.setText(AnnotatedString(snippet.code)) },
                        onViewInEmulator = onViewInEmulator
                    )
                }

                // Build status indicator pill
                if (message.buildStatus != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GeminiGreen.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GeminiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.buildStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodeSnippetCard(
    snippet: CodeSnippet,
    onCopy: () -> Unit,
    onViewInEmulator: () -> Unit
) {
    var isCopied by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, StudioBorder, RoundedCornerShape(12.dp)),
        color = Color(0xFF0D1117)
    ) {
        Column {
            // Header: file name, language, copy & run buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161B22))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = GeminiCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = snippet.filename,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Preview button
                    TextButton(
                        onClick = onViewInEmulator,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Live Preview", fontSize = 11.sp, color = GeminiGreen)
                    }

                    // Copy button
                    TextButton(
                        onClick = {
                            onCopy()
                            isCopied = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = if (isCopied) GeminiGreen else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "Copied" else "Copy",
                            fontSize = 11.sp,
                            color = if (isCopied) GeminiGreen else TextSecondary
                        )
                    }
                }
            }

            // Code Content
            SelectionContainer {
                Text(
                    text = snippet.code,
                    color = TextCode,
                    fontSize = 11.5.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(12.dp),
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
fun AgentGeneratingIndicator(activeModel: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StudioSurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = GeminiCyan,
                strokeWidth = 2.dp
            )
            Column {
                Text(
                    text = "Gemini Architect is building your app...",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextPrimary
                )
                Text(
                    text = "Synthesizing Compose UI tree & compiling incremental APK...",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
