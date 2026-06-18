package com.example.musicx2.data.repository

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.musicx2.data.model.Playlist
import com.example.musicx2.data.model.Track
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TrackRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val mediaManager: MediaManager,
    private val okHttpClient: OkHttpClient
) {
    private var lastVisibleTrack: DocumentSnapshot? = null

    suspend fun getTracksPage(pageSize: Long = 40): List<Track> {
        var query = firestore.collection("tracks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(pageSize)
        
        lastVisibleTrack?.let {
            query = query.startAfter(it)
        }

        val snapshot = query.get().await()
        if (!snapshot.isEmpty) {
            lastVisibleTrack = snapshot.documents[snapshot.size() - 1]
        }
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Track::class.java)?.copy(id = doc.id)
        }
    }

    fun resetPagination() {
        lastVisibleTrack = null
    }

    suspend fun getAllTracks(): List<Track> {
        val snapshot = firestore.collection("tracks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Track::class.java)?.copy(id = doc.id)
        }
    }

    fun getTracks(): Flow<List<Track>> = callbackFlow {
        val subscription = firestore.collection("tracks")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val tracks = it.documents.mapNotNull { doc ->
                        doc.toObject(Track::class.java)?.copy(id = doc.id)
                    }
                    trySend(tracks)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun downloadTrackToPublic(track: Track, onProgress: (Int) -> Unit) {
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(track.audioUrl).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) throw Exception("Failed to download: ${response.code}")

            val body = response.body ?: throw Exception("Empty response body")
            val totalBytes = body.contentLength()

            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file =
                File(downloadsDir, "${track.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}.mp3")

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (totalBytes > 0) {
                            onProgress((totalRead * 100 / totalBytes).toInt())
                        }
                    }
                }
            }
            onProgress(100)
        }
    }

    suspend fun getTracksByIds(trackIds: List<String>): List<Track> {
        val validIds = trackIds.filter { it.isNotBlank() }.distinct()
        if (validIds.isEmpty()) return emptyList()
        // Firestore 'in' query limit is 10-30. Chunking by 10 for safety.
        val chunks = validIds.chunked(10)
        val allTracks = mutableListOf<Track>()

        for (chunk in chunks) {
            val snapshot = firestore.collection("tracks")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                .get()
                .await()
            allTracks.addAll(snapshot.documents.mapNotNull { doc ->
                doc.toObject(Track::class.java)?.copy(id = doc.id)
            })
        }
        // Return in the order requested, allowing duplicates if requested
        return trackIds.mapNotNull { id -> allTracks.find { it.id == id } }
    }

    suspend fun getFavoriteTrackIds(userId: String): List<String> {
        val snapshot = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.getString("trackId") }
    }

    fun getFavoriteTrackIdsFlow(userId: String): Flow<List<String>> = callbackFlow {
        val subscription = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val favorites = snapshot.documents.mapNotNull { it.getString("trackId") }
                    trySend(favorites)
                } else {
                    trySend(emptyList())
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun toggleFavorite(trackId: String) {
        val userId = authRepository.getActiveId() ?: return
        val docId = "${userId}_$trackId"
        val docRef = firestore.collection("favorites").document(docId)
        val doc = docRef.get().await()

        if (doc.exists()) {
            docRef.delete().await()
        } else {
            val data = mapOf(
                "userId" to userId,
                "trackId" to trackId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(data).await()
        }
    }

    suspend fun uploadAudio(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        mediaManager.upload(uri)
            .option("resource_type", "video")
            .unsigned("myupload")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    continuation.resume(resultData["secure_url"] as String)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        mediaManager.upload(uri)
            .option("resource_type", "image")
            .unsigned("myupload")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    continuation.resume(resultData["secure_url"] as String)
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    suspend fun downloadFile(url: String, fileName: String): File {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()
            val file = File(context.cacheDir, fileName)
            val fos = FileOutputStream(file)
            fos.write(response.body?.bytes() ?: throw Exception("Failed to download file"))
            fos.close()
            file
        }
    }

    suspend fun addTrack(track: Track) {
        val trackWithId = if (track.id.isBlank()) track.copy(id = UUID.randomUUID().toString()) else track
        firestore.collection("tracks").document(trackWithId.id).set(trackWithId).await()
    }

    suspend fun deleteTrack(trackId: String) {
        firestore.collection("tracks").document(trackId).delete().await()
    }

    suspend fun updateTrack(trackId: String, updates: Map<String, Any>) {
        firestore.collection("tracks").document(trackId).update(updates).await()
    }

    fun getPlaylists(userId: String): Flow<List<Playlist>> = callbackFlow {
        val subscription = firestore.collection("playlists")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    trySend(it.toObjects(Playlist::class.java))
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updatePlaylistOrder(playlistId: String, trackIds: List<String>) {
        firestore.collection("playlists").document(playlistId)
            .update("trackIds", trackIds)
            .await()
    }

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        firestore.collection("playlists").document(playlistId)
            .update("name", newName)
            .await()
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) {
        firestore.collection("playlists").document(playlistId)
            .update("trackIds", FieldValue.arrayRemove(trackId))
            .await()
    }

    suspend fun deletePlaylist(playlistId: String) {
        firestore.collection("playlists").document(playlistId).delete().await()
    }

    suspend fun createPlaylist(name: String) {
        val userId = authRepository.getActiveId() ?: return
        val id = UUID.randomUUID().toString()
        val playlist = Playlist(
            id = id,
            name = name,
            userId = userId,
            trackIds = emptyList()
        )
        firestore.collection("playlists").document(id).set(playlist).await()
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        firestore.collection("playlists").document(playlistId)
            .update("trackIds", FieldValue.arrayUnion(trackId))
            .await()
    }
}
