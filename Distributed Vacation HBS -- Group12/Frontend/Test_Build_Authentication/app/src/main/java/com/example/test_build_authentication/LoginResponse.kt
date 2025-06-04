/*
 * LoginResponse.kt
 *
 * Data class representing the response from a user login request.
 */
package com.example.test_build_authentication

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val name: String
)
