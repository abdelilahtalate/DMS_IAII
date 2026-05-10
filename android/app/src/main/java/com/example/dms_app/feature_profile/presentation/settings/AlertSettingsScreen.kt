package com.example.dms_app.feature_profile.presentation.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.*
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun AlertSettingsScreen(
    onBack: () -> Unit = {},
    onNavigate: (NavDestination) -> Unit = {},
    viewModel: AlertSettingsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val fatigueLevel by viewModel.fatigueLevel.collectAsStateWithLifecycle()
    val phoneLevel by viewModel.phoneLevel.collectAsStateWithLifecycle()
    val distractionLevel by viewModel.distractionLevel.collectAsStateWithLifecycle()
    val voiceAlertsOn by viewModel.voiceAlertsOn.collectAsStateWithLifecycle()
    val hapticOn by viewModel.hapticOn.collectAsStateWithLifecycle()
    val criticalOnlyOn by viewModel.criticalOnlyOn.collectAsStateWithLifecycle()

    val colors = LocalAppColors.current

    DriveMindBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // 1. HEADER
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp)
            ) {
                Text(
                    text = "Alert Settings",
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = "Customize your safety preferences",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedForeground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 2. DETECTION SENSITIVITY CARD
            SurfaceCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Detection Sensitivity",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    SensitivityRow(
                        label = "Fatigue Detection",
                        icon = Icons.Rounded.Visibility,
                        currentLevel = fatigueLevel,
                        onLevelSelected = viewModel::setFatigue
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SensitivityRow(
                        label = "Phone Usage",
                        icon = Icons.Rounded.PhoneAndroid,
                        currentLevel = phoneLevel,
                        onLevelSelected = viewModel::setPhone
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SensitivityRow(
                        label = "Distraction Alert",
                        icon = Icons.Rounded.Warning,
                        currentLevel = distractionLevel,
                        onLevelSelected = viewModel::setDistraction
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. NOTIFICATION PREFERENCES CARD
            SurfaceCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Notification Preferences",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    ToggleRow(
                        label = "Voice Alerts",
                        icon = Icons.Rounded.VolumeUp,
                        isOn = voiceAlertsOn,
                        onToggle = viewModel::toggleVoice
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ToggleRow(
                        label = "Haptic Feedback",
                        icon = Icons.Rounded.Vibration,
                        isOn = hapticOn,
                        onToggle = viewModel::toggleHaptic
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ToggleRow(
                        label = "Critical Only",
                        icon = Icons.Rounded.Notifications,
                        isOn = criticalOnlyOn,
                        onToggle = viewModel::toggleCritical
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. VOICE PREFERENCE CARD
            SurfaceCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                val currentVoice by viewModel.preferredVoice.collectAsStateWithLifecycle()
                val voices = listOf("DEFAULT", "NOVA", "ECHO", "SHIMMER")
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Voice Preference",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        voices.forEach { voice ->
                            val isSelected = currentVoice == voice
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) colors.cyan else colors.surface2,
                                label = "voice_bg"
                            )
                            val textColor = if (isSelected) {
                                if (colors.isDark) Color.Black else Color.White
                            } else {
                                colors.mutedForeground
                            }
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .clickable { viewModel.setVoice(voice) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = voice.lowercase().capitalize(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. SOUND PREVIEW CARD
            SurfaceCard(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Sound Preview",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "Warning",
                            onClick = viewModel::previewWarning,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                        CriticalPreviewButton(
                            text = "Critical",
                            onClick = viewModel::previewCritical,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            BottomNavBar(
                current = NavDestination.PROFILE,
                onNavigate = onNavigate,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun SensitivityRow(
    label: String,
    icon: ImageVector,
    currentLevel: SensitivityLevel,
    onLevelSelected: (SensitivityLevel) -> Unit
) {
    val colors = LocalAppColors.current
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = currentLevel.name.lowercase().capitalize(),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.cyan,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SensitivityLevel.values().forEach { level ->
                val isSelected = currentLevel == level
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) colors.cyan else colors.surface2,
                    label = "btn_bg"
                )
                val textColor = if (isSelected) {
                    if (colors.isDark) Color.Black else Color.White
                } else {
                    colors.mutedForeground
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onLevelSelected(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.name.lowercase().capitalize(),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun ToggleRow(
    label: String,
    icon: ImageVector,
    isOn: Boolean,
    onToggle: () -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.mutedForeground,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f)
        )
        ToggleSwitch(isOn = isOn, onToggle = onToggle)
    }
}

@Composable
fun CriticalPreviewButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.redAlert.copy(alpha = 0.2f))
            .border(1.dp, colors.redAlert.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colors.redAlert,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}
