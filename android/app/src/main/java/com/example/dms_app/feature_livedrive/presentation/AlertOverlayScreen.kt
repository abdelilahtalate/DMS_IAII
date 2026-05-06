package com.example.dms_app.feature_livedrive.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.IconTextButton
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.SpaceGrotesk

@Composable
fun AlertOverlayScreen(
    onDismiss: () -> Unit,
    viewModel: AlertOverlayViewModel = viewModel()
) {
    val colors = LocalAppColors.current
    val alertType by viewModel.alertType.collectAsState()
    val showDismiss by viewModel.showDismiss.collectAsState()
    
    // Entry animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Pulsing background gradient opacity
    val infiniteTransition = rememberInfiniteTransition(label = "background_pulse")
    val gradientOpacity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_opacity"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            // 1. Radial Gradient Overlay (Centered Top)
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colors.redAlert.copy(alpha = 0.25f * gradientOpacity),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2, 0f),
                        radius = size.height * 0.8f
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 2. ALERT ICON & PULSE RINGS
                Box(contentAlignment = Alignment.Center) {
                    // Pulse Rings
                    PulseRing(delayMillis = 0)
                    PulseRing(delayMillis = 500)

                    // Entry animation for the central icon
                    val iconScale by animateFloatAsState(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
                        label = "icon_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .scale(iconScale)
                            .clip(CircleShape)
                            .background(colors.redAlert.copy(alpha = 0.2f))
                            .border(2.dp, colors.redAlert, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = "Warning",
                            modifier = Modifier.size(64.dp),
                            tint = colors.redAlert
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 3. ALERT MESSAGE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text(
                        text = when(alertType) {
                            AlertType.FATIGUE -> "Fatigue Detected"
                            AlertType.DISTRACTION -> "Distraction Detected"
                            AlertType.PHONE -> "Phone Usage Detected"
                        },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = SpaceGrotesk,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colors.redAlert,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "You appear to be showing signs of drowsiness. Please pull over safely when possible.",
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        color = colors.mutedForeground,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 4. ACTION BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    IconTextButton(
                        icon = Icons.Rounded.Phone,
                        label = "Call Help",
                        onClick = { viewModel.callHelp() },
                        tint = colors.redAlert
                    )
                    IconTextButton(
                        icon = Icons.Rounded.VolumeUp,
                        label = "Voice Alert",
                        onClick = { viewModel.triggerVoiceAlert() }
                    )
                }
            }

            // 5. DISMISS BUTTON (Bottom)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                AnimatedVisibility(
                    visible = showDismiss,
                    enter = fadeIn(tween(300))
                ) {
                    Surface(
                        onClick = { 
                            viewModel.dismiss()
                            onDismiss()
                        },
                        color = colors.surface2,
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = colors.foreground
                            )
                            Text(
                                text = "I'm fine, dismiss",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.foreground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PulseRing(delayMillis: Int) {
    val colors = LocalAppColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing, delayMillis = delayMillis),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(128.dp)
            .scale(scale)
            .alpha(alpha)
            .border(2.dp, colors.redAlert, CircleShape)
    )
}
