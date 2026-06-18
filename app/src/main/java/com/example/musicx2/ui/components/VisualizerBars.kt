package com.example.musicx2.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.theme.PrimaryVariant

@Composable
fun VisualizerBars(
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    isPlaying: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            VisualizerBar(
                isPlaying = isPlaying,
                delay = index * 150
            )
        }
    }
}

@Composable
private fun VisualizerBar(
    isPlaying: Boolean,
    delay: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val heightScale by if (isPlaying) {
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "height"
        )
    } else {
        remember { mutableFloatStateOf(0.2f) }
    }

    Box(
        modifier = Modifier
            .width(3.dp)
            .fillMaxHeight(heightScale)
            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryVariant,
                        Primary
                    )
                )
            )
    )
}
