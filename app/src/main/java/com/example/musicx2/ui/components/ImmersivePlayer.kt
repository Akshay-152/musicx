package com.example.musicx2.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.Player
import androidx.palette.graphics.Palette
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.musicx2.data.model.Track
import com.example.musicx2.ui.theme.Background
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary

@Composable
fun PlayingBarsAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    isPlaying: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playing_bars")

    val height1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val height2 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val height3 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        modifier = modifier.height(18.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(height1, height2, height3).forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(if (isPlaying) h else 0.3f)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImmersivePlayer(
    track: Track,
    isPlaying: Boolean,
    progress: Float,
    isFavorite: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    playbackSpeed: Float,
    volume: Float,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onFavoriteClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onRepeatClick: () -> Unit,
    onSpeedClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf(Color(0xFF1DB954)) }
    var showVolumeSlider by remember { mutableStateOf(false) }
    
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(track.coverUrl)
            .allowHardware(false)
            .build()
    )

    if (painter.state is AsyncImagePainter.State.Success) {
        val bitmap = (painter.state as AsyncImagePainter.State.Success).result.drawable.toBitmap()
        LaunchedEffect(bitmap) {
            Palette.from(bitmap).generate { palette ->
                palette?.dominantSwatch?.let {
                    dominantColor = Color(it.rgb)
                }
            }
        }
    }

    val animatedBgColor by animateColorAsState(
        targetValue = dominantColor.copy(alpha = 0.4f),
        animationSpec = tween(1000),
        label = "bg_color"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    var dragOffsetY by remember { mutableStateOf(0f) }
    val animatedDragOffset by animateFloatAsState(
        targetValue = dragOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "drag_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragOffsetY > 300f) {
                            onCloseClick()
                        }
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceAtLeast(0f)
                    }
                )
            }
            .offset(y = animatedDragOffset.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedBgColor, Background)
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header (Back on Left, Favorite on Right, Animation in Center)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSecondary
                    )
                }

                PlayingBarsAnimation(isPlaying = isPlaying)
                
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Primary else OnSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Swipe indicator
            Box(
                modifier = Modifier
                    .size(40.dp, 4.dp)
                    .clip(CircleShape)
                    .background(OnSecondary.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Rotating Disc with premium visuals
            Box(
                modifier = Modifier
                    .size(320.dp)
                    .shadow(32.dp, CircleShape, spotColor = dominantColor)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer ring decoration
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.1f)
                                )
                            )
                        )
                )
                
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize(0.95f)
                        .clip(CircleShape)
                        .rotate(if (isPlaying) rotation else 0f),
                    contentScale = ContentScale.Crop
                )
                
                // Center hole
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Background)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .background(OnSecondary.copy(alpha = 0.1f))
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Track Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = track.title.ifBlank { "Unknown Title" },
                    color = OnSecondary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = track.artist.ifBlank { "Unknown Artist" },
                    color = OnSecondary.copy(alpha = 0.7f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timeline Slider
            Column {
                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    colors = SliderDefaults.colors(
                        thumbColor = Primary,
                        activeTrackColor = Primary,
                        inactiveTrackColor = OnSecondary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime((progress * track.duration).toLong()),
                        color = OnSecondary.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = formatTime(track.duration),
                        color = OnSecondary.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Controls (Slimmer)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShuffleClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (shuffleModeEnabled) Primary else OnSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onPreviousClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Rounded.SkipPrevious,
                        contentDescription = "Previous",
                        tint = OnSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(12.dp, CircleShape, spotColor = Primary)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable { onPlayPauseClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = onNextClick, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Rounded.SkipNext,
                        contentDescription = "Next",
                        tint = OnSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                IconButton(onClick = onRepeatClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                            Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = "Repeat",
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) Primary else OnSecondary.copy(
                            alpha = 0.6f
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Actions (Speed, Playlist, Volume)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onSpeedClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = OnSecondary.copy(alpha = 0.6f))
                ) {
                    Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "${playbackSpeed}x", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onPlaylistClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = "Add to Playlist",
                        tint = OnSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = { showVolumeSlider = !showVolumeSlider }) {
                        Icon(
                            imageVector = if (volume > 0.5f) Icons.AutoMirrored.Rounded.VolumeUp else if (volume > 0f) Icons.AutoMirrored.Rounded.VolumeDown else Icons.AutoMirrored.Rounded.VolumeOff,
                            contentDescription = "Volume",
                            tint = if (showVolumeSlider) Primary else OnSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (showVolumeSlider) {
                        Slider(
                            value = volume,
                            onValueChange = onVolumeChange,
                            modifier = Modifier.padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = OnSecondary,
                                activeTrackColor = OnSecondary,
                                inactiveTrackColor = OnSecondary.copy(alpha = 0.2f)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
