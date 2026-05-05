package com.example.dms_app.feature_device.presentation.qr_scan

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean  // ← FIX 1: thread-safe flag

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun QrScannerScreen(
    onQrScanned: (String, String) -> Unit
) {
    val vm: QrViewModel = viewModel()
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ── Permissions ───────────────────────────────────────────────────────────
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // ── FIX 1: Use AtomicBoolean so the background analyzer thread ────────────
    // always reads the CURRENT value, not the captured-at-creation value.
    val isScanningEnabled = remember { AtomicBoolean(true) }

    // ── FIX 5: UI feedback states ─────────────────────────────────────────────
    var scanSuccess by remember { mutableStateOf(false) }
    var scanError   by remember { mutableStateOf<String?>(null) }

    // Scan-line animation
    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line_y"
    )

    // Corner accent pulse
    val cornerAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1.0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_alpha"
    )

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Pair Device",
                style = MaterialTheme.typography.displaySmall,
                color = colors.foreground,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scan the QR code on your DriveMind device",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.mutedForeground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Scanner box ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // ── FIX 2 & 3: Camera preview ─────────────────────────────
                    // AndroidView factory runs ONCE. We pass isScanningEnabled
                    // (AtomicBoolean) by reference so the analyzer always reads
                    // the latest value — not a captured snapshot.
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val executor = Executors.newSingleThreadExecutor()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()

                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val scanner = BarcodeScanning.getClient(
                                    BarcodeScannerOptions.Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                        .build()
                                )

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(
                                        ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                    )
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(executor) { imageProxy ->

                                            // FIX 1: AtomicBoolean.get() is thread-safe
                                            if (!isScanningEnabled.get()) {
                                                imageProxy.close()
                                                return@setAnalyzer
                                            }

                                            val mediaImage = imageProxy.image
                                            if (mediaImage == null) {
                                                imageProxy.close()
                                                return@setAnalyzer
                                            }

                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )

                                            scanner.process(image)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val raw = barcode.rawValue ?: continue
                                                        Log.d("QrScanner", "Raw QR: $raw")

                                                        // FIX 1: compareAndSet → only ONE thread wins
                                                        if (isScanningEnabled.compareAndSet(true, false)) {
                                                            val parsed = vm.parseQr(raw)
                                                            if (parsed != null) {
                                                                Log.d("QrScanner", "Parsed: $parsed")
                                                                // FIX 4: post to main thread
                                                                android.os.Handler(
                                                                    android.os.Looper.getMainLooper()
                                                                ).post {
                                                                    onQrScanned(
                                                                        parsed.first,
                                                                        parsed.second
                                                                    )
                                                                }
                                                            } else {
                                                                Log.e(
                                                                    "QrScanner",
                                                                    "Bad QR format: $raw"
                                                                )
                                                                // Re-enable scanning after bad QR
                                                                android.os.Handler(
                                                                    android.os.Looper.getMainLooper()
                                                                ).postDelayed({
                                                                    isScanningEnabled.set(true)
                                                                }, 1500)
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("QrScanner", "ML Kit error", e)
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    Log.e("QrScanner", "Binding failed", e)
                                }

                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // No permission placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surface2.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "📷",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera permission required",
                                color = colors.mutedForeground,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            TextButton(onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) {
                                Text("Grant Permission", color = colors.cyan)
                            }
                        }
                    }
                }

                // ── FIX 2: All 4 corner accents + scan line overlay ───────────
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cornerLen   = 48.dp.toPx()
                    val strokeWidth = 4.dp.toPx()
                    val radius      = 24.dp.toPx()
                    val cyanColor   = colors.cyan.copy(alpha = cornerAlpha)

                    // Helper to draw one L-shaped corner
                    fun drawCorner(
                        xStart: Float, yStart: Float,
                        xDir: Float,   yDir: Float
                    ) {
                        // Horizontal arm
                        drawLine(
                            color       = cyanColor,
                            start       = Offset(xStart, yStart),
                            end         = Offset(xStart + xDir * cornerLen, yStart),
                            strokeWidth = strokeWidth,
                            cap         = StrokeCap.Round
                        )
                        // Vertical arm
                        drawLine(
                            color       = cyanColor,
                            start       = Offset(xStart, yStart),
                            end         = Offset(xStart, yStart + yDir * cornerLen),
                            strokeWidth = strokeWidth,
                            cap         = StrokeCap.Round
                        )
                    }

                    val w = size.width
                    val h = size.height
                    val inset = strokeWidth / 2f

                    // Top-left
                    drawCorner(inset, inset, 1f, 1f)
                    // Top-right
                    drawCorner(w - inset, inset, -1f, 1f)
                    // Bottom-left
                    drawCorner(inset, h - inset, 1f, -1f)
                    // Bottom-right
                    drawCorner(w - inset, h - inset, -1f, -1f)

                    // Scan line (only when actively scanning)
                    if (isScanningEnabled.get()) {
                        val lineY = scanLineY * h
                        drawLine(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    colors.cyan.copy(alpha = 0.8f),
                                    colors.cyan,
                                    colors.cyan.copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            ),
                            start       = Offset(0f, lineY),
                            end         = Offset(w, lineY),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                }

                // ── FIX 5: Success overlay ────────────────────────────────────
                // Use androidx.compose.animation.AnimatedVisibility with
                // explicit Box scope to avoid the ColumnScope receiver conflict.
                androidx.compose.animation.AnimatedVisibility(
                    visible = scanSuccess,
                    enter   = fadeIn(),
                    exit    = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.success.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "✓",
                            style = MaterialTheme.typography.displaySmall,
                            color = colors.success
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status text below scanner
            Text(
                text = when {
                    scanSuccess          -> "QR Code detected!"
                    scanError != null    -> "Invalid QR code. Try again."
                    !hasCameraPermission -> "Camera access needed to scan"
                    else                 -> "Align the QR code within the frame"
                },
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    scanSuccess       -> colors.success
                    scanError != null -> colors.redAlert
                    else              -> colors.mutedForeground
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Emulator fallback button
            PrimaryButton(
                text = "Simulate Scan (Emulator)",
                onClick = {
                    if (isScanningEnabled.compareAndSet(true, false)) {
                        onQrScanned("ESP_001", "abc123")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
    }
}