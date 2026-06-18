package com.example.musicx2.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class YoutubeRequest(
    val url: String
)

data class YoutubeResponse(
    @SerializedName("audio_url")
    val audioUrl: String,
    val title: String,
    val artist: String,
    @SerializedName("cover_url")
    val coverUrl: String
)

interface YouTubeApi {
    @GET("/")
    suspend fun checkHealth(): retrofit2.Response<Unit>

    @POST("extract_youtube")
    suspend fun convertYoutube(
        @Body request: YoutubeRequest
    ): retrofit2.Response<YoutubeResponse>
}
