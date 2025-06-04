/*
 * RegisterRequest.kt
 *
 * Data class representing a user registration request payload.
 */
package com.example.test_build_authentication

    data class RegisterRequest(
        val username: String,
        val email: String,
        val password: String
    )