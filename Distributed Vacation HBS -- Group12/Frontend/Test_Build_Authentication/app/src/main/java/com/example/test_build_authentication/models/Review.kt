/*
 * Review.kt
 *
 * Data class representing a review for a vacation home.
 */
package com.example.test_build_authentication.models

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String,
    val comments: String,
    val date: String,
    @SerializedName("listing_id")
    val listingId: String,
    @SerializedName("reviewer_id")
    val reviewerId: String,
    @SerializedName("reviewer_name")
    val reviewerName: String,
    val rating: Double = 0.0
)

data class ReviewResponse(
    val success: Boolean,
    val message: String,
    val review: Review? = null
)

data class ReviewListResponse(
    val reviews: List<Review>,
    val pagination: Pagination
) 