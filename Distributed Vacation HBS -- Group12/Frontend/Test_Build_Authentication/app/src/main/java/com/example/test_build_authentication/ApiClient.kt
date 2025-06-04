package com.example.test_build_authentication

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.test_build_authentication.api.VacationHomeService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/*
 * ApiClient.kt
 *
 * Provides network client setup and utility functions for making API requests throughout the app.
 * Centralizes configuration for HTTP operations.
 */

object ApiClient {
    private const val TAG = "ApiClient"
    private const val MAX_RETRIES = 3
    private const val RETRY_DELAY_MS = 1000L
    private const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB

    private lateinit var context: Context
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d(TAG, "OkHttp: $message")
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val networkStateInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            if (!isNetworkAvailable()) {
                Log.e(TAG, "Network check failed. Active network: ${getActiveNetworkInfo()}")
                throw IOException("No network connection available")
            }
            return chain.proceed(chain.request())
        }
    }

    private val retryInterceptor = object : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            var retryCount = 0
            var response: Response? = null
            var lastException: Exception? = null

            while (retryCount < MAX_RETRIES && response == null) {
                try {
                    if (retryCount > 0) {
                        Log.d(TAG, "Retrying request (attempt ${retryCount + 1}/$MAX_RETRIES)")
                        Thread.sleep(RETRY_DELAY_MS * retryCount)
                    }
                    
                    val request = chain.request()
                    Log.d(TAG, "Making request to: ${request.url}")
                    
                    response = chain.proceed(request)
                    if (response.isSuccessful) {
                        return response
                    } else {
                        Log.e(TAG, "Request failed with status code: ${response.code}")
                        response.close()
                        response = null
                    }
                } catch (e: Exception) {
                    lastException = e
                    Log.e(TAG, "Request failed (attempt ${retryCount + 1}/$MAX_RETRIES): ${e.message}")
                    response?.close()
                    response = null
                    
                    // Check if it's a connection issue
                    if (e is java.net.ProtocolException || e is java.net.SocketTimeoutException) {
                        Log.e(TAG, "Connection issue detected, will retry")
                    }
                }
                retryCount++
            }

            if (response == null) {
                val errorMessage = when (lastException) {
                    is java.net.ProtocolException -> "Connection was interrupted. Please try again."
                    is java.net.SocketTimeoutException -> "Request timed out. Please check your connection."
                    else -> "Failed after $MAX_RETRIES retries: ${lastException?.message ?: "Unknown error"}"
                }
                throw IOException(errorMessage)
            }
            return response
        }
    }

    private val connectionPool = ConnectionPool(
        maxIdleConnections = 5,
        keepAliveDuration = 5,
        timeUnit = TimeUnit.MINUTES
    )

    private val cache: Cache by lazy {
        val cacheDir = File(context.cacheDir, "http_cache")
        Cache(cacheDir, CACHE_SIZE)
    }

    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(networkStateInterceptor)
            .addInterceptor(retryInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectionPool(connectionPool)
            .cache(cache)
            .retryOnConnectionFailure(true)
            .build()
    }

    private val listingsRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Config.LISTINGS_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val listingsService: VacationHomeService by lazy { listingsRetrofit.create(VacationHomeService::class.java) }

    private fun isNetworkAvailable(): Boolean {
        try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            Log.d(TAG, "Network check - Active network: $network")
            Log.d(TAG, "Network check - Capabilities: $capabilities")
            
            return capabilities != null && (
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network availability", e)
            return false
        }
    }

    private fun getActiveNetworkInfo(): String {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            "Network: $network, Capabilities: $capabilities"
        } catch (e: Exception) {
            "Error getting network info: ${e.message}"
        }
    }
}

