package com.example.musicx2.playback

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicx2.data.model.Track
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(UnstableApi::class)
@Singleton
class PlayerController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _customCommands = MutableSharedFlow<String>()
    val customCommands: SharedFlow<String> = _customCommands.asSharedFlow()

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playWhenReady = MutableStateFlow(false)
    val playWhenReady: StateFlow<Boolean> = _playWhenReady.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _onTrackEndedNaturally = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onTrackEndedNaturally: SharedFlow<Unit> = _onTrackEndedNaturally.asSharedFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: kotlinx.coroutines.Job? = null

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            val controller = mediaControllerFuture?.get() ?: return@addListener
            mediaController = controller
            
            // Sync initial state
            _isPlaying.value = controller.isPlaying
            _playWhenReady.value = controller.playWhenReady
            _shuffleModeEnabled.value = controller.shuffleModeEnabled
            _repeatMode.value = controller.repeatMode
            _playbackSpeed.value = controller.playbackParameters.speed
            _volume.value = controller.volume
            updateCurrentTrack(controller.mediaMetadata)
            if (controller.isPlaying) startProgressUpdate()

            val listener = object : Player.Listener {
                override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                    updateCurrentTrack(metadata)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        _onTrackEndedNaturally.tryEmit(Unit)
                    }
                    mediaController?.let { updateCurrentTrack(it.mediaMetadata) }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    _isBuffering.value = playbackState == Player.STATE_BUFFERING
                    if (playbackState == Player.STATE_READY) {
                        mediaController?.let { updateCurrentTrack(it.mediaMetadata) }
                    }
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    _error.value = error.localizedMessage
                    _isPlaying.value = false
                    stopProgressUpdate()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        _error.value = null
                        startProgressUpdate()
                    } else {
                        stopProgressUpdate()
                    }
                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    _playWhenReady.value = playWhenReady
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    _shuffleModeEnabled.value = shuffleModeEnabled
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    _repeatMode.value = repeatMode
                }

                override fun onPlaybackParametersChanged(playbackParameters: androidx.media3.common.PlaybackParameters) {
                    _playbackSpeed.value = playbackParameters.speed
                }

                override fun onVolumeChanged(volume: Float) {
                    _volume.value = volume
                }
            }
            controller.addListener(listener)
        }, MoreExecutors.directExecutor())
    }

    private fun updateCurrentTrack(metadata: MediaMetadata) {
        val controller = mediaController ?: return
        val mediaItem = controller.currentMediaItem
        val duration = controller.duration
        
        val id = mediaItem?.mediaId ?: metadata.extras?.getString("id") ?: ""
        val audioUrl = metadata.extras?.getString("audioUrl") ?: ""
        
        _currentTrack.value = Track(
            id = id,
            title = metadata.title?.toString() ?: "Unknown",
            artist = metadata.artist?.toString() ?: "Unknown Artist",
            audioUrl = audioUrl,
            coverUrl = metadata.artworkUri?.toString() ?: "",
            duration = if (duration > 0 && duration != androidx.media3.common.C.TIME_UNSET) duration else 0
        )
    }

    fun playTrack(track: Track, playlist: List<Track>, initialPosition: Long = 0) {
        val controller = mediaController ?: return
        
        // Optimization: Check if playlist is already set to avoid reloading
        val currentMediaIds = List(controller.mediaItemCount) { i -> controller.getMediaItemAt(i).mediaId }
        val newMediaIds = playlist.map { it.id }

        if (currentMediaIds != newMediaIds) {
            val mediaItems = playlist.map { item ->
                MediaItem.Builder()
                    .setMediaId(item.id)
                    .setUri(item.audioUrl)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(item.title)
                            .setArtist(item.artist)
                            .setArtworkUri(android.net.Uri.parse(item.coverUrl))
                            .setExtras(android.os.Bundle().apply { 
                                putString("id", item.id)
                                putString("audioUrl", item.audioUrl)
                            })
                            .build()
                    )
                    .build()
            }
            controller.setMediaItems(mediaItems)
            controller.prepare()
        }

        val startIndex = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        controller.seekTo(startIndex, initialPosition)
        controller.play()
        _currentTrack.value = track
        _playWhenReady.value = true
    }

    fun play() {
        mediaController?.let {
            if (it.playbackState == Player.STATE_IDLE) {
                it.prepare()
            }
            it.play()
            _playWhenReady.value = true
        }
    }

    fun pause() {
        mediaController?.pause()
        _playWhenReady.value = false
    }

    fun togglePlayPause() {
        if (mediaController?.playWhenReady == true) {
            pause()
        } else {
            play()
        }
    }

    fun skipToNext() {
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.currentPosition > 3000) {
                controller.seekTo(0)
            } else {
                controller.seekToPrevious()
            }
        }
    }

    fun setVolume(volume: Float) {
        mediaController?.volume = volume
        _volume.value = volume
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun toggleShuffle() {
        val nextMode = !(_shuffleModeEnabled.value)
        mediaController?.shuffleModeEnabled = nextMode
    }

    fun toggleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        mediaController?.repeatMode = nextMode
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaController?.setPlaybackSpeed(speed)
    }

    private fun startProgressUpdate() {
        stopProgressUpdate()
        progressJob = scope.launch {
            while (isActive) {
                mediaController?.let {
                    if (it.duration > 0) {
                        _progress.value = it.currentPosition.toFloat() / it.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressUpdate()
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
    }
}
