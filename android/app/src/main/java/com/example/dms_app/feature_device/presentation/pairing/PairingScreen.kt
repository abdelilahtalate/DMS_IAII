package com.example.dms_app.feature_device.presentation.pairing

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun PairingScreen(
    deviceId: String,
    secret: String,
    onPairingSuccess: (String) -> Unit,
    onBack: () -> Unit = {}
) {
    val vm: PairingViewModel = viewModel()
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    LaunchedEffect(Unit) {
        vm.pairDevice(deviceId, secret)
    }

    LaunchedEffect(state.success) {
        if (state.success) {
            state.previewUrl?.let { url ->
                kotlinx.coroutines.delay(1000)
                onPairingSuccess(url)
            }
        }
    }

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Status Icon
            Box(contentAlignment = Alignment.Center) {
                if (state.isLoading) {
                    repeat(3) { i ->
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_$i")
                        val scaleAnim by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, delayMillis = i * 600),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "scale"
                        )
                        val alphaAnim by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, delayMillis = i * 600),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "alpha"
                        )
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(scaleAnim)
                                .clip(CircleShape)
                                .background(colors.cyan.copy(alpha = alphaAnim))
                        )
                    }
                }

                val icon = when {
                    state.success -> Icons.Rounded.CheckCircle
                    state.error != null -> Icons.Rounded.ErrorOutline
                    else -> Icons.Rounded.BluetoothSearching
                }
                val iconColor = when {
                    state.success -> colors.success
                    state.error != null -> colors.redAlert
                    else -> colors.cyan
                }

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(iconColor.copy(alpha = 0.2f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = when {
                    state.success -> "Device Paired"
                    state.error != null -> "Pairing Failed"
                    else -> "Connecting..."
                },
                style = MaterialTheme.typography.displaySmall,
                color = colors.foreground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when {
                    state.success -> "Your device is ready to monitor."
                    state.error != null -> state.error ?: "Connection failed"
                    else -> "Scanning for DriveMind hardware..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (state.error != null) {
                PrimaryButton(
                    text = "Try Again",
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape),
                    color = colors.cyan,
                    trackColor = colors.surface3
                )
            }
        }
    }
}