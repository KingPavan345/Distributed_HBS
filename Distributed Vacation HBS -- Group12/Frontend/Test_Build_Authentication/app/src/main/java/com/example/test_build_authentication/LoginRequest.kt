package com.example.test_build_authentication

/*
 * LoginRequest.kt
 *
 * Data class representing a user login request payload.
 */

data class LoginRequest(
    val username: String,
    val password: String
)
