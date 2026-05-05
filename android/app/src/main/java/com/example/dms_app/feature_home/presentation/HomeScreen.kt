package  com.example.dms_app.feature_home.presentation
import androidx.compose.animation.animateColorAsState


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.components.SecondaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun HomeScreen(
    onNavigateToPreview: () -> Unit
) {
    val vm: HomeViewModel = viewModel()
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    val statusColor by animateColorAsState(
        targetValue = when {
            !state.isMonitoring -> colors.mutedForeground
            state.lastResult?.distraction?.is_distracted == true -> colors.redAlert
            state.lastResult?.fatigue?.is_fatigued == true -> colors.amberWarn
            else -> colors.success
        },
        animationSpec = tween(500), label = "status_color"
    )

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.displaySmall,
                color = colors.foreground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (state.isMonitoring) "Live Monitoring Active" else "Ready to Start",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status Indicator
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(statusColor.copy(alpha = 0.2f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.1f))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (state.lastResult?.distraction?.is_distracted == true) "DISTRACTED"
                            else if (state.lastResult?.fatigue?.is_fatigued == true) "FATIGUE"
                            else if (state.isMonitoring) "FOCUSED" else "OFF",
                            color = statusColor,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.isMonitoring) {
                            Text(
                                text = "Score: ${String.format("%.2f", state.lastResult?.distraction?.score ?: 0f)}",
                                color = statusColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Monitoring Toggle Button
            PrimaryButton(
                text = if (state.isMonitoring) "Stop Drive Monitoring" else "Start Drive Monitoring",
                onClick = { vm.toggleMonitoring() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // View Stream Button
            SecondaryButton(
                text = "View Camera Preview",
                onClick = onNavigateToPreview,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
