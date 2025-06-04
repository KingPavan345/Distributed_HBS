/*
 * VacationHomeListResponse.kt
 *
 * Data class representing the response for a list of vacation homes from the API.
 */
package com.example.test_build_authentication.models

import com.google.gson.annotations.SerializedName

data class VacationHomeListResponse(
    val success: Boolean,
    val data: List<VacationHome>,
    val pagination: Pagination,
    val message: String? = null
)

data class Pagination(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("total_pages")
    val totalPages: Int,
    @SerializedName("total_items")
    val totalItems: Int,
    @SerializedName("has_next")
    val hasNext: Boolean,
    @SerializedName("has_prev")
    val hasPrevious: Boolean,
    @SerializedName("items_per_page")
    val itemsPerPage: Int
) 