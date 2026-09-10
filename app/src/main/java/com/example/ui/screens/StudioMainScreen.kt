package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AttachedFile
import com.example.ui.components.*
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioBorder
import com.example.ui.viewmodel.StudioTab
import com.example.ui.viewmodel.StudioViewModel
import java.util.UUID

@Composable
fun StudioMainScreen(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Global file picker launcher for device attachment
    val globalFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            var fileName = "dispositivo_archivo_${System.currentTimeMillis() % 1000}"
            var fileSize = 20000L
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
            // Project ZIP → load sources into the workspace for real compile/repair
            if (ext == "zip") {
                viewModel.importProjectZip(context, selectedUri)
            }
            viewModel.attachFile(
                AttachedFile(
                    id = UUID.randomUUID().toString(),
                    name = fileName,
                    sizeBytes = fileSize,
                    extension = ext,
                    isImage = isImg,
                    uriString = selectedUri.toString()
                )
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.StudioLightBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            Column(modifier = Modifier.fillMaxSize()) {
                // Main AI Studio Header & Navigation
                StudioHeader(
                    uiState = uiState,
                    onTabSelected = { viewModel.selectTab(it) },
                    onReloadPreview = { viewModel.triggerPreviewReload() },
                    onOpenSettings = { viewModel.setSettingsOpen(true) },
                    onOpenGitHub = { viewModel.setGitHubDialogOpen(true) },
                    onOpenExport = { viewModel.setExportDialogOpen(true) },
                    onSelectModel = { viewModel.selectModel(it) }
                )

                if (isWideScreen && uiState.activeTab != StudioTab.PROJECTS && uiState.activeTab != StudioTab.CODE) {
                    // Split screen for Tablets / Foldables / Large screens
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left: Chat & Agent Panel
                        ChatPanel(
                            uiState = uiState,
                            onSendPrompt = { viewModel.sendPrompt(it) },
                            onToggleThinking = { viewModel.toggleThinking(it) },
                            onOpenAttachmentPicker = { viewModel.setAttachmentPickerOpen(true) },
                            onRemoveAttachment = { viewModel.removeAttachedFile(it) },
                            onQuickPromptSelected = { viewModel.sendPrompt(it) },
                            onViewInEmulator = { viewModel.selectTab(StudioTab.PREVIEW) },
                            onAttachFile = { viewModel.attachFile(it) },
                            onClearChat = { viewModel.clearChat() },
                            modifier = Modifier.weight(0.48f)
                        )

                        VerticalDivider(color = com.example.ui.theme.StudioLightBorder, thickness = 1.dp)

                        // Right: Live Streaming Emulator
                        EmulatorView(
                            uiState = uiState,
                            onReload = { viewModel.triggerPreviewReload() },
                            onDeviceSkinChange = { viewModel.setDeviceSkin(it) },
                            onBoostMetric = { viewModel.boostDynamicMetric() },
                            onAddDynamicItem = { viewModel.addDynamicItem() },
                            modifier = Modifier.weight(0.52f)
                        )
                    }
                } else {
                    // Mobile Screen / Specific Tabbed View (Chat, Preview, Code, Projects, Export)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        when (uiState.activeTab) {
                            StudioTab.CHAT -> {
                                ChatPanel(
                                    uiState = uiState,
                                    onSendPrompt = { viewModel.sendPrompt(it) },
                                    onToggleThinking = { viewModel.toggleThinking(it) },
                                    onOpenAttachmentPicker = { viewModel.setAttachmentPickerOpen(true) },
                                    onRemoveAttachment = { viewModel.removeAttachedFile(it) },
                                    onQuickPromptSelected = { viewModel.sendPrompt(it) },
                                    onViewInEmulator = { viewModel.selectTab(StudioTab.PREVIEW) },
                                    onAttachFile = { viewModel.attachFile(it) },
                                    onClearChat = { viewModel.clearChat() }
                                )
                            }
                            StudioTab.PREVIEW -> {
                                EmulatorView(
                                    uiState = uiState,
                                    onReload = { viewModel.triggerPreviewReload() },
                                    onDeviceSkinChange = { viewModel.setDeviceSkin(it) },
                                    onBoostMetric = { viewModel.boostDynamicMetric() },
                                    onAddDynamicItem = { viewModel.addDynamicItem() },
                                    onDismissActionsOverlay = { viewModel.dismissActionsOverlay() },
                                    onTriggerGitHubActionsCompile = { viewModel.triggerWorkflowDispatchEvent() }
                                )
                            }
                            StudioTab.CODE -> {
                                CodeEditorView(
                                    uiState = uiState,
                                    onSelectFile = { viewModel.selectFile(it) },
                                    onUpdateFileContent = { index, content -> viewModel.updateFileContent(index, content) },
                                    onApplyAndRecompile = {
                                        viewModel.triggerPreviewReload()
                                        viewModel.selectTab(StudioTab.PREVIEW)
                                    }
                                )
                            }
                            StudioTab.PROJECTS -> {
                                ProjectsView(
                                    uiState = uiState,
                                    projects = uiState.projects,
                                    activeProjectId = uiState.activeProjectId,
                                    onSelectProject = { project -> viewModel.selectProject(project) },
                                    onOpenNewProjectDialog = { viewModel.setNewProjectDialogOpen(true) },
                                    onDownloadApk = { _ -> viewModel.startBuildApk() },
                                    onDeleteProject = { project -> viewModel.deleteProject(project) }
                                )
                            }
                            StudioTab.EXPORT -> {
                                ExportDialog(
                                    uiState = uiState,
                                    onDismiss = { viewModel.selectTab(StudioTab.CHAT) },
                                    onStartBuildApk = { viewModel.startBuildApk() },
                                    onSelectCompileTarget = { viewModel.setCompileTarget(it) },
                                    onOpenApkUrl = { url ->
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                        }
                                    },
                                    onOpenNewProjectDialog = { viewModel.setNewProjectDialogOpen(true) },
                                    onOpenGitHubDialog = { viewModel.setGitHubDialogOpen(true) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            uiState = uiState,
            onDismiss = { viewModel.setSettingsOpen(false) },
            onSelectModel = { viewModel.selectModel(it) },
            onSelectLlmProvider = { viewModel.selectLlmProvider(it) },
            onSelectMinSdk = { viewModel.setAndroidMinSdk(it) },
            onSaveApiKey = { viewModel.setCustomApiKey(it) },
            onSaveExternalApiKeys = { openAi, ant, ds, end -> viewModel.saveExternalApiKeys(openAi, ant, ds, end) },
            onSignInFirebase = { email, pass -> viewModel.signInWithFirebase(email, pass) },
            onSignUpFirebase = { email, pass, name -> viewModel.signUpWithFirebase(email, pass, name) },
            onSignOutFirebase = { viewModel.signOutFirebase() },
            onSyncFirestore = { viewModel.syncPreferencesToFirestore() },
            onAddOrUpdateExternalApiKey = { keyObj -> viewModel.addOrUpdateExternalApiKey(keyObj) },
            onDeleteExternalApiKey = { keyId -> viewModel.deleteExternalApiKey(keyId) },
            onConnectGcp = { token, proj -> viewModel.connectGoogleCloudAuth(token, proj) },
            onUpdateGoogleServicesJsonInput = { json -> viewModel.updateGoogleServicesJsonInput(json) },
            onValidateGoogleServicesJson = { json -> viewModel.validateGoogleServicesJson(json) },
            onSaveGoogleServicesJson = { json -> viewModel.saveGoogleServicesJson(json) }
        )
    }

    if (uiState.isGitHubDialogOpen) {
        GitHubSyncDialog(
            uiState = uiState,
            onDismiss = { viewModel.setGitHubDialogOpen(false) },
            onConnectToken = { token -> viewModel.connectGitHubToken(token) },
            onConnectOAuth = { activity -> viewModel.connectWithGitHubOAuth(activity) },
            onClearError = { viewModel.clearGitHubStatusMessages() },
            onRefreshRepos = { viewModel.loadGitHubRepositories() },
            onSelectRepo = { repo -> viewModel.selectGitHubRepository(repo) },
            onCreateRepo = { name, desc, isPrivate -> viewModel.createGitHubRepository(name, desc, isPrivate) },
            onPushToGitHub = { commitMsg -> viewModel.pushToGitHub(commitMsg) },
            onTriggerGeminiCompile = { viewModel.triggerWorkflowDispatchEvent() }
        )
    }

    if (uiState.isExportDialogOpen) {
        ExportDialog(
            uiState = uiState,
            onDismiss = { viewModel.setExportDialogOpen(false) },
            onStartBuildApk = { viewModel.startBuildApk() },
            onSelectCompileTarget = { viewModel.setCompileTarget(it) },
            onOpenApkUrl = { url ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            },
            onOpenNewProjectDialog = { viewModel.setNewProjectDialogOpen(true) },
            onOpenGitHubDialog = { viewModel.setGitHubDialogOpen(true) }
        )
    }

    if (uiState.isNewProjectDialogOpen) {
        NewProjectDialog(
            onDismiss = { viewModel.setNewProjectDialogOpen(false) },
            onCreateProject = { name, category, theme, sdk ->
                viewModel.createNewProject(name, category, theme, sdk)
            }
        )
    }

    if (uiState.isAttachmentPickerOpen) {
        AttachmentPickerSheet(
            onDismiss = { viewModel.setAttachmentPickerOpen(false) },
            onFileSelected = { viewModel.attachFile(it) },
            onLaunchSystemFilePicker = {
                globalFilePickerLauncher.launch("*/*")
            }
        )
    }
}
