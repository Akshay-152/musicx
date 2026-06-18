package com.example.musicx2.di

import android.content.Context
import android.util.Log
import com.cloudinary.android.MediaManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideMediaManager(@ApplicationContext context: Context): MediaManager {
        val config = mapOf(
            "cloud_name" to com.example.musicx2.BuildConfig.CLOUDINARY_CLOUD_NAME,
            "secure" to true
        )
        
        try {
            MediaManager.init(context, config)
        } catch (_: IllegalStateException) {
            // Already initialized, safe to ignore
        } catch (e: Exception) {
            Log.e("CloudinaryModule", "Failed to initialize MediaManager", e)
        }
        
        return MediaManager.get()
    }
}
