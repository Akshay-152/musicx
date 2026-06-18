package com.example.musicx2.playback

data class UserPresence(
    val status: Boolean = false,
    val lastSeen: Long = 0
)

data class SyncRequest(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val status: String = "pending",
    val timestamp: Long = 0,
    val trackUri: String? = null,
    val sessionId: String = ""
)

data class TrackSync(
    val id: String = "",
    val uri: String = "",
    val title: String = "",
    val artist: String = "",
    val duration: Long = 0
)

data class PlaybackStateSync(
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val lastUpdated: Long = 0, // This will be the server timestamp
    val updatedBy: String = "",
    val revision: Long = 0,
    val lastActionId: String = ""
)

data class SyncSession(
    val sessionId: String = "",
    val master: String = "",
    val slave: String = "",
    val active: Boolean = true,
    val currentTrack: TrackSync? = null,
    val playbackState: PlaybackStateSync = PlaybackStateSync()
)
