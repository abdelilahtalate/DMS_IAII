package com.example.dms_app.feature_device.presentation.pairing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.components.SecondaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.PillShape

@Composable
fun DevicePairingScreen(
    onNavigateHome: () -> Unit,
    viewModel: DevicePairingViewModel = viewModel()
) {
    val colors = LocalAppColors.current
    val devices by viewModel.devices.collectAsState()
    val selectedId by viewModel.selectedId.collectAsState()
    val connectionType by viewModel.connectionType.collectAsState()

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Header
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = "Device Pairing",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.foreground
            )
            Text(
                text = "Connect your dashcam or OBD adapter",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground
            )

            // 2. Connection Type Tabs
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ConnectionTypeTab(
                    type = ConnectionType.WIFI,
                    icon = Icons.Rounded.Wifi,
                    isSelected = connectionType == ConnectionType.WIFI,
                    onClick = { viewModel.setConnectionType(ConnectionType.WIFI) }
                )
                ConnectionTypeTab(
                    type = ConnectionType.BLUETOOTH,
                    icon = Icons.Rounded.Bluetooth,
                    isSelected = connectionType == ConnectionType.BLUETOOTH,
                    onClick = { viewModel.setConnectionType(ConnectionType.BLUETOOTH) }
                )
            }

            // 3. Device List
            Spacer(modifier = Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        isSelected = selectedId == device.id,
                        onClick = { viewModel.selectDevice(device.id) }
                    )
                }

                // 4. Add Device Button
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    AddDeviceButton()
                }
            }

            // 5. Bottom Button
            Spacer(modifier = Modifier.height(24.dp))
            if (selectedId != null) {
                PrimaryButton(
                    text = "Connect Device",
                    onClick = { 
                        viewModel.connectSelected()
                        onNavigateHome()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SecondaryButton(
                    text = "Skip for now",
                    onClick = onNavigateHome,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ConnectionTypeTab(
    type: ConnectionType,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val bgColor by animateColorAsState(if (isSelected) colors.cyan.copy(alpha = 0.1f) else colors.surface2, label = "bg")
    val borderColor by animateColorAsState(if (isSelected) colors.cyan else colors.border, label = "border")
    val contentColor by animateColorAsState(if (isSelected) colors.cyan else colors.mutedForeground, label = "content")

    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(PillShape)
            .background(bgColor)
            .border(1.dp, borderColor, PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(16.dp))
            Text(text = type.name, color = contentColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DeviceCard(
    device: PairedDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    val borderColor by animateColorAsState(if (isSelected) colors.cyan else Color.Transparent, label = "border")
    
    // Pulse Ring Animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulse_alpha"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        if (!device.isConnected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .scale(pulseScale)
                    .border(2.dp, colors.cyan.copy(alpha = pulseAlpha), RoundedCornerShape(16.dp))
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .clickable(onClick = onClick),
            color = if (isSelected) colors.surface2 else colors.surface,
            tonalElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Box
                val iconBg = if (device.isConnected) colors.success.copy(alpha = 0.2f) else colors.cyan.copy(alpha = 0.1f)
                val iconTint = if (device.isConnected) colors.success else colors.cyan
                val icon = if (device.type == DeviceType.CAMERA) Icons.Rounded.CameraAlt else Icons.Rounded.Wifi
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = device.name, style = MaterialTheme.typography.titleMedium, color = colors.foreground)
                        if (device.isConnected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(colors.success.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(text = "Connected", color = colors.success, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val heights = listOf(8, 11, 14, 17)
                        heights.forEachIndexed { index, h ->
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(h.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(if (index < device.signalBars) colors.cyan else colors.surface3)
                            )
                        }
                        Text(
                            text = "Signal strength",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.mutedForeground,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                // Trailing
                if (device.isConnected) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = colors.success, modifier = Modifier.size(20.dp))
                } else {
                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddDeviceButton() {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = colors.surface3,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                ),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colors.surface2),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = colors.mutedForeground, modifier = Modifier.size(16.dp))
            }
            Text(text = "Add New Device", style = MaterialTheme.typography.bodyMedium, color = colors.mutedForeground)
        }
    }
}
