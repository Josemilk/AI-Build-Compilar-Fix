package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceSkin
import com.example.ui.theme.*
import com.example.ui.viewmodel.StudioUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmulatorView(
    uiState: StudioUiState,
    onReload: () -> Unit,
    onDeviceSkinChange: (DeviceSkin) -> Unit,
    onBoostMetric: () -> Unit,
    onAddDynamicItem: () -> Unit,
    onDismissActionsOverlay: () -> Unit = {},
    onTriggerGitHubActionsCompile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isLogcatExpanded by remember { mutableStateOf(false) }

    val reloadTransition = rememberInfiniteTransition(label = "reload_rotate")
    val reloadRotation by reloadTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StudioLightBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emulator Top Toolbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = StudioLightSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Device Model & API Level
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeminiGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = GeminiGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = when (uiState.deviceSkin) {
                                DeviceSkin.PIXEL_9_PRO -> "Pixel 9 Pro (Live Preview)"
                                DeviceSkin.TABLET -> "Pixel Tablet (Live Preview)"
                                DeviceSkin.COMPACT -> "Compact Device (Live Preview)"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = StudioLightTextPrimary
                        )
                        Text(
                            text = "Streaming Android 16 • Target API ${uiState.selectedTargetSdk}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioLightTextSecondary
                        )
                    }
                }

                // Center: Performance & Streaming Status
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GeminiGreen.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(GeminiGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "60 FPS • LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GeminiGreen
                        )
                    }
                }

                // Right: Device Skin switch, Reload, Logcat
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Skin switcher
                    IconButton(
                        onClick = {
                            val nextSkin = when (uiState.deviceSkin) {
                                DeviceSkin.PIXEL_9_PRO -> DeviceSkin.TABLET
                                DeviceSkin.TABLET -> DeviceSkin.COMPACT
                                DeviceSkin.COMPACT -> DeviceSkin.PIXEL_9_PRO
                            }
                            onDeviceSkinChange(nextSkin)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = "Switch Device Skin",
                            tint = StudioLightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Hot Reload
                    IconButton(
                        onClick = onReload,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GeminiBlue.copy(alpha = 0.12f))
                            .testTag("emulator_reload_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Hot Reload APK",
                            tint = GeminiBlue,
                            modifier = if (uiState.isReloadingPreview) Modifier.rotate(reloadRotation) else Modifier.size(16.dp)
                        )
                    }

                    // Logcat Drawer Toggle
                    IconButton(
                        onClick = { isLogcatExpanded = !isLogcatExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Toggle Logcat",
                            tint = if (isLogcatExpanded) GeminiBlue else StudioLightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Device Frame Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val deviceWidth = when (uiState.deviceSkin) {
                DeviceSkin.PIXEL_9_PRO -> 310.dp
                DeviceSkin.TABLET -> 380.dp
                DeviceSkin.COMPACT -> 270.dp
            }

            // Outer Device Bezel
            Surface(
                modifier = Modifier
                    .width(deviceWidth)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFF334155)),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(StudioLightBackground)
                ) {
                    // Device Status Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioLightSurface)
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "9:41",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = StudioLightTextPrimary
                        )

                        // Camera Hole Punch
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A))
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = StudioLightTextPrimary, modifier = Modifier.size(12.dp))
                            Icon(Icons.Default.BatteryFull, contentDescription = null, tint = StudioLightTextPrimary, modifier = Modifier.size(13.dp))
                        }
                    }

                    // Reloading Overlay or Live Dynamic App Content
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        // Live Interactive Preview App
                        DynamicLiveAppContent(
                            uiState = uiState,
                            onBoostMetric = onBoostMetric,
                            onAddDynamicItem = onAddDynamicItem
                        )

                        // Reloading Ripple Overlay
                        if (uiState.isReloadingPreview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = GeminiBlue, modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Recompilando e Instalando APK...", color = StudioLightTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // GitHub Actions Real-time Build Status Overlay
                        val actionsState = uiState.githubState.actionsCompilationState
                        if (actionsState.isCompiling || actionsState.logs.isNotEmpty() || actionsState.runStatus != "idle") {
                            GitHubActionsOverlay(
                                actionsState = actionsState,
                                onDismiss = onDismissActionsOverlay,
                                onReTrigger = onTriggerGitHubActionsCompile,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(8.dp)
                            )
                        }
                    }

                    // Android Navigation Bar Gesture Pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StudioLightSurface)
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF64748B))
                        )
                    }
                }
            }
        }

        // Collapsible Logcat & Console Logs Drawer
        AnimatedVisibility(visible = isLogcatExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF090D16),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Terminal, contentDescription = null, tint = GeminiGreen, modifier = Modifier.size(14.dp))
                            Text("Live Logcat & System Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text("API ${uiState.selectedTargetSdk} Ready", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GeminiGreen)
                    }
                    HorizontalDivider(color = Color(0xFF1E293B), modifier = Modifier.padding(vertical = 4.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(uiState.emulatorConsoleLogs) { log ->
                            Text(
                                text = log,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (log.contains("Error") || log.contains("W/")) GeminiAmber else Color(0xFF86EFAC),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicLiveAppContent(
    uiState: StudioUiState,
    onBoostMetric: () -> Unit,
    onAddDynamicItem: () -> Unit
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }

    val themeBrush = when (uiState.dynamicAppThemeColor) {
        "Purple" -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFF9333EA)))
        "Green" -> Brush.horizontalGradient(listOf(Color(0xFF059669), Color(0xFF10B981)))
        "Dark" -> Brush.horizontalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
        else -> Brush.horizontalGradient(listOf(GeminiBlue, GeminiPurple))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(themeBrush),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(
                                text = uiState.dynamicAppName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = StudioLightTextPrimary
                            )
                            Text(
                                text = "Streaming Live • Compose M3",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeminiBlue
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onBoostMetric, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Bolt, contentDescription = "Boost", tint = GeminiAmber, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onAddDynamicItem, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Item", tint = GeminiBlue, modifier = Modifier.size(20.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioLightSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioLightSurface,
                tonalElevation = 2.dp,
                modifier = Modifier.height(52.dp)
            ) {
                NavigationBarItem(
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(18.dp)) },
                    label = { Text("Resumen", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = "Analytics", modifier = Modifier.size(18.dp)) },
                    label = { Text("Métricas", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedNavIndex == 2,
                    onClick = { selectedNavIndex = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(18.dp)) },
                    label = { Text("Perfil", fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        containerColor = StudioLightBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            // Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeBrush)
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(
                                        "Meta de Proyecto",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.25f)
                                ) {
                                    val percent = ((uiState.dynamicCounter.toFloat() / uiState.dynamicGoal) * 100).toInt()
                                    Text(
                                        "$percent%",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val progress = (uiState.dynamicCounter.toFloat() / uiState.dynamicGoal).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.35f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${uiState.dynamicCounter} de ${uiState.dynamicGoal} completados",
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White,
                                    modifier = Modifier.clickable(onClick = onBoostMetric)
                                ) {
                                    Text(
                                        "+1 Sumar",
                                        color = GeminiBlue,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Stat Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PreviewMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ElectricBolt,
                        title = "Rendimiento",
                        value = "98%",
                        tint = GeminiAmber
                    )
                    PreviewMiniCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Favorite,
                        title = "Salud APK",
                        value = "Óptima",
                        tint = GeminiGreen
                    )
                }
            }

            // Dynamic items title & quick add button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Componentes Vivos (${uiState.dynamicItemsCount})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StudioLightTextPrimary
                    )
                    TextButton(
                        onClick = onAddDynamicItem,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = GeminiBlue)
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("Agregar Item", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeminiBlue)
                    }
                }
            }

            items((1..uiState.dynamicItemsCount).toList()) { index ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioLightSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GeminiBlue.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Widgets,
                                    contentDescription = null,
                                    tint = GeminiBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Módulo Reactivo Compose #$index",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudioLightTextPrimary
                                )
                                Text(
                                    text = "Estado: Sincronizado en tiempo real",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = StudioLightTextSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GeminiGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PreviewMiniCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    tint: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = StudioLightSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(value, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = StudioLightTextPrimary)
                Text(title, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = StudioLightTextSecondary)
            }
        }
    }
}

