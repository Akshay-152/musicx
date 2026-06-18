package com.example.musicx2

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Cloudinary initialization will be handled in a Hilt module or here
    }
}
