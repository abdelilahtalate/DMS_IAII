package com.example.dms_app.feature_auth.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    viewModel: SplashViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = LocalAppColors.current
    val navigationTarget by viewModel.navigationTarget.collectAsStateWithLifecycle()

    LaunchedEffect(navigationTarget) {
        navigationTarget?.let { onNavigate(it) }
    }

    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnims = true }

    // Animations
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0f,
        animationSpec = tween(800, easing = EaseOut), label = "logo_alpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0.8f,
        animationSpec = tween(800, easing = EaseOut), label = "logo_scale"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400, easing = EaseOut), label = "name_alpha"
    )
    val nameOffsetY by animateDpAsState(
        targetValue = if (startAnims) 0.dp else 20.dp,
        animationSpec = tween(600, delayMillis = 400, easing = EaseOut), label = "name_offset"
    )
    val progress by animateFloatAsState(
        targetValue = if (startAnims) 1f else 0f,
        animationSpec = tween(1500, delayMillis = 600, easing = LinearOutSlowInEasing), label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        // Dot grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 50.dp.toPx()
            val radius  = 1.5.dp.toPx()
            val dotColor = colors.cyan.copy(alpha = 0.10f)
            var x = 0f
            while (x <= size.width) {
                var y = 0f
                while (y <= size.height) {
                    drawCircle(dotColor, radius, Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
        }

        // Aurora glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = Brush.radialGradient(
                colors = listOf(colors.cyan.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width / 2f, 0f),
                radius = size.width * 0.75f
            )
            drawRect(brush = brush)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EyeShieldLogo(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer(
                        alpha  = logoAlpha,
                        scaleX = logoScale,
                        scaleY = logoScale
                    ),
                color = colors.cyan
            )

            Spacer(modifier = Modifier.height(28.dp))

            val cyanBrush = remember(colors.cyan, colors.cyanGlow) {
                Brush.horizontalGradient(listOf(colors.cyan, colors.cyanGlow))
            }

            Text(
                text = "DriveMind AI",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    brush = cyanBrush
                ),
                modifier = Modifier
                    .offset(y = nameOffsetY)
                    .graphicsLayer(alpha = nameAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your AI Co-Driver",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground,
                modifier = Modifier
                    .offset(y = nameOffsetY)
                    .graphicsLayer(alpha = nameAlpha)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 32.dp, end = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ALWAYS WATCHING. ALWAYS PROTECTING.",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontWeight    = FontWeight.Medium
                ),
                color = colors.cyan
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(colors.surface3)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(colors.cyan, colors.cyanGlow))
                        )
                )
            }
        }
    }
}

@Composable
fun EyeShieldLogo(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4DD9D0)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = (w * 0.035f)

        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.04f)
            lineTo(w * 0.88f, h * 0.22f)
            lineTo(w * 0.88f, h * 0.60f)
            quadraticBezierTo(w * 0.88f, h * 0.82f, w * 0.50f, h * 0.96f)
            quadraticBezierTo(w * 0.12f, h * 0.82f, w * 0.12f, h * 0.60f)
            lineTo(w * 0.12f, h * 0.22f)
            close()
        }

        drawPath(
            path = shield,
            color = color,
            style = Stroke(width = strokePx)
        )

        // Eye shape
        val eyeCenterX = w * 0.50f
        val eyeCenterY = h * 0.46f
        val eyeRx      = w * 0.22f
        val eyeRy      = h * 0.10f

        val eyePath = Path().apply {
            moveTo(eyeCenterX - eyeRx, eyeCenterY)
            cubicTo(
                eyeCenterX - eyeRx, eyeCenterY - eyeRy * 2.2f,
                eyeCenterX + eyeRx, eyeCenterY - eyeRy * 2.2f,
                eyeCenterX + eyeRx, eyeCenterY
            )
            cubicTo(
                eyeCenterX + eyeRx, eyeCenterY + eyeRy * 2.2f,
                eyeCenterX - eyeRx, eyeCenterY + eyeRy * 2.2f,
                eyeCenterX - eyeRx, eyeCenterY
            )
            close()
        }

        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = strokePx)
        )

        // Pupil
        drawCircle(
            color = color,
            radius = eyeRy * 0.6f,
            center = Offset(eyeCenterX, eyeCenterY)
        )

        // Chevron
        val chevronY  = eyeCenterY + eyeRy * 2.8f
        val chevronPath = Path().apply {
            moveTo(w * 0.32f, chevronY)
            lineTo(w * 0.50f, chevronY + h * 0.08f)
            lineTo(w * 0.68f, chevronY)
        }

        drawPath(
            path = chevronPath,
            color = color,
            style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}