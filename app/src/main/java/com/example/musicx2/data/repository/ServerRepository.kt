package com.example.musicx2.data.repository

import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ServerRepository @Inject constructor(
    private val client: OkHttpClient
) {
    suspend fun checkHealth(url: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val request = Request.Builder()
                .url(url.plus("/health"))
                .build()
            
            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
