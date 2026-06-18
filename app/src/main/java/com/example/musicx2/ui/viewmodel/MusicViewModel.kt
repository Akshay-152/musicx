package com.example.musicx2.ui.viewmodel

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.musicx2.data.model.Playlist
import com.example.musicx2.data.model.Track
import com.example.musicx2.data.repository.AuthRepository
import com.example.musicx2.data.repository.TrackRepository
import com.example.musicx2.playback.ChatMessage
import com.example.musicx2.playback.ConnectionState
import com.example.musicx2.playback.ControllerRequest
import com.example.musicx2.playback.PlayerController
import com.example.musicx2.playback.ReplyMetadata
import com.example.musicx2.playback.RoomSync
import com.example.musicx2.playback.RoomSyncManager
import com.example.musicx2.playback.SyncManager
import com.example.musicx2.playback.SyncRequest
import com.example.musicx2.playback.TrackSync
import com.example.musicx2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

enum class UploadStatus { IDLE, UPLOADING, DONE, ERROR }

enum class ActivePanel { NONE, UPLOAD, LOGIN, EDIT, PLAYLIST, PLAYER, DELETE, PLAYLIST_DETAILS, SYNC_STUDIO }

@OptIn(UnstableApi::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MusicViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val authRepository: AuthRepository,
    private val playerController: PlayerController,
    private val syncManager: SyncManager,
    private val roomSyncManager: RoomSyncManager,
    private val notificationHelper: com.example.musicx2.util.NotificationHelper,
    @param:ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _activePanel = MutableStateFlow(ActivePanel.NONE)
    val activePanel = _activePanel.asStateFlow()

    private val _selectedTrack = MutableStateFlow<Track?>(null)
    val selectedTrack = _selectedTrack.asStateFlow()

    fun showPanel(panel: ActivePanel, track: Track? = null) {
        _selectedTrack.value = track
        _activePanel.value = panel
    }

    fun closePanel() {
        _activePanel.value = ActivePanel.NONE
        _selectedTrack.value = null
        _selectedPlaylist.value = null
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.DATE_ADDED)

    enum class SortOrder { TITLE, ARTIST, DATE_ADDED }

    private val _tracks = MutableStateFlow<Resource<List<Track>>>(Resource.Loading)
    private val _allTracksList = MutableStateFlow<List<Track>>(emptyList())
    private var isLastPage = false
    private var isFetching = false

    fun fetchTracks(isFirstPage: Boolean = true) {
        if (isFetching || (isLastPage && !isFirstPage)) return

        viewModelScope.launch {
            isFetching = true
            if (isFirstPage) {
                _tracks.value = Resource.Loading
                trackRepository.resetPagination()
                isLastPage = false
            }

            try {
                val newTracks = trackRepository.getTracksPage()
                if (newTracks.isEmpty()) {
                    isLastPage = true
                    if (isFirstPage) _tracks.value = Resource.Success(emptyList())
                } else {
                    if (isFirstPage) {
                        _allTracksList.value = newTracks
                    } else {
                        val currentIds = _allTracksList.value.map { it.id }.toSet()
                        val uniqueNew = newTracks.filter { it.id !in currentIds }
                        _allTracksList.value += uniqueNew
                    }
                    _tracks.value = Resource.Success(_allTracksList.value)
                }
            } catch (e: Exception) {
                if (isFirstPage) _tracks.value = Resource.Error(e.message ?: "Unknown Error")
            } finally {
                isFetching = false
            }
        }
    }

    fun loadMoreTracks() {
        fetchTracks(isFirstPage = false)
    }

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist = _selectedPlaylist.asStateFlow()

    private val _playlistTracks = MutableStateFlow<List<Track>>(emptyList())
    val playlistTracks = _playlistTracks.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly = _showFavoritesOnly.asStateFlow()

    private val _favoriteTrackIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteTrackIds = _favoriteTrackIds.asStateFlow()

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    val filteredTracks = combine(_tracks, _searchQuery, _sortOrder, _showFavoritesOnly, _favoriteTrackIds) { resource, query, sort, favOnly, favIds ->
        when (resource) {
            is Resource.Success -> {
                var filtered = resource.data
                if (favOnly) filtered = filtered.filter { favIds.contains(it.id) }
                if (query.isNotBlank()) {
                    filtered = filtered.filter {
                        it.title.contains(query, ignoreCase = true) ||
                                it.artist.contains(query, ignoreCase = true)
                    }
                }
                filtered = when (sort) {
                    SortOrder.TITLE -> filtered.sortedBy { it.title.lowercase() }
                    SortOrder.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
                    SortOrder.DATE_ADDED -> filtered.sortedByDescending { it.createdAt }
                }
                Resource.Success(filtered)
            }
            else -> resource
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading)

    fun showPlaylistDetails(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        _activePanel.value = ActivePanel.PLAYLIST_DETAILS
        viewModelScope.launch {
            _playlistTracks.value = trackRepository.getTracksByIds(playlist.trackIds)
        }
    }

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    val currentTrack = playerController.currentTrack
    val isPlaying = playerController.isPlaying
    val playWhenReady = playerController.playWhenReady
    val progress = playerController.progress
    val shuffleModeEnabled = playerController.shuffleModeEnabled
    val repeatMode = playerController.repeatMode
    val playbackSpeed = playerController.playbackSpeed
    val volume = playerController.volume

    private val _uploadStatus = MutableStateFlow(UploadStatus.IDLE)
    private val _isUploading = MutableStateFlow(false)
    val isUploading = _isUploading.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.IDLE)
    val connectionState = _connectionState.asStateFlow()

    val currentUserId = authRepository.authState

    val isAdmin: StateFlow<Boolean> = currentUserId.map { it == "AKSHAY PK 185" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _currentRoom = MutableStateFlow<RoomSync?>(null)
    val currentRoom = _currentRoom.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _showControlRequestModal = MutableStateFlow<ControllerRequest?>(null)
    val showControlRequestModal = _showControlRequestModal.asStateFlow()

    val isController: StateFlow<Boolean> = combine(currentRoom, currentUserId) { room, userId ->
        room?.controllerId == userId
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val onlineUsers: StateFlow<Map<String, Boolean>> = syncManager.observePresence()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val isFirebaseConnected: StateFlow<Boolean> = syncManager.isConnected
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val incomingRequest: StateFlow<SyncRequest?> = currentUserId
        .filterNotNull()
        .flatMapLatest { userId -> syncManager.observeRequests(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    private val _backgroundColors = MutableStateFlow(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
    val backgroundColors = _backgroundColors.asStateFlow()

    private var roomJob: Job? = null
    private var syncJob: Job? = null
    private var lastSyncedSongId: String? = null

    private var lastAppliedRevision: Long = -1L

    init {
        viewModelScope.launch {
            authRepository.autoLogin()
            // Start presence heartbeat
            currentUserId.filterNotNull().collectLatest { userId ->
                syncManager.setUserOnline(userId)
                try {
                    while (isActive) {
                        syncManager.updateHeartbeat(userId)
                        delay(60000) // Update every minute
                    }
                } finally {
                    syncManager.setUserOffline(userId)
                }
            }
        }

        // Sync studio: Lease renewal / heartbeat for controller
        viewModelScope.launch {
            while (isActive) {
                val myId = currentUserId.value
                val room = currentRoom.value
                if (_isSyncing.value && room != null && room.controllerId == myId && myId != null) {
                    roomSyncManager.renewLease(room.roomId, myId)
                }
                delay(10000) // 10 seconds heartbeat is enough for lease
            }
        }

        viewModelScope.launch {
            playerController.currentTrack.collectLatest { track ->
                if (track != null) {
                    updateBackgroundColors(track.coverUrl)

                    // Sync Studio: If we are the controller, push the new track state to the room
                    val myId = currentUserId.value
                    val room = currentRoom.value
                    if (_isSyncing.value && room != null && room.controllerId == myId && myId != null) {
                        updateRoomPlayback(trackOverride = track, positionOverride = 0L)
                    }
                }
            }
        }

        viewModelScope.launch {
            playerController.playWhenReady.collectLatest { isPlaying ->
                val myId = currentUserId.value
                val room = currentRoom.value
                if (_isSyncing.value && room != null && room.controllerId == myId && myId != null) {
                    updateRoomPlayback(isPlayingOverride = isPlaying)
                }
            }
        }

        fetchTracks()
        observeFavorites()
        observePlaylists()
    }

    private fun updateBackgroundColors(coverUrl: String?) {
        viewModelScope.launch {
            if (coverUrl.isNullOrBlank()) {
                _backgroundColors.value = listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                return@launch
            }

            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(coverUrl)
                    .allowHardware(false) // Required for Palette
                    .build()

                val result = (loader.execute(request) as? SuccessResult)?.drawable
                val bitmap = (result as? BitmapDrawable)?.bitmap

                if (bitmap != null) {
                    val palette = Palette.from(bitmap).generate()
                    val dominantColor = palette.getDominantColor(0xFF1A1A2E.toInt())
                    val darkMutedColor = palette.getDarkMutedColor(0xFF16213E.toInt())

                    // Create an aesthetic gradient pair
                    _backgroundColors.value = listOf(
                        Color(dominantColor).copy(alpha = 0.8f),
                        Color(darkMutedColor).copy(alpha = 0.9f)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            currentUserId.filterNotNull().collectLatest { userId ->
                trackRepository.getFavoriteTrackIdsFlow(userId).collect { ids ->
                    _favoriteTrackIds.value = ids.toSet()
                }
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            currentUserId.filterNotNull().collectLatest { userId ->
                trackRepository.getPlaylists(userId).collect {
                    _playlists.value = it
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun togglePlayPause() {
        val nextState = !playWhenReady.value
        playerController.togglePlayPause()
        if (_isSyncing.value) {
            val myId = currentUserId.value ?: return
            viewModelScope.launch {
                val roomId = currentRoom.value?.roomId ?: return@launch
                roomSyncManager.renewLease(roomId, myId)
                updateRoomPlayback(isPlayingOverride = nextState)
            }
        }
    }

    fun skipNext() {
        playerController.skipToNext()
        if (_isSyncing.value) {
            val myId = currentUserId.value ?: return
            viewModelScope.launch {
                val roomId = currentRoom.value?.roomId ?: return@launch
                roomSyncManager.renewLease(roomId, myId)
                // updateRoomPlayback will be triggered by track change if we add an observer or just call it here
                updateRoomPlayback()
            }
        }
    }

    fun skipPrevious() {
        playerController.skipToPrevious()
        if (_isSyncing.value) {
            val myId = currentUserId.value ?: return
            viewModelScope.launch {
                val roomId = currentRoom.value?.roomId ?: return@launch
                roomSyncManager.renewLease(roomId, myId)
                updateRoomPlayback()
            }
        }
    }

    fun seekTo(position: Float) {
        currentTrack.value?.let { track ->
            val seekPos = (position * track.duration).toLong()
            playerController.seekTo(seekPos)
            if (_isSyncing.value) {
                val myId = currentUserId.value ?: return@let
                viewModelScope.launch {
                    val roomId = currentRoom.value?.roomId ?: return@launch
                    roomSyncManager.renewLease(roomId, myId)
                    updateRoomPlayback(positionOverride = seekPos)
                }
            }
        }
    }

    fun playTrack(track: Track, tracks: List<Track> = emptyList()) {
        val queue = tracks.ifEmpty {
            (_tracks.value as? Resource.Success)?.data ?: listOf(track)
        }
        playerController.playTrack(track, queue)
        if (_isSyncing.value) {
            val myId = currentUserId.value ?: return
            viewModelScope.launch {
                val roomId = currentRoom.value?.roomId ?: return@launch
                roomSyncManager.renewLease(roomId, myId)
                updateRoomPlayback(
                    trackOverride = track,
                    isPlayingOverride = true,
                    positionOverride = 0L
                )
            }
        }
    }

    fun shuffleAll() {
        val tracks = (_tracks.value as? Resource.Success)?.data ?: return
        if (tracks.isNotEmpty()) playerController.playTrack(tracks.shuffled()[0], tracks.shuffled())
    }

    fun generateRandomId(): String {
        val randomNum = (0..99999).random()
        return "user_${String.format(Locale.US, "%05d", randomNum)}"
    }

    fun login(id: String) {
        viewModelScope.launch {
            authRepository.signInWithCustomId(id)
            closePanel()
        }
    }

    fun logout() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                syncManager.setUserOffline(userId)
            }
            leaveRoom()
            authRepository.clearCustomId()
            _favoriteTrackIds.value = emptySet()
            _playlists.value = emptyList()
        }
    }

    fun uploadTrack(uri: Uri, title: String, artist: String, imageUri: Uri?) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                _uploadStatus.value = UploadStatus.UPLOADING
                val audioUrl = trackRepository.uploadAudio(uri)
                val imageUrl = imageUri?.let { trackRepository.uploadImage(it) } ?: ""
                val track = Track(
                    title = title,
                    artist = artist,
                    audioUrl = audioUrl,
                    coverUrl = imageUrl,
                    createdAt = java.util.Date()
                )
                trackRepository.addTrack(track)
                _uploadStatus.value = UploadStatus.DONE
                _userMessage.emit("Track uploaded successfully")
                closePanel()
            } catch (e: Exception) {
                _uploadStatus.value = UploadStatus.ERROR
                _userMessage.emit("Upload failed: ${e.message}")
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch { trackRepository.toggleFavorite(trackId) }
    }

    fun deleteTrack(trackId: String) {
        viewModelScope.launch { trackRepository.deleteTrack(trackId); closePanel() }
    }

    fun updateTrack(trackId: String, title: String, artist: String) {
        viewModelScope.launch {
            trackRepository.updateTrack(
                trackId,
                mapOf("title" to title, "artist" to artist)
            ); closePanel()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch { trackRepository.createPlaylist(name) }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            trackRepository.addTrackToPlaylist(
                playlistId,
                trackId
            ); _userMessage.emit("Added to playlist")
        }
    }
    fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            trackRepository.removeTrackFromPlaylist(playlistId, trackId)
            selectedPlaylist.value?.let { showPlaylistDetails(it) }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch {
            try {
                notificationHelper.showDownloadNotification(track.title, 0)
                _userMessage.emit("Download started for ${track.title}")

                var lastProgress = 0
                trackRepository.downloadTrackToPublic(track) { progress ->
                    // Only update notification every 5% to avoid spamming
                    if (progress >= lastProgress + 5 || progress == 100) {
                        notificationHelper.showDownloadNotification(track.title, progress)
                        lastProgress = progress
                    }
                }

                _userMessage.emit("Download complete: ${track.title}")
            } catch (e: Exception) {
                e.printStackTrace()
                notificationHelper.showDownloadNotification(track.title, 0, isFailed = true)
                _userMessage.emit("Download failed: ${e.message}")
            }
        }
    }

    fun setVolume(volume: Float) {
        playerController.setVolume(volume)
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeatMode() {
        playerController.toggleRepeatMode()
    }

    fun setPlaybackSpeed(speed: Float) {
        playerController.setPlaybackSpeed(speed)
    }

    fun moveTrackInPlaylist(playlistId: String, from: Int, to: Int) {
        viewModelScope.launch {
            val playlist = _playlists.value.find { it.id == playlistId } ?: return@launch
            val newTrackIds = playlist.trackIds.toMutableList()
            if (from in newTrackIds.indices && to in newTrackIds.indices) {
                val trackId = newTrackIds.removeAt(from)
                newTrackIds.add(to, trackId)
                trackRepository.updatePlaylistOrder(playlistId, newTrackIds)
            }
        }
    }

    fun renamePlaylist(playlistId: String, newName: String) {
        viewModelScope.launch { trackRepository.renamePlaylist(playlistId, newName) }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            trackRepository.deletePlaylist(playlistId)
            closePanel()
        }
    }

    // --- Sync Studio Logic ---

    fun startSyncWithRoom(targetUserId: String) {
        val targetId = targetUserId.trim()
        if (targetId.isBlank()) {
            viewModelScope.launch { _userMessage.emit("Please enter a User ID") }
            return
        }

        viewModelScope.launch {
            val myUserId = authRepository.getActiveId()
            if (myUserId == targetId) {
                _userMessage.emit("You cannot sync with yourself")
                return@launch
            }
            _connectionState.value = ConnectionState.CONNECTING
            try {
                if (!(onlineUsers.value[targetId] ?: false)) {
                    _connectionState.value = ConnectionState.IDLE
                    _userMessage.emit("User $targetId is offline.")
                    return@launch
                }
                syncManager.sendSyncRequest(myUserId, targetId)
                _userMessage.emit("Request sent to $targetId")
                observeRoomRequestStatus(targetId, myUserId)
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.FAILED
                _userMessage.emit("Failed: ${e.message}")
            }
        }
    }

    private fun observeRoomRequestStatus(targetId: String, myId: String) {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            syncManager.observeRequestStatus(targetId, myId).collect { request ->
                if (request?.status == "accepted") {
                    val roomId = if (myId < targetId) "${myId}_$targetId" else "${targetId}_$myId"
                    joinRoom(roomId)
                    // Once joined, clear the request so it doesn't trigger again
                    syncManager.clearRequest(targetId, myId)
                    syncJob?.cancel()
                } else if (request?.status == "declined" || request?.status == "rejected") {
                    _connectionState.value = ConnectionState.IDLE
                    _userMessage.emit("Request declined")
                    syncJob?.cancel()
                }
            }
        }
    }

    fun respondToRoomRequest(from: String, accept: Boolean) {
        viewModelScope.launch {
            try {
                val to = authRepository.getActiveId()
                if (accept) {
                    val roomId = if (from < to) "${from}_$to" else "${to}_$from"
                    val currentTrackSync = currentTrack.value?.let {
                        TrackSync(it.id, it.audioUrl, it.title, it.artist, it.duration)
                    }
                    // 1. Create room FIRST
                    roomSyncManager.createRoom(from, to, currentTrackSync)
                    // 2. Then update status so the requester can join
                    syncManager.respondToRequest(to, from, accept)
                    joinRoom(roomId)
                } else {
                    syncManager.respondToRequest(to, from, accept)
                    _connectionState.value = ConnectionState.IDLE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Connection error: ${e.message}")
                _connectionState.value = ConnectionState.IDLE
            }
        }
    }

    fun joinRoom(roomId: String) {
        if (roomId.isBlank()) {
            viewModelScope.launch { _userMessage.emit("Invalid Room ID") }
            return
        }
        roomJob?.cancel()
        roomJob = viewModelScope.launch {
            try {
                val userId = authRepository.getActiveId()
                _isSyncing.value = true
                _connectionState.value = ConnectionState.CONNECTED
                roomSyncManager.observeRoom(roomId).collectLatest { room ->
                    _currentRoom.value = room
                    if (room == null || !room.active) {
                        if (_isSyncing.value) {
                            leaveRoom()
                            notificationHelper.showRoomTerminatedNotification()
                        }
                    } else {
                        handleRoomUpdate(room, userId)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _userMessage.emit("Failed to join room: ${e.message}")
                _isSyncing.value = false
                _connectionState.value = ConnectionState.FAILED
            }
        }
        viewModelScope.launch {
            try {
                roomSyncManager.observeMessages(roomId).collect { _messages.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun handleRoomUpdate(room: RoomSync, userId: String) {
        val syncState = room.playbackState
        if (syncState.revision <= lastAppliedRevision) return
        lastAppliedRevision = syncState.revision

        if (room.controllerId != userId) {
            val syncTrack = room.currentSong ?: return
            val currentT = currentTrack.value

            if (currentT?.id != syncTrack.id) {
                // Song has changed
                val tracks = trackRepository.getTracksByIds(listOf(syncTrack.id))
                if (tracks.isNotEmpty()) {
                    val trackToPlay = tracks[0].copy(duration = syncTrack.duration)
                    playerController.playTrack(
                        trackToPlay,
                        listOf(trackToPlay),
                        initialPosition = syncState.positionMs
                    )

                    delay(500)
                    if (syncState.isPlaying) playerController.play() else playerController.pause()
                }
            } else {
                // Play/Pause state
                if (syncState.isPlaying && !playWhenReady.value) {
                    playerController.play()
                } else if (!syncState.isPlaying && playWhenReady.value) {
                    playerController.pause()
                }

                // Drift correction
                val currentPos = (progress.value * (currentT.duration)).toLong()
                val targetPos = syncState.positionMs
                val diff = abs(currentPos - targetPos)

                if (syncState.isPlaying) {
                    if (diff > 1500) {
                        playerController.seekTo(targetPos)
                    } else if (diff > 400) {
                        // For small drift while playing, we can be more lenient or use a seek if needed
                        playerController.seekTo(targetPos)
                    }
                } else {
                    if (diff > 300) {
                        playerController.seekTo(targetPos)
                    }
                }
            }
        }
    }

    fun requestControl() {
        val roomId = currentRoom.value?.roomId ?: return
        viewModelScope.launch {
            val userId = authRepository.getActiveId()
            roomSyncManager.requestControl(roomId, userId, System.currentTimeMillis())
        }
    }

    fun respondToControlRequest(allow: Boolean) {
        val roomId = currentRoom.value?.roomId ?: return
        val request = _showControlRequestModal.value ?: return
        viewModelScope.launch {
            roomSyncManager.respondToControlRequest(roomId, allow, request.uid)
            _showControlRequestModal.value = null
        }
    }

    fun sendMessage(content: String, replyTo: ReplyMetadata?) {
        val roomId = currentRoom.value?.roomId ?: return
        viewModelScope.launch {
            val userId = authRepository.getActiveId()
            roomSyncManager.sendMessage(
                roomId,
                ChatMessage(senderId = userId, content = content, replyTo = replyTo)
            )
        }
    }

    fun deleteMessage(messageId: String) {
        currentRoom.value?.roomId?.let {
            viewModelScope.launch {
                roomSyncManager.deleteMessage(
                    it,
                    messageId
                )
            }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        currentRoom.value?.roomId?.let {
            viewModelScope.launch {
                roomSyncManager.editMessage(
                    it,
                    messageId,
                    newContent
                )
            }
        }
    }

    fun leaveRoom() {
        val roomId = currentRoom.value?.roomId
        viewModelScope.launch {
            if (roomId != null) roomSyncManager.leaveRoom(roomId)
            roomJob?.cancel()
            syncJob?.cancel()
            _currentRoom.value = null
            _isSyncing.value = false
            _messages.value = emptyList()
            _connectionState.value = ConnectionState.IDLE
            lastSyncedSongId = null
        }
    }

    fun cancelConnecting() {
        syncJob?.cancel()
        _connectionState.value = ConnectionState.IDLE
    }


    private fun updateRoomPlayback(
        isPlayingOverride: Boolean? = null,
        trackOverride: Track? = null,
        positionOverride: Long? = null
    ) {
        val roomId = currentRoom.value?.roomId ?: return
        val myId = currentUserId.value ?: return

        // Only the controller should write
        if (currentRoom.value?.controllerId != myId) return

        val track = trackOverride ?: currentTrack.value ?: return
        val isPlaying = isPlayingOverride ?: playWhenReady.value
        val position = positionOverride ?: (progress.value * track.duration).toLong()

        val trackSync =
            TrackSync(track.id, track.audioUrl, track.title, track.artist, track.duration)
        val actionId = java.util.UUID.randomUUID().toString()

        viewModelScope.launch {
            roomSyncManager.updateRoomState(
                roomId,
                trackSync,
                isPlaying,
                position,
                myId,
                System.currentTimeMillis(),
                actionId
            )
        }
    }
}
