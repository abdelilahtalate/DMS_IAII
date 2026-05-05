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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dms_app.core.ui.theme.LocalAppColors

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit,
    viewModel: SplashViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val colors = LocalAppColors.current
    val loadingComplete by viewModel.loadingComplete.collectAsStateWithLifecycle()

    LaunchedEffect(loadingComplete) {
        if (loadingComplete) onNavigateNext()
    }

    var startAnims by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnims = true }

    // ── Animations ────────────────────────────────────────────────────────────
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

        // ── 1. Background layers ──────────────────────────────────────────────
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

        // Aurora glow — top center radial gradient painted on Canvas (avoids blur clip issues)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = Brush.radialGradient(
                colors = listOf(colors.cyan.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width / 2f, 0f),
                radius = size.width * 0.75f
            )
            drawRect(brush = brush)
        }

        // ── 2. Center content ─────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Eye-in-shield logo
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

            // App name — gradient text fix:
            // Use a Canvas that draws the text shape then fills with gradient.
            // The simpler reliable approach on all API levels:
            // Draw text normally with a transparent color, then overdraw with gradient via BlendMode.
            val cyanBrush = remember(colors.cyan, colors.cyanGlow) {
                Brush.horizontalGradient(listOf(colors.cyan, colors.cyanGlow))
            }

            Text(
                text = "DriveMind AI",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    brush = cyanBrush          // ← Compose 1.3+ supports Brush on TextStyle directly
                ),
                modifier = Modifier
                    .offset(y = nameOffsetY)
                    .graphicsLayer(alpha = nameAlpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Your AI Co-Driver",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground,
                modifier = Modifier
                    .offset(y = nameOffsetY)
                    .graphicsLayer(alpha = nameAlpha)
            )
        }

        // ── 3. Bottom area ────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 32.dp, end = 32.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tagline — single instance, correct letter spacing
            Text(
                text = "ALWAYS WATCHING. ALWAYS PROTECTING.",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 3.sp,
                    fontWeight    = FontWeight.Medium
                ),
                color = colors.cyan
            )

            // Loading bar
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

