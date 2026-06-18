package com.example.musicx2.di

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseApp(@ApplicationContext context: Context): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setApiKey("AIzaSyC-ft54KwehdTIsV93xBjLbd0nMeZ4ibDw")
            .setProjectId("akshay-b744a")
            .setApplicationId("1:752091661451:web:7ccfa46feff8ce87c6808e")
            .setStorageBucket("akshay-b744a.appspot.com")
            .build()

        return if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context, options)
        } else {
            FirebaseApp.getInstance()
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(firebaseApp: FirebaseApp): FirebaseFirestore {
        return FirebaseFirestore.getInstance(firebaseApp)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(firebaseApp: FirebaseApp): FirebaseStorage {
        return FirebaseStorage.getInstance(firebaseApp)
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(firebaseApp: FirebaseApp): FirebaseDatabase {
        return FirebaseDatabase.getInstance(firebaseApp)
    }
}
