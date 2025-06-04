/*
 * VacationHomeService.kt
 *
 * Defines API endpoints and methods for vacation home-related network operations.
 */
package com.example.test_build_authentication.api

import com.example.test_build_authentication.models.Review
import com.example.test_build_authentication.models.VacationHome
import com.example.test_build_authentication.models.VacationHomeListResponse
import retrofit2.Response
import retrofit2.http.*

interface VacationHomeService {
    @GET("api/listings/")
    @Headers("Accept: application/json")
    suspend fun getListings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("property_type") propertyType: String? = null,
        @Query("country") country: String? = null,
        @Query("min_price") minPrice: Double? = null,
        @Query("max_price") maxPrice: Double? = null,
        @Query("min_bedrooms") minBedrooms: Int? = null,
        @Query("sort_by") sortBy: String? = null
    ): Response<VacationHomeListResponse>

    @GET("api/listing/{id}/")
    @Headers("Accept: application/json")
    suspend fun getHome(
        @Path("id") id: String
    ): Response<com.example.test_build_authentication.models.VacationHomeResponse>

    @GET("api/listing/{id}/reviews/")
    @Headers("Accept: application/json")
    suspend fun getListingReviews(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<com.example.test_build_authentication.models.ReviewListResponse>

    @POST("api/listing/{id}/review/")
    @Headers("Accept: application/json")
    suspend fun addReview(
        @Path("id") id: String,
        @Body review: Review,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @PUT("api/listing/{id}/review/{reviewId}/")
    @Headers("Accept: application/json")
    suspend fun updateReview(
        @Path("id") id: String,
        @Path("reviewId") reviewId: String,
        @Body review: Review,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>

    @DELETE("api/listing/{id}/review/{reviewId}/")
    @Headers("Accept: application/json")
    suspend fun deleteReview(
        @Path("id") id: String,
        @Path("reviewId") reviewId: String,
        @Header("Authorization") token: String
    ): Response<Map<String, String>>
}
