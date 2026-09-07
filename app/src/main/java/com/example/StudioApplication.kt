package com.example

import android.app.Application
import android.util.Log

/**
 * Application class.
 * Firebase is initialized automatically by the Google Services Gradle plugin
 * using the real configuration from app/google-services.json.
 * Manual FirebaseOptions with placeholders are removed to avoid conflicts.
 */
class StudioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("StudioApplication", "Application started. Firebase will be initialized by Google Services plugin if google-services.json is present.")
    }
}
