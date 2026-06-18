# MusicX2: Real-Time Event-Driven Music Synchronization

MusicX2 is a high-performance, real-time music synchronization system powered by **Firebase Realtime
Database**. It enables users to listen to music together with sub-second latency, featuring instant
request popups, shared playback controls, and glassmorphic UI design.

## 🚀 Key Features

### 1. Real-Time Presence & Peer Discovery

- **Online Detection**: Uses Firebase's `.info/connected` to monitor user connectivity.
- **Auto-Cleanup**: Leverages `onDisconnect` hooks to immediately mark users as offline when they
  disconnect.
- **Peer List**: Browse a list of available online peers to initiate a sync session.

### 2. Instant Sync Requests

- **Event-Driven Popups**: A persistent listener in the `MusicViewModel` detects incoming requests
  in `/sync_requests/{uid}`.
- **System-Wide Dialog**: Triggers a glassmorphic `AlertDialog` (Accept/Decline) regardless of the
  current app screen.
- **Timeout Management**: Requests automatically expire after 45 seconds if no action is taken.

### 3. Shared Playback Control (The "Sync Room")

- **Room Architecture**: Dynamic creation of sync rooms in `/rooms/{roomId}` with master/slave
  roles.
- **Bi-Directional Sync**:
    - Master controls the track selection.
    - Play/Pause state and Seek position are synchronized across all participants.
- **Latency Compensation**:
    - Uses Firebase Server Time Offset to calculate network delay.
    - Local position is adjusted dynamically:
      `adjustedPosition = remotePosition + (currentTime - lastUpdateTime)`.
- **Drift Correction**: A smart 2-second threshold prevents "stuttering" or "rubber-banding" by only
  seeking when the local and remote positions diverge significantly.

### 4. Glassmorphic UI Design

- **Modern Aesthetic**: Deep blurs, semi-transparent surfaces, and vibrant accent colors.
- **Components**:
    - `GlassmorphicCard`: Elevation and blur-based grouping.
    - `SyncStudio`: Central hub for managing connections and status.
    - `SyncControlCard`: Reactive UI for request status and session management.

## 🛠 Technical Architecture

- **Backend**: Pure Firebase Realtime Database (No traditional server needed).
- **Frontend**: Jetpack Compose with Material 3.
- **State Management**: Kotlin Coroutines & Flow (StateFlow/SharedFlow) within an MVVM architecture.
- **Playback**: Media3 (ExoPlayer) with a centralized `PlayerController` for consistent state across
  the UI and sync engine.

## 📡 Sync Logic Implementation

```kotlin
// Latency Adjustment Example
val serverTimeNow = syncManager.getServerTime()
val latency = serverTimeNow - playback.lastUpdated
val adjustedRemotePos = if (playback.isPlaying) {
    playback.positionMs + latency 
} else {
    playback.positionMs
}

// Drift Correction (2s Threshold)
val drift = abs(adjustedRemotePos - currentPosLocal)
if (drift > 2000) {
    playerController.seekTo(adjustedRemotePos)
}
```

## 📂 Project Structure

- `com.example.musicx2.playback`: Core sync managers and Media3 service.
- `com.example.musicx2.ui.components`: Glassmorphic Compose UI elements.
- `com.example.musicx2.ui.viewmodel`: Reactive logic handling Firebase events and Player state.
- `com.example.musicx2.data`: Track repositories and Auth management.

---
Built with ❤️ for synchronized music experiences.
