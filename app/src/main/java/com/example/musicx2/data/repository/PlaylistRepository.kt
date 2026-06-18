package com.example.musicx2.data.repository

import com.example.musicx2.data.model.Playlist
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
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

    suspend fun renamePlaylist(playlistId: String, newName: String) {
        firestore.collection("playlists").document(playlistId)
            .update("name", newName)
            .await()
    }

    suspend fun deletePlaylist(playlistId: String) {
        firestore.collection("playlists").document(playlistId).delete().await()
    }
}
