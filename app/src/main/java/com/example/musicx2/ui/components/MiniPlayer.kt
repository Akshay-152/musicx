package com.example.musicx2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.musicx2.data.model.Track
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 8.dp),
        cornerRadius = 20.dp,
        onClick = onClick
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Artwork with Visualizer Overlay
                Box(contentAlignment = Alignment.BottomCenter) {
                    Image(
                        painter = rememberAsyncImagePainter(track.coverUrl),
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    if (isPlaying) {
                        VisualizerBars(
                            modifier = Modifier
                                .width(40.dp)
                                .height(12.dp)
                                .padding(bottom = 2.dp),
                            barCount = 3,
                            isPlaying = true
                        )
                    }
                }

                // Track Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title.ifBlank { "Unknown Title" },
                        color = OnSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist.ifBlank { "Unknown Artist" },
                        color = OnSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onPreviousClick) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Previous",
                            tint = OnSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = OnSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = onNextClick) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Next",
                            tint = OnSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Progress Indicator (Thin line at the bottom)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(2.dp),
                color = Primary,
                trackColor = Color.Transparent,
            )
        }
    }
}
