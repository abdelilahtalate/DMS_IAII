package com.example.dms_app.feature_auth.presentation.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dms_app.core.ui.components.DriveMindBackground
import com.example.dms_app.core.ui.components.PrimaryButton
import com.example.dms_app.core.ui.theme.LocalAppColors
import com.example.dms_app.core.ui.theme.PillShape
import kotlinx.coroutines.launch

data class OnboardingSlide(val icon: ImageVector, val title: String, val description: String)

val slides = listOf(
    OnboardingSlide(Icons.Rounded.Shield, "Real-Time Protection", "AI continuously monitors your driving patterns and alerts you before dangerous situations escalate."),
    OnboardingSlide(Icons.Rounded.Visibility, "Fatigue Detection", "Advanced face recognition detects early signs of drowsiness and keeps you alert on long journeys."),
    OnboardingSlide(Icons.Rounded.Notifications, "Smart Alerts", "Receive calm, contextual notifications instead of alarming warnings. Your focus stays intact.")
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val colors = LocalAppColors.current
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    DriveMindBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Progress Dots
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 8.dp,
                        animationSpec = tween(300), label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = width, height = 4.dp)
                            .clip(PillShape)
                            .background(if (isSelected) colors.cyan else colors.surface3)
                    )
                }
            }

            // 2. Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                OnboardingPage(slide = slides[index])
            }

            // 3. Bottom Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val isLastPage = pagerState.currentPage == slides.size - 1

                PrimaryButton(
                    text = if (isLastPage) "Get Started" else "Continue",
                    onClick = {
                        if (isLastPage) {
                            onOnboardingComplete()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                if (!isLastPage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onOnboardingComplete) {
                        Text(
                            text = "Skip for now",
                            color = colors.mutedForeground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(slide: OnboardingSlide) {
    val colors = LocalAppColors.current
    
    // Floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8.dp.value,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer { this.translationY = translationY }
                .clip(RoundedCornerShape(24.dp))
                .background(colors.cyan.copy(alpha = 0.15f))
                .border(1.dp, colors.cyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = slide.icon,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = slide.title,
            style = MaterialTheme.typography.displaySmall,
            color = colors.foreground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = slide.description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.mutedForeground,
            textAlign = TextAlign.Center,
            lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified // will be 22sp via typography usually
        )
    }
}
