package com.example.test_build_authentication

import android.app.Application
import android.util.Log

/*
 * TestBuildApplication.kt
 *
 * Application class for initializing global app state and configuration on startup.
 */

class TestBuildApplication : Application() {
    companion object {
        private const val TAG = "TestBuildApplication"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            ApiClient.initialize(this)
            Log.d(TAG, "ApiClient initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ApiClient", e)
        }
    }
} 