// ── Eye-in-Shield logo ────────────────────────────────────────────────────────
// Matches the reference image: a shield with a glowing eye + chevron inside
@Composable
fun EyeShieldLogo(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4DD9D0)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokePx = (w * 0.035f)  // ~2dp relative to canvas size

        // ── Glow paint (applied via saveLayer + BlurMaskFilter) ───────────────
        val glowPaint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias     = true
                this.color      = android.graphics.Color.TRANSPARENT
                setShadowLayer(strokePx * 3f, 0f, 0f, color.copy(alpha = 0.8f).toArgb())
            }
        }

        // ── Shield path ───────────────────────────────────────────────────────
        // Hexagonal shield shape (like the reference)
        val shield = Path().apply {
            // Top point center
            moveTo(w * 0.50f, h * 0.04f)
            // Top-right slope
            lineTo(w * 0.88f, h * 0.22f)
            // Right side down
            lineTo(w * 0.88f, h * 0.60f)
            // Bottom-right curve to bottom point
            quadraticBezierTo(w * 0.88f, h * 0.82f, w * 0.50f, h * 0.96f)
            // Bottom-left curve up
            quadraticBezierTo(w * 0.12f, h * 0.82f, w * 0.12f, h * 0.60f)
            // Left side up
            lineTo(w * 0.12f, h * 0.22f)
            close()
        }

        // Draw shield with glow
        drawContext.canvas.nativeCanvas.apply {
            val framePaint = android.graphics.Paint().apply {
                isAntiAlias  = true
                style        = android.graphics.Paint.Style.STROKE
                strokeWidth  = strokePx
                this.color   = color.toArgb()
                maskFilter   = android.graphics.BlurMaskFilter(
                    strokePx * 2.5f,
                    android.graphics.BlurMaskFilter.Blur.OUTER
                )
            }
            val solidPaint = android.graphics.Paint().apply {
                isAntiAlias  = true
                style        = android.graphics.Paint.Style.STROKE
                strokeWidth  = strokePx
                this.color   = color.toArgb()
            }
            val androidPath = shield.asAndroidPath()
            drawPath(androidPath, framePaint)   // glow layer
            drawPath(androidPath, solidPaint)   // crisp layer
        }

        // ── Eye (center of shield) ────────────────────────────────────────────
        val eyeCenterX = w * 0.50f
        val eyeCenterY = h * 0.46f
        val eyeRx      = w * 0.22f   // horizontal radius
        val eyeRy      = h * 0.10f   // vertical radius — makes eye shape

        val eyePath = Path().apply {
            // Upper arc of eye
            moveTo(eyeCenterX - eyeRx, eyeCenterY)
            cubicTo(
                eyeCenterX - eyeRx, eyeCenterY - eyeRy * 2.2f,
                eyeCenterX + eyeRx, eyeCenterY - eyeRy * 2.2f,
                eyeCenterX + eyeRx, eyeCenterY
            )
            // Lower arc of eye
            cubicTo(
                eyeCenterX + eyeRx, eyeCenterY + eyeRy * 2.2f,
                eyeCenterX - eyeRx, eyeCenterY + eyeRy * 2.2f,
                eyeCenterX - eyeRx, eyeCenterY
            )
            close()
        }

        drawContext.canvas.nativeCanvas.apply {
            val eyeGlowPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style       = android.graphics.Paint.Style.STROKE
                strokeWidth = strokePx
                this.color  = color.toArgb()
                maskFilter  = android.graphics.BlurMaskFilter(
                    strokePx * 2f,
                    android.graphics.BlurMaskFilter.Blur.OUTER
                )
            }
            val eyeSolidPaint = android.graphics.Paint().apply {
                isAntiAlias = true
                style       = android.graphics.Paint.Style.STROKE
                strokeWidth = strokePx
                this.color  = color.toArgb()
            }
            val androidEye = eyePath.asAndroidPath()
            drawPath(androidEye, eyeGlowPaint)
            drawPath(androidEye, eyeSolidPaint)
        }

        // ── Pupil (filled circle inside eye) ─────────────────────────────────
        val pupilRadius = eyeRy * 0.85f
        drawContext.canvas.nativeCanvas.apply {
            val pupilGlow = android.graphics.Paint().apply {
                isAntiAlias = true
                style       = android.graphics.Paint.Style.FILL
                this.color  = color.toArgb()
                maskFilter  = android.graphics.BlurMaskFilter(
                    pupilRadius * 1.2f,
                    android.graphics.BlurMaskFilter.Blur.NORMAL
                )
            }
            val pupilSolid = android.graphics.Paint().apply {
                isAntiAlias = true
                style       = android.graphics.Paint.Style.FILL
                this.color  = color.toArgb()
            }
            drawCircle(eyeCenterX, eyeCenterY, pupilRadius, pupilGlow)
            drawCircle(eyeCenterX, eyeCenterY, pupilRadius * 0.7f, pupilSolid)
        }

        // ── Chevron / V shape below the eye ───────────────────────────────────
        val chevronY  = eyeCenterY + eyeRy * 2.8f
        val chevronPath = Path().apply {
            moveTo(w * 0.32f, chevronY)
            lineTo(w * 0.50f, chevronY + h * 0.08f)
            lineTo(w * 0.68f, chevronY)
        }

        drawContext.canvas.nativeCanvas.apply {
            val chevGlow = android.graphics.Paint().apply {
                isAntiAlias   = true
                style         = android.graphics.Paint.Style.STROKE
                strokeWidth   = strokePx * 0.9f
                strokeJoin    = android.graphics.Paint.Join.ROUND
                strokeCap     = android.graphics.Paint.Cap.ROUND
                this.color    = color.toArgb()
                maskFilter    = android.graphics.BlurMaskFilter(
                    strokePx * 2f,
                    android.graphics.BlurMaskFilter.Blur.OUTER
                )
            }
            val chevSolid = android.graphics.Paint().apply {
                isAntiAlias   = true
                style         = android.graphics.Paint.Style.STROKE
                strokeWidth   = strokePx * 0.9f
                strokeJoin    = android.graphics.Paint.Join.ROUND
                strokeCap     = android.graphics.Paint.Cap.ROUND
                this.color    = color.toArgb()
            }
            val androidChev = chevronPath.asAndroidPath()
            drawPath(androidChev, chevGlow)
            drawPath(androidChev, chevSolid)
        }
    }
}