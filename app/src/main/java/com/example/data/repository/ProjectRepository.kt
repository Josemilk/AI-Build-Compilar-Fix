package com.example.data.repository

import android.util.Log
import com.example.data.model.AiModelInfo
import com.example.data.model.AndroidSdkOption
import com.example.data.model.ProjectFile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ProjectRepository {
    private const val TAG = "ProjectRepository"
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val availableModels = listOf(
        AiModelInfo(
            id = "gemini-2.5-flash",
            name = "Gemini 2.5 Flash",
            alias = "Fast & Multimodal Architect",
            description = "High speed, large 1M token context, state-of-the-art UI coding and reactive logic generation.",
            maxDailyQuota = 1500,
            currentUsage = 0,
            contextWindow = "1M tokens",
            speed = "0.4s avg",
            isDefault = true
        ),
        AiModelInfo(
            id = "gemini-1.5-pro",
            name = "Gemini 1.5 Pro",
            alias = "Deep Reasoning & Architecture",
            description = "Complex system architecture, 2M token window, mathematical optimizations and full-stack design.",
            maxDailyQuota = 250,
            currentUsage = 0,
            contextWindow = "2M tokens",
            speed = "1.1s avg"
        ),
        AiModelInfo(
            id = "gemini-1.5-flash",
            name = "Gemini 1.5 Flash",
            alias = "Ultra Efficient & High Quota",
            description = "Sub-second response time for rapid tweaks, component fixes, and quick code refactoring.",
            maxDailyQuota = 2500,
            currentUsage = 0,
            contextWindow = "1M tokens",
            speed = "0.3s avg"
        ),
        AiModelInfo(
            id = "gemini-2.5-flash-thinking",
            name = "Gemini 2.5 Flash Thinking",
            alias = "Self-Repair & Autonomous Agent",
            description = "Deep thinking chain-of-thought for automatic build error resolution and complex debugging.",
            maxDailyQuota = 500,
            currentUsage = 0,
            contextWindow = "1M tokens",
            speed = "0.8s avg"
        ),
        AiModelInfo(
            id = "gemini-2.0-flash",
            name = "Gemini 2.0 Flash",
            alias = "Next-Gen Speed & Realtime",
            description = "Next generation Flash model tuned for low latency streaming and instant code generation.",
            maxDailyQuota = 1000,
            currentUsage = 0,
            contextWindow = "1M tokens",
            speed = "0.3s avg"
        )
    )

    val supportedSdkVersions = listOf(
        AndroidSdkOption(36, "Android 16", "Baklava (Preview)", "Latest Google API"),
        AndroidSdkOption(35, "Android 15", "Vanilla Ice Cream", "Recommended (92.5%)"),
        AndroidSdkOption(34, "Android 14", "Upside Down Cake", "Wide Adoption (96.8%)"),
        AndroidSdkOption(33, "Android 13", "Tiramisu", "High Compatibility (97.9%)"),
        AndroidSdkOption(31, "Android 12", "Snow Cone", "Material You Baseline (98.6%)"),
        AndroidSdkOption(29, "Android 10", "Quince Tart", "Legacy Base (99.2%)"),
        AndroidSdkOption(26, "Android 8.0", "Oreo", "Maximum Reach (99.7%)"),
        AndroidSdkOption(24, "Android 7.0", "Nougat", "Universal Minimum (99.9%)")
    )

    fun getInitialProjectFiles(): List<ProjectFile> {
        return listOf(
            ProjectFile(
                path = "app/src/main/java/com/example/MainActivity.kt",
                name = "MainActivity.kt",
                extension = "kt",
                content = """package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen()
                }
            }
        }
    }
}"""
            ),
            ProjectFile(
                path = "app/src/main/java/com/example/ui/screens/DashboardScreen.kt",
                name = "DashboardScreen.kt",
                extension = "kt",
                content = """package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    var itemsCount by remember { mutableStateOf(4) }
    var activeTab by remember { mutableStateOf(0) }
    var energyLevel by remember { mutableFloatStateOf(0.78f) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("PulseFit AI Pro", fontWeight = FontWeight.Bold)
                        Text("Real-time telemetry & sync", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { energyLevel = (energyLevel + 0.1f).coerceAtMost(1f) }) {
                        Icon(Icons.Default.Bolt, contentDescription = "Boost", tint = Color(0xFFF59E0B))
                    }
                    IconButton(onClick = { itemsCount++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Goal")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Hero Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF2563EB), Color(0xFF7C3AED), Color(0xFF06B6D4))
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Daily Target: 10,000 Steps", color = Color.White, fontWeight = FontWeight.SemiBold)
                                Badge(containerColor = Color.White.copy(alpha = 0.25f)) {
                                    Text("82%", color = Color.White, modifier = Modifier.padding(4.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { energyLevel },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("8,240 / 10,000 steps completed", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            // Metrics grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Heart Rate",
                        value = "74 bpm",
                        icon = Icons.Default.Favorite,
                        tint = Color(0xFFEF4444)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Calories",
                        value = "642 kcal",
                        icon = Icons.Default.LocalFireDepartment,
                        tint = Color(0xFFF97316)
                    )
                }
            }
            
            item {
                Text(
                    "Active Routines",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items((1..itemsCount).toList()) { index ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Column {
                                Text("Routine #${'$'}index: Core & Cardio", fontWeight = FontWeight.SemiBold)
                                Text("35 mins • High Intensity", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { /* Check item */ }) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = "Complete", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun MetricCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}"""
            ),
            ProjectFile(
                path = "app/build.gradle.kts",
                name = "build.gradle.kts",
                extension = "kts",
                content = """plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.aistudio.buildstudio.kzyrqp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}"""
            ),
            ProjectFile(
                path = "app/src/main/AndroidManifest.xml",
                name = "AndroidManifest.xml",
                extension = "xml",
                content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>"""
            ),
            ProjectFile(
                path = "app/src/main/res/values/strings.xml",
                name = "strings.xml",
                extension = "xml",
                content = """<resources>
    <string name="app_name">PulseFit AI Pro</string>
</resources>"""
            )
        )
    }

    fun getInitialProjects(): List<com.example.data.model.StudioApkProject> {
        return emptyList()
    }

    suspend fun deleteProject(projectId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        try {
            if (user != null) {
                firestore.collection("users")
                    .document(user.uid)
                    .collection("projects")
                    .document(projectId)
                    .delete()
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting project $projectId", e)
            Result.failure(e)
        }
    }

    fun createNewApkProject(
        name: String,
        category: String,
        themeColor: String = "Blue",
        targetSdk: Int = 36
    ): com.example.data.model.StudioApkProject {
        val cleanPkg = name.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val pkg = "com.aistudio.$cleanPkg.app"
        val id = "proj_" + java.util.UUID.randomUUID().toString().take(8)
        return com.example.data.model.StudioApkProject(
            id = id,
            name = name,
            packageName = pkg,
            description = "Proyecto recién creado. Listo para generar código con el agente.",
            category = category,
            themeColor = themeColor,
            targetSdk = targetSdk,
            minSdk = 24,
            version = "1.0.0",
            apkSizeMb = 0f,
            lastUpdated = "Recién creado",
            isBuilt = false,
            dynamicAppName = name,
            dynamicCounter = 0,
            dynamicGoal = 0,
            dynamicItemsCount = 0,
            files = getInitialProjectFiles(),
            supervisorNotes = "Proyecto inicializado. Usa el chat para generar código real."
        )
    }
}

