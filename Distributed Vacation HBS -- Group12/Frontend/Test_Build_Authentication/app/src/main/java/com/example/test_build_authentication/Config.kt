/*
 * Config.kt
 *
 * Contains configuration constants and utility functions used throughout the app.
 */
package com.example.test_build_authentication

object Config {
    // Base URLs
    private const val LOCAL_BASE_URL = "http://10.0.2.2:8001/"
    private const val LOCAL_AUTH_URL = "http://10.0.2.2:8000/"
    private const val LOCAL_LISTINGS_URL = "http://10.0.2.2:8001/"
    private const val LOCAL_BOOKINGS_URL = "http://10.0.2.2:8002/"

    private const val AWS_BASE_URL = "http://your-ec2-instance-ip:8001/"
    private const val AWS_AUTH_URL = "http://your-ec2-instance-ip:8000/"
    private const val AWS_LISTINGS_URL = "http://your-ec2-instance-ip:8001/"
    private const val AWS_BOOKINGS_URL = "http://your-ec2-instance-ip:8002/"

    // Environment flag
    const val USE_AWS = false

    // Computed URLs based on environment
    val BASE_URL get() = if (USE_AWS) AWS_BASE_URL else LOCAL_BASE_URL
    val AUTH_BASE_URL get() = if (USE_AWS) AWS_AUTH_URL else LOCAL_AUTH_URL
    val LISTINGS_BASE_URL get() = if (USE_AWS) AWS_LISTINGS_URL else LOCAL_LISTINGS_URL
    val BOOKINGS_BASE_URL get() = if (USE_AWS) AWS_BOOKINGS_URL else LOCAL_BOOKINGS_URL

    // API Endpoints
    val LOGIN_ENDPOINT get() = "${AUTH_BASE_URL}api/auth/login/"
    val REGISTER_ENDPOINT get() = "${AUTH_BASE_URL}api/auth/register/"
    val RESET_PASSWORD_ENDPOINT get() = "${AUTH_BASE_URL}api/auth/reset-password/"
    val RESEND_VERIFICATION_ENDPOINT get() = "${AUTH_BASE_URL}api/auth/resend-verification/"
} 