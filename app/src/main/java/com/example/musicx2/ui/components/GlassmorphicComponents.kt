package com.example.musicx2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val backgroundBrush = if (containerColor != null) {
        Brush.verticalGradient(
            colors = listOf(
                containerColor.copy(alpha = 0.5f),
                containerColor.copy(alpha = 0.3f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1A1A24).copy(alpha = 0.7f),
                Color(0xFF121216).copy(alpha = 0.9f)
            )
        )
    }

    val borderBrush = if (borderColor != null) {
        Brush.verticalGradient(listOf(borderColor, borderColor))
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.Transparent,
                Color.White.copy(alpha = 0.04f)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .background(brush = backgroundBrush)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
    ) {
        // Overlay for better texture (noise/frosted look)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun GlassmorphicSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .blur(20.dp)
            .background(Color.Black.copy(alpha = 0.3f))
    ) {
        content()
    }
}
