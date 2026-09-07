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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgentProcessingState
import com.example.data.model.AgentWorkPhase
import com.example.data.model.ToolAction
import com.example.data.model.ToolStatus
import com.example.ui.theme.*

@Composable
fun AiProcessingStatusIndicator(
    processingState: AgentProcessingState,
    activeModelName: String,
    modifier: Modifier = Modifier
) {
    var isDetailsExpanded by remember { mutableStateOf(false) }

    // Shimmer and rotation animation for Gemini AI working state
    val infiniteTransition = rememberInfiniteTransition(label = "ai_working_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GeminiBlue.copy(alpha = pulseAlpha),
                        GeminiPurple.copy(alpha = pulseAlpha),
                        GeminiCyan.copy(alpha = pulseAlpha)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("ai_processing_indicator"),
        color = StudioLightSurface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
                .animateContentSize()
        ) {
            // Main Interactive Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDetailsExpanded = !isDetailsExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Animated Gemini Sparkle Badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
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
                            contentDescription = "Phase Icon",
                            tint = Color.White,
                            modifier = if (processingState.phase != AgentWorkPhase.COMPLETED) {
                                Modifier
                                    .size(20.dp)
                                    .rotate(if (processingState.phase == AgentWorkPhase.WORKING) spinAngle else 0f)
                            } else Modifier.size(20.dp)
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
                                fontSize = 14.sp,
                                color = StudioLightTextPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GeminiBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = activeModelName.replace("Gemini ", ""),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GeminiBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = processingState.currentDetail,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StudioLightTextSecondary
                        )
                    }
                }

                // Expand/Collapse Chevron with interactive pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = StudioLightSurfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isDetailsExpanded) "Hide steps" else "Show steps (${processingState.stepHistory.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioLightTextSecondary
                        )
                        Icon(
                            imageVector = if (isDetailsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = StudioLightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Interactive Phase Pipeline Badges (working, analizando, iluminating, build, verifying)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val phases = listOf(
                    AgentWorkPhase.WORKING,
                    AgentWorkPhase.ANALYZING,
                    AgentWorkPhase.ILLUMINATING,
                    AgentWorkPhase.BUILDING,
                    AgentWorkPhase.VERIFYING
                )

                phases.forEach { phase ->
                    val isCurrentPhase = processingState.phase == phase
                    val isPastPhase = processingState.phase.ordinal > phase.ordinal

                    val phaseBg = when {
                        isCurrentPhase -> GeminiBlue
                        isPastPhase -> GeminiGreen.copy(alpha = 0.15f)
                        else -> StudioLightSurfaceVariant.copy(alpha = 0.6f)
                    }

                    val phaseTextColor = when {
                        isCurrentPhase -> Color.White
                        isPastPhase -> GeminiGreen
                        else -> StudioLightTextMuted
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = phaseBg,
                        border = if (isPastPhase) androidx.compose.foundation.BorderStroke(1.dp, GeminiGreen.copy(alpha = 0.4f)) else null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
                        ) {
                            Text(
                                text = phase.displayName.replace("...", ""),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = phaseTextColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Expanded Step History & Tool Logs (like Google AI Studio Build)
            AnimatedVisibility(visible = isDetailsExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = StudioLightBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Real-time AI Tool Actions & Architecture Trace:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioLightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (processingState.stepHistory.isEmpty()) {
                        Text(
                            text = "• Initializing execution context...",
                            fontSize = 11.sp,
                            color = StudioLightTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        processingState.stepHistory.forEach { step ->
                            ToolActionStepRow(step = step)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToolActionStepRow(step: ToolAction) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = StudioLightSurfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioLightBorder.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (step.status) {
                    ToolStatus.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = GeminiCyan,
                            strokeWidth = 1.5.dp
                        )
                    }
                    ToolStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GeminiGreen,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    ToolStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = GeminiRed,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Text(
                    text = step.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StudioLightTextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = StudioLightSurface
            ) {
                Text(
                    text = "${step.executionMs}ms",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioLightTextMuted,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
