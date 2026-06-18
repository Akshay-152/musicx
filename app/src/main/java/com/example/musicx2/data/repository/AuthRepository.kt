package com.example.musicx2.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    private val CUSTOM_ID_KEY = stringPreferencesKey("custom_id")

    private val _authState = MutableStateFlow<String?>(null)
    val authState = _authState.asStateFlow()

    val customId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_ID_KEY]
    }

    suspend fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }

    suspend fun signInWithCustomId(id: String) {
        signInAnonymously()
        context.dataStore.edit { preferences ->
            preferences[CUSTOM_ID_KEY] = id
        }
        _authState.value = id
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun clearCustomId() {
        context.dataStore.edit { preferences ->
            preferences.remove(CUSTOM_ID_KEY)
        }
        _authState.value = null
    }

    private val mutex = Mutex()

    suspend fun getActiveId(): String {
        return mutex.withLock {
            val storedCustomId = customId.first()

            if (auth.currentUser == null) {
                try {
                    auth.signInAnonymously().await()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (storedCustomId != null) {
                _authState.value = storedCustomId
                return@withLock storedCustomId
            }

            // Auto login / user generation if none exists (user_XXXXX format)
            val randomNum = (0..99999).random()
            val newId = "user_%05d".format(java.util.Locale.US, randomNum)

            context.dataStore.edit { preferences ->
                preferences[CUSTOM_ID_KEY] = newId
            }
            _authState.value = newId
            newId
        }
    }

    suspend fun autoLogin() {
        getActiveId()
    }
}
