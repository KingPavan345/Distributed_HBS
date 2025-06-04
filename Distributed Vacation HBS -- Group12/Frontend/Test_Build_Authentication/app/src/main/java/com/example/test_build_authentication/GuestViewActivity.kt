package com.example.test_build_authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test_build_authentication.adapters.VacationHomeAdapter
import com.example.test_build_authentication.ApiClient
import com.example.test_build_authentication.api.VacationHomeService
import com.example.test_build_authentication.models.VacationHome
import com.example.test_build_authentication.models.VacationHomeListResponse
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Button
import android.widget.ImageButton

/*
 * GuestViewActivity.kt
 *
 * Displays vacation homes to guest users. Handles guest browsing, viewing details, and filtering homes.
 *
 */

class GuestViewActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var sortChipGroup: ChipGroup
    private lateinit var retryButton: Button
    private lateinit var homeButton: ImageButton
    private lateinit var logoutButton: ImageButton
    
    private lateinit var adapter: VacationHomeAdapter
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreItems = true
    private var currentSortBy: String? = null
    private var chipFilters: Map<String, String> = emptyMap()
    private var dialogFilters: Map<String, String> = emptyMap()
    
    companion object {
        private const val ITEMS_PER_PAGE = 10
        private const val TAG = "GuestViewActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_view)

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view)
        progressBar = findViewById(R.id.progress_bar)
        statusText = findViewById(R.id.status_text)
        filterChipGroup = findViewById(R.id.filter_chip_group)
        sortChipGroup = findViewById(R.id.sort_chip_group)
        retryButton = findViewById(R.id.retry_button)
        homeButton = findViewById(R.id.home_button)
        logoutButton = findViewById(R.id.logout_button)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = VacationHomeAdapter(this) { vacationHome ->
            // Handle item click
            Log.d(TAG, "Item clicked: ${vacationHome.name}")
            // Always use MongoDB _id for detail activity
            val idToUse = vacationHome.id
            if (!idToUse.isNullOrEmpty()) {
                Log.d(TAG, "GuestViewActivity: Passing MongoDB _id: $idToUse")
                val intent = Intent(this, VacationHomeDetailActivity::class.java)
                intent.putExtra("vacation_home_id", idToUse)
                startActivity(intent)
            } else {
                Log.e(TAG, "No valid MongoDB _id found for vacation home: ${vacationHome.name}")
                Toast.makeText(this, "Cannot view details: Invalid listing ID", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter
        
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && hasMoreItems) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }
        })

        // Set up filter chips
        setupFilterChips()
        
        // Set up sort chips
        setupSortChips()

        // Load initial data
        loadListings()

        retryButton.setOnClickListener {
            retryButton.visibility = View.GONE
            statusText.visibility = View.GONE
            loadListings()
        }

        homeButton.setOnClickListener {
            // Reset filters, sort, and search to initial state
            chipFilters = emptyMap()
            dialogFilters = emptyMap()
            currentSortBy = null
            currentPage = 1
            // Optionally, clear sort chip selection
            for (i in 0 until sortChipGroup.childCount) {
                val chip = sortChipGroup.getChildAt(i)
                if (chip is com.google.android.material.chip.Chip) {
                    chip.isChecked = false
                }
            }
            // Optionally, clear filter chip selection
            for (i in 0 until filterChipGroup.childCount) {
                val chip = filterChipGroup.getChildAt(i)
                if (chip is com.google.android.material.chip.Chip) {
                    chip.isChecked = false
                }
            }
            loadListings()
        }

        logoutButton.setOnClickListener {
            // Navigate to LoginActivity and finish this activity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Add filter button logic for guests
        findViewById<ImageButton>(R.id.filter_button).setOnClickListener {
            val dialog = com.example.test_build_authentication.FilterDialogFragment.newInstance(dialogFilters)
            dialog.setFilterListener { filters ->
                dialogFilters = filters
                currentPage = 1
                loadListings()
            }
            dialog.show(supportFragmentManager, "FilterDialog")
        }
    }

    private fun setupFilterChips() {
        val priceRanges = listOf(
            getString(R.string.price_range_0_100),
            getString(R.string.price_range_100_200),
            getString(R.string.price_range_200_500),
            getString(R.string.price_range_500_plus)
        )
        priceRanges.forEach { range ->
            val chip = Chip(this).apply {
                text = range
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        val (min, max) = parsePriceRange(range)
                        chipFilters = mapOf("min_price" to min, "max_price" to max)
                        currentPage = 1
                        loadListings()
                    }
                }
            }
            filterChipGroup.addView(chip)
        }
    }

    private fun setupSortChips() {
        val sortOptions = listOf(
            getString(R.string.sort_price_low_high),
            getString(R.string.sort_price_high_low),
            getString(R.string.sort_rating),
            getString(R.string.sort_newest)
        )
        sortOptions.forEach { option ->
            val chip = Chip(this).apply {
                text = option
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        currentSortBy = when (option) {
                            getString(R.string.sort_price_low_high) -> "price_asc"
                            getString(R.string.sort_price_high_low) -> "price_desc"
                            getString(R.string.sort_rating) -> "rating_desc"
                            getString(R.string.sort_newest) -> "created_at_desc"
                            else -> null
                        }
                        currentPage = 1
                        loadListings()
                    }
                }
            }
            sortChipGroup.addView(chip)
        }
    }

    private fun loadListings() {
        if (isLoading) return
        isLoading = true
        showLoading(true)
        statusText.visibility = View.GONE
        var attempt = 0
        val maxAttempts = 3
        fun tryLoad() {
            lifecycleScope.launch {
                try {
                    Log.d(TAG, "Fetching listings from API... (attempt "+(attempt+1)+")")
                    // Merge chipFilters and dialogFilters, dialogFilters take precedence
                    val mergedFilters = chipFilters + dialogFilters
                    val response = ApiClient.listingsService.getListings(
                        page = currentPage,
                        propertyType = mergedFilters["property_type"],
                        country = mergedFilters["country"],
                        minPrice = mergedFilters["min_price"]?.toDoubleOrNull(),
                        maxPrice = mergedFilters["max_price"]?.toDoubleOrNull(),
                        minBedrooms = mergedFilters["min_bedrooms"]?.toIntOrNull(),
                        sortBy = currentSortBy
                    )
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val listResponse = response.body()
                            if (listResponse != null) {
                                val responseHeaders = response.headers()
                                Log.d(TAG, "API Response Headers: $responseHeaders")
                                Log.d(TAG, "API Response: ${listResponse.data.size} listings received")
                                Log.d(TAG, "Pagination: currentPage=${listResponse.pagination.currentPage}, " +
                                        "totalPages=${listResponse.pagination.totalPages}, " +
                                        "hasNext=${listResponse.pagination.hasNext}")
                                Log.d(TAG, "Received VacationHomeListResponse data: ${listResponse.data}")
                                if (currentPage == 1) {
                                    adapter.submitList(listResponse.data)
                                } else {
                                    val currentList = adapter.currentList.toMutableList()
                                    currentList.addAll(listResponse.data)
                                    adapter.submitList(currentList)
                                }
                                hasMoreItems = listResponse.pagination.hasNext
                                statusText.visibility = View.GONE
                                if (listResponse.data.isEmpty()) {
                                    val country = (chipFilters + dialogFilters)["country"]
                                    if (!country.isNullOrBlank()) {
                                        statusText.text = "No listings available for this country."
                                    } else {
                                        statusText.text = "No listings found."
                                    }
                                    statusText.visibility = View.VISIBLE
                                } else {
                                    statusText.visibility = View.GONE
                                }
                            } else {
                                showError(getString(R.string.error_loading_listings, "No data received"))
                            }
                            showLoading(false)
                            isLoading = false
                        } else {
                            showError(getString(R.string.error_loading_listings, response.message()))
                            showLoading(false)
                            isLoading = false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading listings: ${e.message}")
                    attempt++
                    if (attempt < maxAttempts) {
                        Toast.makeText(this@GuestViewActivity, "Network error, retrying... (${e.message})", Toast.LENGTH_SHORT).show()
                        recyclerView.postDelayed({ tryLoad() }, 1500)
                    } else {
                        showError(getString(R.string.error_loading_listings, e.message ?: "end of stream, try again"))
                        showLoading(false)
                        isLoading = false
                        showRetryButton()
                    }
                }
            }
        }
        tryLoad()
    }

    private fun showRetryButton() {
        statusText.visibility = View.VISIBLE
        statusText.text = getString(R.string.error_loading_listings, "end of stream, try again")
        retryButton.visibility = View.VISIBLE
    }

    private fun loadMoreItems() {
        if (!isLoading && hasMoreItems) {
            isLoading = true
            currentPage++
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val response = ApiClient.listingsService.getListings(
                        page = currentPage,
                        propertyType = chipFilters["property_type"],
                        country = chipFilters["country"],
                        minPrice = chipFilters["min_price"]?.toDoubleOrNull(),
                        maxPrice = chipFilters["max_price"]?.toDoubleOrNull(),
                        minBedrooms = chipFilters["min_bedrooms"]?.toIntOrNull(),
                        sortBy = currentSortBy
                    )
                    
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val listResponse = response.body()
                            if (listResponse != null) {
                                // Log response headers
                                val responseHeaders = response.headers()
                                Log.d(TAG, "API Response Headers: $responseHeaders")

                                Log.d(TAG, "API Response: ${listResponse.data.size} listings received")
                                Log.d(TAG, "Pagination: currentPage=${listResponse.pagination.currentPage}, " +
                                        "totalPages=${listResponse.pagination.totalPages}, " +
                                        "hasNext=${listResponse.pagination.hasNext}")

                                // Log the received list data for debugging
                                Log.d(TAG, "Received VacationHomeListResponse data: ${listResponse.data}")

                                if (currentPage == 1) {
                                    adapter.submitList(listResponse.data)
                                } else {
                                    val currentList = adapter.currentList.toMutableList()
                                    currentList.addAll(listResponse.data)
                                    adapter.submitList(currentList)
                                }
                                
                                hasMoreItems = listResponse.pagination.hasNext
                                currentPage = listResponse.pagination.currentPage
                                statusText.visibility = View.GONE
                                
                                // Log each listing for debugging
                                listResponse.data.forEach { home ->
                                    Log.d(TAG, "Listing: id=${home.id}, name=${home.name}, " +
                                            "price=${home.price}, location=${home.location}")
                                }
                            } else {
                                Log.e(TAG, "API Response body is null")
                                statusText.text = getString(R.string.no_listings_found)
                                statusText.visibility = View.VISIBLE
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            Log.e(TAG, "API Error: ${response.code()} - $errorBody")
                            val errorMessage = when {
                                errorBody?.contains("Connection was interrupted") == true -> "Connection was interrupted. Please check your internet connection and try again."
                                errorBody?.contains("timed out") == true -> "Request timed out. Please check your internet connection and try again."
                                errorBody?.contains("No network") == true -> "No internet connection available. Please check your network settings."
                                else -> getString(R.string.error_loading_listings, errorBody)
                            }
                            statusText.text = errorMessage
                            statusText.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading more listings", e)
                    withContext(Dispatchers.Main) {
                        statusText.text = getString(R.string.error_loading_listings, e.message)
                        statusText.visibility = View.VISIBLE
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        isLoading = false
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            statusText.visibility = View.GONE
        }
    }

    private fun parsePriceRange(range: String): Pair<String, String> {
        return when (range) {
            getString(R.string.price_range_0_100) -> "0" to "100"
            getString(R.string.price_range_100_200) -> "100" to "200"
            getString(R.string.price_range_200_500) -> "200" to "500"
            getString(R.string.price_range_500_plus) -> "500" to ""
            else -> "" to ""
        }
    }

    private fun showError(message: String) {
        statusText.visibility = View.VISIBLE
        statusText.text = message
        retryButton.visibility = View.VISIBLE
    }
} 