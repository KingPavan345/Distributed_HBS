/*
 * VacationHome.kt
 *
 * Data class representing a vacation home entity with all its properties.
 */
package com.example.test_build_authentication.models

import com.google.gson.annotations.SerializedName
import java.util.Date
import android.util.Log

data class VacationHome(
    @SerializedName("_id")
    val id: String? = null,
    val name: String,
    val description: String?,
    val price: Double,
    @SerializedName("property_type")
    val propertyType: String,
    @SerializedName("room_type")
    val roomType: String?,
    val bedrooms: Int,
    val bathrooms: Double,
    val beds: Int,
    @SerializedName("minimum_nights")
    val minimumNights: Int,
    @SerializedName("maximum_nights")
    val maximumNights: Int,
    @SerializedName("review_scores")
    val reviewScores: ReviewScores? = null,
    @SerializedName("number_of_reviews")
    val numberOfReviews: Int,
    @SerializedName("host")
    val host: Host? = null,
    @SerializedName("house_rules")
    val houseRules: String?,
    @SerializedName("cancellation_policy")
    val cancellationPolicy: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("images")
    val images: Images? = null,
    val amenities: List<String>? = null,
    @SerializedName("address")
    val address: Address? = null,
    @SerializedName("availability")
    val availability: Availability? = null,
    @SerializedName("bed_type")
    val bedType: String?,
    @SerializedName("calendar_last_scraped")
    val calendarLastScraped: String?,
    @SerializedName("cleaning_fee")
    val cleaningFee: Double?,
    @SerializedName("extra_people")
    val extraPeople: Double?,
    @SerializedName("first_review")
    val firstReview: String?,
    @SerializedName("guests_included")
    val guestsIncluded: Double?,
    @SerializedName("interaction")
    val interaction: String?,
    @SerializedName("last_review")
    val lastReview: String?,
    @SerializedName("last_scraped")
    val lastScraped: String?,
    @SerializedName("listing_url")
    val listingUrl: String?,
    @SerializedName("neighborhood_overview")
    val neighborhoodOverview: String?,
    val notes: String?,
    val reviews: List<Review> = emptyList(),
    @SerializedName("picture_url")
    val pictureUrl: String? = null,
    @SerializedName("listing_id")
    val listingId: String? = null,
    @SerializedName("location")
    val location: String? = null
) {
    companion object {
        private const val TAG = "VacationHome"
    }

    // Computed property: listingId
    val hasValidId: Boolean
        get() {
            val id = listingId
            return !id.isNullOrEmpty()
        }

    // Helper properties for reviews
    val reviewCount: Int
        get() = reviews.size

    val latestReviews: List<Review>
        get() = reviews.take(5)  // Get the 5 most recent reviews

    val averageRating: Double
        get() = reviewScores?.reviewScoresRating?.toDouble() ?: 0.0

    val rating: Double
        get() = averageRating

    val imageUrl: String
        get() = images?.pictureUrl ?: ""

    val hostImageUrl: String
        get() = host?.hostPictureUrl ?: ""

    val hostName: String
        get() = host?.hostName ?: "Unknown Host"

    val hostLocation: String?
        get() = host?.hostLocation

    val hostAbout: String?
        get() = host?.hostAbout

    val hostResponseTime: String?
        get() = host?.hostResponseTime

    val hostResponseRate: Double?
        get() = host?.hostResponseRate?.toDouble()

    val hostIsSuperhost: Boolean
        get() = host?.hostIsSuperhost ?: false

    val hostIdentityVerified: Boolean
        get() = host?.hostIdentityVerified ?: false

    val hostListingsCount: Int
        get() = host?.hostListingsCount ?: 0

    val hostTotalListingsCount: Int
        get() = host?.hostTotalListingsCount ?: 0

    val hostVerifications: List<String>
        get() = host?.hostVerifications ?: emptyList()

    override fun toString(): String {
        return """
            VacationHome(
                id=$id,
                listingId=$listingId,
                name='$name',
                price=$price,
                location='$location',
                propertyType='$propertyType'
            )
        """.trimIndent()
    }
}

data class Address(
    @SerializedName("street")
    val street: String?,
    @SerializedName("suburb")
    val suburb: String?,
    @SerializedName("government_area")
    val governmentArea: String?,
    @SerializedName("market")
    val market: String?,
    @SerializedName("country")
    val country: String?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("location")
    val location: Location?
)

data class Location(
    @SerializedName("type")
    val type: String?,
    @SerializedName("coordinates")
    val coordinates: List<Double>?,
    @SerializedName("is_location_exact")
    val isLocationExact: Boolean?
)

data class Images(
    @SerializedName("picture_url")
    val pictureUrl: String?,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("medium_url")
    val mediumUrl: String?,
    @SerializedName("xl_picture_url")
    val xlPictureUrl: String?
)

data class Availability(
    @SerializedName("availability_30")
    val availability30: Int?,
    @SerializedName("availability_60")
    val availability60: Int?,
    @SerializedName("availability_90")
    val availability90: Int?,
    @SerializedName("availability_365")
    val availability365: Int?
)

data class ReviewScores(
    @SerializedName("review_scores_accuracy")
    val reviewScoresAccuracy: Int,
    @SerializedName("review_scores_checkin")
    val reviewScoresCheckin: Int,
    @SerializedName("review_scores_cleanliness")
    val reviewScoresCleanliness: Int,
    @SerializedName("review_scores_communication")
    val reviewScoresCommunication: Int,
    @SerializedName("review_scores_location")
    val reviewScoresLocation: Int,
    @SerializedName("review_scores_rating")
    val reviewScoresRating: Int,
    @SerializedName("review_scores_value")
    val reviewScoresValue: Int
)
