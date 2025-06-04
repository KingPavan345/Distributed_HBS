/*
 * Host.kt
 *
 * Data class representing a host of a vacation home.
 */
package com.example.test_build_authentication.models

import com.google.gson.annotations.SerializedName

data class Host(
    @SerializedName("host_id")
    val hostId: String,
    @SerializedName("host_name")
    val hostName: String,
    @SerializedName("host_location")
    val hostLocation: String?,
    @SerializedName("host_about")
    val hostAbout: String?,
    @SerializedName("host_response_time")
    val hostResponseTime: String?,
    @SerializedName("host_response_rate")
    val hostResponseRate: Int?,
    @SerializedName("host_is_superhost")
    val hostIsSuperhost: Boolean,
    @SerializedName("host_identity_verified")
    val hostIdentityVerified: Boolean,
    @SerializedName("host_listings_count")
    val hostListingsCount: Int,
    @SerializedName("host_total_listings_count")
    val hostTotalListingsCount: Int,
    @SerializedName("host_verifications")
    val hostVerifications: List<String>,
    @SerializedName("host_picture_url")
    val hostPictureUrl: String?,
    @SerializedName("host_thumbnail_url")
    val hostThumbnailUrl: String?,
    @SerializedName("host_has_profile_pic")
    val hostHasProfilePic: Boolean,
    @SerializedName("host_neighbourhood")
    val hostNeighbourhood: String?,
    @SerializedName("host_url")
    val hostUrl: String?
) 