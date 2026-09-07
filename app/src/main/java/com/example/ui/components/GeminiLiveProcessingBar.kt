package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentProcessingState
import com.example.data.model.AgentWorkPhase
import com.example.ui.theme.*

/**
 * Componente de interfaz de estado sobre la interfaz de chat que refleja
 * en tiempo real los estados del flujo de procesamiento con Gemini:
 * «Analizando...», «Iluminando...», «Construyendo...», «Verificando...»
 */
@Composable
fun GeminiLiveProcessingBar(
    processingState: AgentProcessingState,
    activeModelName: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "gemini_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GeminiBlue.copy(alpha = pulseAlpha),
                        GeminiPurple.copy(alpha = pulseAlpha),
                        GeminiCyan.copy(alpha = pulseAlpha)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("gemini_live_processing_bar"),
        color = StudioLightSurface,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .animateContentSize()
        ) {
            // Top Bar Main Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(GeminiBlue, GeminiPurple, GeminiCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (processingState.phase) {
                                AgentWorkPhase.WORKING -> Icons.Default.AutoAwesome
                                AgentWorkPhase.ANALYZING -> Icons.Default.Search
                                AgentWorkPhase.ILLUMINATING -> Icons.Default.Lightbulb
                                AgentWorkPhase.BUILDING -> Icons.Default.Build
                                AgentWorkPhase.VERIFYING -> Icons.Default.Verified
                                AgentWorkPhase.COMPLETED -> Icons.Default.CheckCircle
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (processingState.phase != AgentWorkPhase.COMPLETED) spinAngle else 0f)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = processingState.currentActionTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = StudioLightTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GeminiBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = activeModelName,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = processingState.currentDetail,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = StudioLightTextSecondary
                        )
                    }
                }

                // Expand button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = StudioLightSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Ocultar" else "Pasos (${processingState.stepHistory.size})",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioLightTextSecondary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = StudioLightTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Real-time Pipeline Phase Pills
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val phases = listOf(
                    AgentWorkPhase.WORKING,
                    AgentWorkPhase.ANALYZING,
                    AgentWorkPhase.ILLUMINATING,
                    AgentWorkPhase.BUILDING,
                    AgentWorkPhase.VERIFYING
                )

                phases.forEach { phase ->
                    val isCurrent = processingState.phase == phase
                    val isPast = processingState.phase.ordinal > phase.ordinal

                    val bg = when {
                        isCurrent -> GeminiBlue
                        isPast -> GeminiGreen.copy(alpha = 0.15f)
                        else -> StudioLightSurfaceVariant.copy(alpha = 0.5f)
                    }

                    val textCol = when {
                        isCurrent -> Color.White
                        isPast -> GeminiGreen
                        else -> StudioLightTextMuted
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = bg,
                        border = if (isPast) androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.3f)) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 3.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = phase.displayName.replace("...", ""),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = textCol,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Expanded Steps
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = StudioLightBorderLight)
                    Spacer(modifier = Modifier.height(6.dp))
                    processingState.stepHistory.forEach { step ->
                        ToolActionStepRow(step = step)
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }
    }
}
