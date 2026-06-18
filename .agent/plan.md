# Project Plan

App Name: musicx2

The user wants a high-detail, production-grade Spotify-style music application. 
Key features include:
- Modern, premium UI with dark glassmorphism, dynamic album-based gradients, and soft glowing accents.
- Immersive playback screen with circular rotating album art, timeline slider, and advanced playback controls (shuffle, repeat, speed, queue, favorites).
- Mini-player for persistent playback control.
- Global library with search, track counting, and filtering.
- Upload functionality for user tracks (Audio and Cover art) with Cloudinary integration.
- YouTube to MP3 pipeline (conversion and upload).
- Playlists management (create, add to, view).
- Firebase integration for Auth (Anonymous and Custom ID) and Firestore for tracks and favorites data.
- Background audio playback with notifications (MediaSession API equivalent in Android).
- Server status check.
- Material Design 3 (M3) and Android UX guidelines compliance.
- Adaptive app icon and Full Edge-to-Edge Display.

The user provided a web prototype (HTML/CSS/JS) which uses Firebase and Cloudinary. The Android app should replicate this experience natively using Kotlin and Jetpack Compose.

## Project Brief

# Project Brief: musicx2

### Features
1.  **Premium Immersive Player:** A sophisticated playback experience featuring a dark glassmorphic design, dynamic album-art
-based background gradients, and a rotating circular disc animation. It includes advanced controls such as shuffle, repeat, and queue management.
2.  **Background Audio & Media Session:** Seamless background playback powered by the Android Media3 API, ensuring music continues when the app is minimized, complete with system-level notifications and lock
-screen controls.
3.  **Cloud Upload & YouTube Integration:** A dedicated pipeline for uploading custom tracks (audio and cover art) to Cloudinary and converting YouTube content to MP3 for library expansion.
4.  **Firebase-Powered Library:** Real-time synchronization of tracks, playlists, and user favorites using Firestore
, supporting both Anonymous and Custom ID authentication.
5.  **Global Search & Discovery:** High-performance search and filtering capabilities across the global library with track counting and category-based navigation.

### High-Level Technical Stack
-   **Language:** Kotlin
-   **UI Framework:** Jetpack Compose (Material
 Design 3)
-   **Media Playback:** Android Media3 (ExoPlayer & MediaSession)
-   **Backend & Auth:** Firebase (Firestore & Authentication)
-   **Cloud Media Management:** Cloudinary Android SDK
-   **Networking:** Retrofit & OkHttp
-   **
Concurrency:** Kotlin Coroutines & Flow
-   **Code Generation:** KSP (Kotlin Symbol Processing)

## Implementation Steps

### Task_1_Core_Foundation: Setup dependencies (Media3, Firebase, Cloudinary, Hilt), implement M3 Theme with glassmorphic accents, and enable Edge-to-Edge display.
- **Status:** COMPLETED
- **Updates:** Task 1: Core Foundation is now complete. I have successfully integrated the required SDKs, implemented a high-fidelity Material 3 theme with glassmorphism, and set up the adaptive app icon and base UI structure.
- **Acceptance Criteria:**
  - Firebase and Cloudinary SDKs integrated
  - Media3 dependencies added
  - Hilt DI setup complete
  - M3 Theme with dynamic colors implemented
  - App builds successfully

### Task_2_Playback_Engine_and_Data: Implement the Media3 playback service (ExoPlayer/MediaSession) and create a Firestore repository for track and favorite synchronization.
- **Status:** COMPLETED
- **Updates:** Task 2: Playback Engine and Data is now complete. I have successfully implemented the Media3 playback service, Firestore synchronization, and authentication logic.
- **Acceptance Criteria:**
  - Background playback functional with MediaSession
  - Media notification controls work
  - Firestore repository fetches track data correctly
  - Anonymous/Custom ID Auth integrated

### Task_3_Main_UI_and_MiniPlayer: Develop the Home, Search, and Library screens with a persistent mini-player for seamless navigation.
- **Status:** COMPLETED
- **Updates:** Task 3: Main UI and MiniPlayer is now complete. I have successfully developed the Home, Search, and Library screens with a persistent mini-player and an immersive full-screen player.

Key Achievements:
- Navigation Structure: Implemented a unified view where the MiniPlayer is persistent and the ImmersivePlayer slides in on top.
- Search and Filtering: Added a premium search bar that filters the Firestore track list by title and artist in real-time.
- MiniPlayer: Functional persistent player with play/pause, skip, and a progress indicator. Clicking it expands the full player.
- Immersive Player: Added a rotating circular art animation, a functional timeline slider for seeking, and full playback controls.
- UI Polish: Integrated dark glassmorphic cards for track items and smooth animated transitions between the library and player views.
- Library Header: Includes a dynamic track counter and "Shuffle All" functionality.

Build Status:
- The app builds successfully and all UI components are integrated with the Media3 playback engine.

- **Acceptance Criteria:**
  - Home screen displays track list
  - Search and filtering functional
  - Mini-player controls visible across screens
  - Navigation between main screens is smooth
- **Duration:** N/A

### Task_4_Premium_Player_and_Uploads: Build the Immersive Player screen (rotating disc, gradients) and the Cloudinary/YouTube upload pipeline.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Immersive player has rotating disc animation and glassmorphism
  - Cloudinary audio/cover art upload works
  - YouTube-to-MP3 pipeline functional
  - Playlist management implemented
- **StartTime:** 2026-04-08 19:16:15 IST

### Task_5_Run_and_Verify: Finalize with an adaptive app icon, stability checks, and verification against requirements.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Adaptive app icon implemented
  - Build pass
  - App does not crash
  - All existing tests pass
  - Critical UI alignment with design confirmed

