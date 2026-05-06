package com.example.dms_app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.*
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.feature_profile.domain.Device
import com.example.dms_app.feature_profile.domain.DeviceType
import com.example.dms_app.feature_profile.domain.UserProfile

@Composable
fun ProfileScreen(
    onToggleTheme: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalAppColors.current

    DriveMindBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
        // 1. HEADER
        Text(
            text = "Profile",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 2. PROFILE HERO CARD
        state.userProfile?.let { user ->
            GradientCard(
                showGlow = true,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // AVATAR
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(colors.cyan, colors.cyan.copy(alpha = 0.7f)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name.split(" ").map { it.take(1) }.joinToString(""),
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.Black
                            )
                        }

                        // INFO COLUMN
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        ) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = colors.foreground
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.mutedForeground,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            if (user.isPro) {
                                Row(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = colors.amberWarn,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Pro Member",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = colors.amberWarn
                                    )
                                }
                            }
                        }

                        // SETTINGS ICON BUTTON
                        IconButton(
                            onClick = { /* Navigate to settings */ },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.surface3)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = colors.foreground,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        thickness = 1.dp,
                        color = colors.border
                    )

                    // STATS ROW
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(value = user.totalTrips.toString(), label = "Total Trips", modifier = Modifier.weight(1f))
                        StatItem(value = user.focusScore.toString(), label = "Focus Score", modifier = Modifier.weight(1f))
                        StatItem(value = user.daysActive.toString(), label = "Days Active", modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. THEME TOGGLE ROW
        SurfaceCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (colors.isDark) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    contentDescription = null,
                    tint = colors.mutedForeground,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                ThemeToggleSwitch(
                    isDark = colors.isDark,
                    onToggle = onToggleTheme
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CONNECTED DEVICES CARD
        SurfaceCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connected Devices",
                        style = MaterialTheme.typography.titleMedium
                    )
                    BadgePill(
                        text = "${state.connectedDevices.count { it.isConnected }} Connected",
                        variant = StatusVariant.SUCCESS
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.connectedDevices.forEach { device ->
                        DeviceRow(device = device)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. SETTINGS LIST CARD
        SurfaceCard(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
        ) {
            Column {
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    label = "Notifications",
                    value = "On"
                )
                HorizontalDivider(color = colors.border)
                SettingsRow(
                    icon = Icons.Rounded.Shield,
                    label = "Privacy"
                )
                HorizontalDivider(color = colors.border)
                SettingsRow(
                    icon = Icons.Rounded.Settings,
                    label = "Settings"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. LOGOUT BUTTON
        DangerButton(
            text = "Sign Out",
            onClick = { viewModel.signOut() },
            icon = Icons.Rounded.Logout,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
}

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 24.sp),
            color = colors.foreground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.mutedForeground,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun DeviceRow(device: Device) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconBg = when (device.type) {
            DeviceType.CAMERA -> colors.cyan.copy(alpha = 0.2f)
            DeviceType.OBD -> colors.success.copy(alpha = 0.2f)
        }
        val iconColor = when (device.type) {
            DeviceType.CAMERA -> colors.cyan
            DeviceType.OBD -> colors.success
        }
        val icon = when (device.type) {
            DeviceType.CAMERA -> Icons.Rounded.CameraAlt
            DeviceType.OBD -> Icons.Rounded.Shield
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.foreground
            )
            Text(
                text = if (device.isConnected) "Connected" else "Disconnected",
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedForeground
            )
        }

        if (device.isConnected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.success)
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    value: String? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { /* Handle click */ }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.mutedForeground,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.foreground,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = colors.mutedForeground,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = colors.mutedForeground,
            modifier = Modifier.size(18.dp)
        )
    }
}
