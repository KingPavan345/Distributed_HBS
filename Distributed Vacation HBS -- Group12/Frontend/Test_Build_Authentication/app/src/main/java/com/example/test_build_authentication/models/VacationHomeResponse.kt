/*
 * VacationHomeResponse.kt
 *
 * Data class representing the response for a single vacation home from the API.
 */
package com.example.test_build_authentication.models

data class VacationHomeResponse(
    val success: Boolean,
    val data: VacationHome,
    val message: String? = null
) 