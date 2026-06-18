package com.example.musicx2.playback

enum class ConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    FAILED
}

data class RoomSync(
    val roomId: String = "",
    val connectedIds: Map<String, Boolean> = emptyMap(),
    val currentSong: TrackSync? = null,
    val playbackState: PlaybackStateSync = PlaybackStateSync(),
    val active: Boolean = true,
    val masterId: String = "",
    val secondMasterId: String = "",
    val controllerId: String = "", // Current active controller UID
    val controllerRequest: ControllerRequest? = null
)

data class ControllerRequest(
    val uid: String = "",
    val timestamp: Long = 0L
)