@Composable
fun GitHubActionsOverlay(
    actionsState: com.example.data.model.GitHubActionsCompilationState,
    onDismiss: () -> Unit,
    onReTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.96f),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, GeminiPurple.copy(alpha = 0.6f)),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("github_actions_realtime_overlay")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                when (actionsState.runStatus) {
                                    "completed" -> GeminiGreen
                                    "repairing" -> GeminiRed
                                    "in_progress", "queued" -> GeminiAmber
                                    else -> GeminiBlue
                                }
                            )
                    )
                    Text(
                        text = "GitHub Actions CI/CD Overlay",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (actionsState.runStatus) {
                            "completed" -> GeminiGreen.copy(alpha = 0.2f)
                            "repairing" -> GeminiRed.copy(alpha = 0.2f)
                            else -> GeminiPurple.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            text = actionsState.runStatus.uppercase(),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (actionsState.runStatus) {
                                "completed" -> GeminiGreen
                                "repairing" -> GeminiRed
                                else -> GeminiPurple
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(22.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close Overlay", tint = Color.LightGray, modifier = Modifier.size(13.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Status message
            Text(
                text = actionsState.statusMessage,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (actionsState.isCompiling) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = if (actionsState.runStatus == "repairing") GeminiRed else GeminiPurple,
                    trackColor = Color(0xFF334155)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Live streaming console output
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF020617),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 95.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(actionsState.logs) { log ->
                        Text(
                            text = log,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = when {
                                log.contains("ERROR", ignoreCase = true) || log.contains("Failed", ignoreCase = true) || log.contains("❌") -> GeminiRed
                                log.contains("SUCCESS", ignoreCase = true) || log.contains("✅") -> GeminiGreen
                                log.contains("Gemini") || log.contains("🤖") || log.contains("🧠") -> GeminiCyan
                                log.contains("⚠️") || log.contains("Warning") -> GeminiAmber
                                else -> Color(0xFFCBD5E1)
                            },
                            lineHeight = 12.sp
                        )
                    }
                }
            }

            // Footer controls
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!actionsState.isCompiling) {
                    TextButton(
                        onClick = onReTrigger,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = GeminiPurple)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Re-compilar Workflow", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = GeminiPurple)
                    }
                }
            }
        }
    }
}
