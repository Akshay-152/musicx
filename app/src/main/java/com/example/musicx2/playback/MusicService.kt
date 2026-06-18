package com.example.musicx2.playback

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class MusicService : MediaSessionService() {

    companion object {
        const val ACTION_STOP_SYNC = "ACTION_STOP_SYNC"
    }

    private var mediaSession: MediaSession? = null

    @Inject
    lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()
        
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer.setAudioAttributes(audioAttributes, true)
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        val intent = Intent(this, Class.forName("com.example.musicx2.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomMediaSessionCallback())
            .build()
    }

    private class CustomMediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            sessionCommands.add(SessionCommand(ACTION_STOP_SYNC, android.os.Bundle.EMPTY))

            val commandButtons = listOf(
                androidx.media3.session.CommandButton.Builder()
                    .setDisplayName("Stop Sync")
                    .setIconResId(com.example.musicx2.R.drawable.ic_stop_sync)
                    .setSessionCommand(SessionCommand(ACTION_STOP_SYNC, android.os.Bundle.EMPTY))
                    .setEnabled(true)
                    .build()
            )

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands.build())
                .setCustomLayout(commandButtons)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: android.os.Bundle
        ): ListenableFuture<SessionResult> {
            if (customCommand.customAction == ACTION_STOP_SYNC) {
                // Return success, the PlayerController's listener will emit the command to the ViewModel
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
            return Futures.immediateFuture(SessionResult(androidx.media3.session.SessionError.ERROR_NOT_SUPPORTED))
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
