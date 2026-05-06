package com.example.dms_app.feature_livedrive.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.*
import com.example.dms_app.core.ui.theme.JetBrainsMono
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.SpaceGrotesk
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveDriveScreen(
    onNavigateToPreview: () -> Unit,
    vm: LiveDriveViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current
    val context = LocalContext.current

    // Location Permission Handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    LaunchedEffect(state.isMonitoring) {
        if (state.isMonitoring) {
            val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
            val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
            if (!hasFine || !hasCoarse) {
                permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    // Status based color
    val statusColor by animateColorAsState(
        targetValue = when {
            !state.isMonitoring -> colors.mutedForeground
            state.driveStatus == DriveStatus.FOCUSED -> colors.cyan
            state.driveStatus == DriveStatus.WARNING -> colors.amberWarn
            state.driveStatus == DriveStatus.ALERT -> colors.redAlert
            else -> colors.success
        },
        animationSpec = tween(500), label = "status_color"
    )

    // Alert Overlay state
    var showAlertOverlay by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.alertEvent.collect {
            showAlertOverlay = true
        }
    }

    DriveMindBackground {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. STATUS BAR
                StatusBar(state.isMonitoring, state.elapsedSeconds)

                Spacer(modifier = Modifier.weight(0.15f))

                // 2. MAIN GAUGE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Glow Background
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .graphicsLayer { alpha = if (colors.isDark) 0.2f else 0.4f }
                            .blur(40.dp)
                            .background(statusColor, CircleShape)
                    )

                    // Canvas Gauge
                    FocusGauge(
                        isMonitoring = state.isMonitoring,
                        focusScore = state.focusScore,
                        statusColor = statusColor
                    )

                    // Glass Center Disc
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassCard(modifier = Modifier.fillMaxSize()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = if (state.isMonitoring) state.focusScore.toString() else "--",
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontSize = 52.sp,
                                        fontFamily = SpaceGrotesk,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = colors.foreground
                                )
                                Text(
                                    text = "Focus Score",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.mutedForeground
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // 3. STATUS CHIPS
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        variant = if (state.isMonitoring && state.faceDetected) StatusVariant.SUCCESS else StatusVariant.INFO,
                        label = if (state.faceDetected) "Eyes Open" else "No Face",
                        icon = Icons.Rounded.Visibility
                    )
                    StatusChip(
                        variant = if (state.isMonitoring) StatusVariant.SUCCESS else StatusVariant.INFO,
                        label = "On Route",
                        icon = Icons.Rounded.Navigation
                    )
                    
                    // Speed chip
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${state.speed} KM/H",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
                            color = if (state.speed > 100) colors.redAlert else colors.success
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. ACTION BUTTONS
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isMonitoring) {
                        DangerButton(
                            text = "End Drive",
                            onClick = { vm.toggleMonitoring() }
                        )
                    } else {
                        PrimaryButton(
                            text = "Start Drive Monitoring",
                            onClick = { vm.toggleMonitoring() }
                        )
                    }
                    
                    SecondaryButton(
                        text = "View Camera Preview",
                        onClick = onNavigateToPreview
                    )
                }
            }
            
            // 5. ALERT OVERLAY
            if (showAlertOverlay) {
                AlertOverlayScreen(
                    onDismiss = { showAlertOverlay = false }
                )
            }
        }
    }
}

@Composable
fun StatusBar(isMonitoring: Boolean, elapsedSeconds: Int) {
    val colors = LocalAppColors.current
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp)
            .height(32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isMonitoring) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(8.dp * pulseScale)
                            .background(colors.success.copy(alpha = pulseAlpha), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(colors.success, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Monitoring",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.success
                )
            } else {
                Text(
                    text = "System Ready",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedForeground
                )
            }
        }

        if (isMonitoring) {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.labelSmall,
                color = colors.mutedForeground,
                fontFamily = JetBrainsMono
            )
        }
    }
}

@Composable
fun FocusGauge(
    isMonitoring: Boolean,
    focusScore: Int,
    statusColor: Color
) {
    val colors = LocalAppColors.current
    val animatedScore by animateFloatAsState(
        targetValue = if (isMonitoring) focusScore.toFloat() else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "gauge_score"
    )

    Canvas(modifier = Modifier.size(240.dp)) {
        val strokeWidth = 12.dp.toPx()
        val size = size.minDimension
        val center = Offset(size / 2, size / 2)
        val radius = (size - strokeWidth) / 2

        // a) Background track
        drawCircle(
            color = colors.surface2,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // b) Tick marks
        val tickCount = 12
        for (i in 0 until tickCount) {
            val angle = i * (360f / tickCount) - 90f
            val angleRad = Math.toRadians(angle.toDouble())
            val startRadius = radius + strokeWidth / 2 + 4.dp.toPx()
            val endRadius = startRadius + 8.dp.toPx()
            
            val startX = center.x + startRadius * cos(angleRad).toFloat()
            val startY = center.y + startRadius * sin(angleRad).toFloat()
            val endX = center.x + endRadius * cos(angleRad).toFloat()
            val endY = center.y + endRadius * sin(angleRad).toFloat()

            drawLine(
                color = colors.surface3,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // c) Progress arc
        if (isMonitoring) {
            val sweepAngle = (animatedScore / 100f) * 360f
            
            drawArc(
                color = statusColor.copy(alpha = 0.3f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size - strokeWidth, size - strokeWidth),
                style = Stroke(width = strokeWidth + 6.dp.toPx(), cap = StrokeCap.Round)
            )

            drawArc(
                color = statusColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size - strokeWidth, size - strokeWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
