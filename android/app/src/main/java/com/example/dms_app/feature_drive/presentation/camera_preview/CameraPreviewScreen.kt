package com.example.dms_app.feature_drive.presentation.camera_preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Face
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun CameraPreviewScreen(
    previewUrl: String,
    onSetupComplete: () -> Unit,
    vm: CameraPreviewViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val colors = LocalAppColors.current

    val decodedUrl = remember(previewUrl) {
        val url = URLDecoder.decode(previewUrl, StandardCharsets.UTF_8.toString())
        if (!url.startsWith("http")) "http://$url" else url
    }

    LaunchedEffect(decodedUrl) {
        vm.startStream(decodedUrl)
    }

    DisposableEffect(decodedUrl) {
        onDispose {
            vm.stopStream(decodedUrl)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        state.bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Driver Stream",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Premium Scanning Overlay
            FaceScanOverlay(colors = colors)
        }

        if (state.isLoading) {
            CircularProgressIndicator(color = colors.cyan)
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.bitmap != null) {
                PrimaryButton(
                    text = "Complete Face Profile",
                    onClick = {
                        vm.stopStream(decodedUrl)
                        onSetupComplete()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "SKIP FOR NOW",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.mutedForeground.copy(alpha = 0.6f),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            vm.stopStream(decodedUrl)
                            onSetupComplete()
                        }
                )
            }
        }

        // Error Handling
        state.error?.let {
            Text(
                text = it,
                color = colors.redAlert,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FaceScanOverlay(colors: com.example.dms_app.core.ui.theme.AppColors) {
    val infiniteTransition = rememberInfiniteTransition(label = "face_scan")
    val scanPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // High-Tech HUD Elements
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padding = 60.dp.toPx()
            val boxWidth = size.width - (padding * 2)
            val boxHeight = size.height * 0.45f
            val startX = padding
            val startY = (size.height - boxHeight) / 2.5f
            val endX = startX + boxWidth
            val endY = startY + boxHeight
            
            val strokeWidth = 2.dp.toPx()
            val cornerSize = 40.dp.toPx()
            val color = colors.cyan

            // Corners
            // TL
            drawLine(color, Offset(startX, startY), Offset(startX + cornerSize, startY), strokeWidth)
            drawLine(color, Offset(startX, startY), Offset(startX, startY + cornerSize), strokeWidth)
            // TR
            drawLine(color, Offset(endX, startY), Offset(endX - cornerSize, startY), strokeWidth)
            drawLine(color, Offset(endX, startY), Offset(endX, startY + cornerSize), strokeWidth)
            // BL
            drawLine(color, Offset(startX, endY), Offset(startX + cornerSize, endY), strokeWidth)
            drawLine(color, Offset(startX, endY), Offset(startX, endY - cornerSize), strokeWidth)
            // BR
            drawLine(color, Offset(endX, endY), Offset(endX - cornerSize, endY), strokeWidth)
            drawLine(color, Offset(endX, endY), Offset(endX, endY - cornerSize), strokeWidth)

            // Scanning Line
            val lineY = startY + (boxHeight * scanPosition)
            drawLine(
                brush = Brush.horizontalGradient(listOf(Color.Transparent, color, Color.Transparent)),
                start = Offset(startX, lineY),
                end = Offset(endX, lineY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // HUD Text
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Face, null, tint = colors.cyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("FACE_ID: SEARCHING", color = colors.cyan, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            Text("BIOMETRIC: CALIBRATING", color = colors.cyan.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
        }

        Text(
            text = "POSITION FACE WITHIN FRAME",
            color = colors.cyan,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 180.dp)
        )
    }
}
