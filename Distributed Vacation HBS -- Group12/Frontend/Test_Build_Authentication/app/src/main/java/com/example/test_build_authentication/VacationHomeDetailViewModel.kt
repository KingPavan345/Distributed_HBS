/*
 * VacationHomeDetailViewModel.kt
 *
 * ViewModel for managing the state and business logic of VacationHomeDetailActivity.
 * Handles data fetching, state updates, and exposes LiveData for the UI.
 */
package com.example.test_build_authentication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.test_build_authentication.api.ApiClient
import com.example.test_build_authentication.models.VacationHome
import com.example.test_build_authentication.models.ReviewListResponse
import com.example.test_build_authentication.models.Pagination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class VacationHomeDetailViewModel : ViewModel() {
    private val _vacationHome = MutableLiveData<VacationHome>()
    val vacationHome: LiveData<VacationHome> = _vacationHome

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _reviews = MutableLiveData<List<com.example.test_build_authentication.models.Review>>()
    val reviews: LiveData<List<com.example.test_build_authentication.models.Review>> = _reviews

    private val _reviewPagination = MutableLiveData<Pagination>()
    val reviewPagination: LiveData<Pagination> = _reviewPagination

    internal val _reviewOperationStatus = MutableLiveData<String?>()
    val reviewOperationStatus: LiveData<String?> = _reviewOperationStatus

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    companion object {
        private const val TAG = "VacationHomeDetailVM"
    }

    fun loadVacationHome(id: String) {
        Log.d("VacationHomeDetailVM", "Calling getHome with id: $id")
        viewModelScope.launch {
            var attempt = 0
            val maxAttempts = 3
            var success = false
            while (attempt < maxAttempts && !success) {
                try {
                    val response = ApiClient.vacationHomeService.getHome(id)
                    Log.d("VacationHomeDetailVM", "API called: /api/listing/$id/")
                    if (response.isSuccessful) {
                        response.body()?.let { homeResponse ->
                            Log.d("VacationHomeDetailVM", "API response: $homeResponse")
                            _vacationHome.value = homeResponse.data
                            success = true
                        } ?: run {
                            Log.e("VacationHomeDetailVM", "No data received from server (body null after parsing).")
                            _error.value = "No data received from server"
                        }
                    } else {
                        Log.e("VacationHomeDetailVM", "Error: ${response.code()} - ${response.message()}")
                        _error.value = "Error: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    Log.e("VacationHomeDetailVM", "Error loading vacation home: ${e.message}")
                    if (attempt == maxAttempts - 1) {
                        _error.value = "Error loading vacation home: ${e.message}"
                    }
                }
                if (!success) {
                    attempt++
                    if (attempt < maxAttempts) {
                        delay(1500)
                    }
                }
            }
        }
    }

    fun loadReviews(id: String, page: Int = 1, limit: Int = 10, sortNewestFirst: Boolean = false) {
        viewModelScope.launch {
            var attempt = 0
            val maxAttempts = 3
            var success = false
            while (attempt < maxAttempts && !success) {
                try {
                    val response = com.example.test_build_authentication.ApiClient.listingsService.getListingReviews(id, page, limit)
                    if (response.isSuccessful) {
                        val body = response.body()
                        var reviews = body?.reviews ?: emptyList()
                        if (sortNewestFirst) {
                            reviews = reviews.sortedByDescending { it.date }
                        }
                        _reviews.value = reviews
                        _reviewPagination.value = body?.pagination
                        success = true
                    } else {
                        _error.value = "Error loading reviews: ${response.code()} - ${response.message()}"
                    }
                } catch (e: Exception) {
                    if (attempt == maxAttempts - 1) {
                        _error.value = "Could not load reviews. Please check your connection and try again. (${e.message})"
                    }
                }
                if (!success) {
                    attempt++
                    if (attempt < maxAttempts) {
                        kotlinx.coroutines.delay(1500)
                    }
                }
            }
        }
    }

    fun addReview(listingId: String, review: com.example.test_build_authentication.models.Review, token: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Attempting to add review with token: $token for listing $listingId")
                Log.d(TAG, "Review data: $review")
                val response = com.example.test_build_authentication.api.ApiClient.vacationHomeService.addReview(listingId, review, "Bearer $token")
                if (response.isSuccessful) {
                    _reviewOperationStatus.value = "Review added successfully"
                    loadReviews(listingId, sortNewestFirst = true)
                } else {
                    _reviewOperationStatus.value = "Failed to add review: ${response.message()}"
                }
            } catch (e: Exception) {
                _reviewOperationStatus.value = "Error adding review: ${e.message}"
            }
        }
    }

    fun updateReview(listingId: String, reviewId: String, review: com.example.test_build_authentication.models.Review, token: String) {
        viewModelScope.launch {
            try {
                val response = com.example.test_build_authentication.api.ApiClient.vacationHomeService.updateReview(listingId, reviewId, review, "Bearer $token")
                if (response.isSuccessful) {
                    _reviewOperationStatus.value = "Review updated successfully"
                    loadReviews(listingId, sortNewestFirst = true)
                } else {
                    _reviewOperationStatus.value = "Failed to update review: ${response.message()}"
                }
            } catch (e: Exception) {
                _reviewOperationStatus.value = "Error updating review: ${e.message}"
            }
        }
    }

    fun deleteReview(listingId: String, reviewId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = com.example.test_build_authentication.api.ApiClient.vacationHomeService.deleteReview(listingId, reviewId, "Bearer $token")
                if (response.isSuccessful) {
                    _reviewOperationStatus.value = "Review deleted successfully"
                    loadReviews(listingId, sortNewestFirst = true)
                } else {
                    _reviewOperationStatus.value = "Failed to delete review: ${response.message()}"
                }
            } catch (e: Exception) {
                _reviewOperationStatus.value = "Error deleting review: ${e.message}"
            }
        }
    }
} 