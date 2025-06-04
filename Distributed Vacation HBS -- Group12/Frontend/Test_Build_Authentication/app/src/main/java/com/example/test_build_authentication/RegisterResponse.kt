package com.example.test_build_authentication

/*
 * RegisterResponse.kt
 *
 * Data class representing the response from a user registration request.
 */

data class RegisterResponse(
    val message: String,
    val user: User
)