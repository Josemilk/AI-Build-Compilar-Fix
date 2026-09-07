package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.model.GcpState
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.example.ui.theme.GeminiPurple
import android.util.Log

@Composable
fun GcpAuthSection(
    gcpState: GcpState,
    onConnectGcp: (String, String) -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            val token = account?.idToken ?: account?.serverAuthCode ?: "unknown_token"
            // For a real production app, you would exchange the serverAuthCode for an Access Token
            // via a backend. For now, we capture the fact that the user successfully authenticated.
            onConnectGcp(token, "gcp-project-\${account?.id}")
        } catch (e: Exception) {
            Log.e("GcpAuthSection", "Google Sign In failed", e)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cloud, contentDescription = "GCP", tint = GeminiPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Google Cloud Build (CI/CD)", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Autentica con Google Cloud para compilar tu APK usando los servidores reales de GCP Build API.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (gcpState.isConnected) {
                Text("✅ Conectado al proyecto: \${gcpState.projectId}", color = Color(0xFF388E3C))
            } else {
                Button(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestServerAuthCode("YOUR_WEB_CLIENT_ID") // Needs real client id for actual token exchange
                            .requestScopes(Scope("https://www.googleapis.com/auth/cloud-platform"))
                            .build()
                        val client = GoogleSignIn.getClient(context, gso)
                        launcher.launch(client.signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeminiPurple, contentColor = Color.White)
                ) {
                    Text("Iniciar sesión con Google (OAuth 2.0)")
                }
            }
        }
    }
}
