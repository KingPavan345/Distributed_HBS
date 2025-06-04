package com.example.test_build_authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test_build_authentication.adapters.VacationHomeAdapter
import com.example.test_build_authentication.api.ApiClient
import com.example.test_build_authentication.databinding.ActivityVacationHomeListBinding
import com.example.test_build_authentication.models.VacationHome
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.launch
import android.widget.SearchView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Button

/*
 * VacationHomeListActivity.kt
 *
 * Shows a list of available vacation homes, supports filtering and navigation to details.
 * Handles data loading, user interactions, and list updates.
 */

class VacationHomeListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVacationHomeListBinding
    private lateinit var adapter: VacationHomeAdapter
    
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreItems = true
    private var selectedPropertyType: String? = null
    private var minPrice: String? = null
    private var maxPrice: String? = null
    private var minBedrooms: String? = null
    private var selectedCountry: String? = null
    private var activeFilters: MutableMap<String, String> = mutableMapOf()
    private var sortBy: String? = null
    private lateinit var homeButton: ImageButton

    companion object {
        private const val TAG = "VacationHomeListActivity"
        private const val ITEMS_PER_PAGE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVacationHomeListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSortChips()
        setupPaginationControls()
        binding.retryButton.setOnClickListener {
            binding.retryButton.visibility = View.GONE
            binding.statusText.visibility = View.GONE
            loadListings()
        }

        // Filter button opens filter dialog
        findViewById<ImageButton>(R.id.filter_button).setOnClickListener {
            val dialog = FilterDialogFragment.newInstance(activeFilters) // To be implemented
            dialog.setFilterListener { filters ->
                activeFilters.clear()
                activeFilters.putAll(filters)
                currentPage = 1
                loadListings()
            }
            dialog.show(supportFragmentManager, "FilterDialog")
        }

        homeButton = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            // Reset filters, sort, and search to initial state
            activeFilters.clear()
            sortBy = null
            currentPage = 1
            updateFilterChips()
            // Optionally, clear sort chip selection
            for (i in 0 until binding.sortChipGroup.childCount) {
                val chip = binding.sortChipGroup.getChildAt(i)
                if (chip is com.google.android.material.chip.Chip) {
                    chip.isChecked = false
                }
            }
            loadListings()
        }

        val logoutButton: ImageButton = findViewById(R.id.logout_button)
        logoutButton.setOnClickListener {
            // Handle logout logic (e.g., clear user session, go to login screen)
            performLogout()
        }

        loadListings()
    }

    private fun setupRecyclerView() {
        adapter = VacationHomeAdapter(this) { vacationHome ->
            // Handle item click
            Log.d(TAG, "Item clicked: ${vacationHome.name}")
            
            if (vacationHome.hasValidId) {
                val intent = Intent(this, VacationHomeDetailActivity::class.java)
                intent.putExtra("vacation_home_id", vacationHome.listingId)
                startActivity(intent)
            } else {
                Log.e(TAG, "No valid ID found for vacation home: ${vacationHome.name}")
                Toast.makeText(this, "Cannot view details: Invalid listing ID", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@VacationHomeListActivity)
            adapter = this@VacationHomeListActivity.adapter
        }
        
        binding.recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
    }

    private fun setupSortChips() {
        // Add sort chips for different sorting options
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
                        // Apply sorting
                        sortBy = when (option) {
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
            binding.sortChipGroup.addView(chip)
        }
    }

    private fun setupPaginationControls() {
        binding.prevPageButton.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                loadListings()
            }
        }
        binding.nextPageButton.setOnClickListener {
            currentPage++
            loadListings()
        }
        updatePaginationControls(1, 1, false, false)
    }

    private fun updatePaginationControls(current: Int, total: Int, hasPrev: Boolean, hasNext: Boolean) {
        binding.pageIndicator.text = "Page $current of $total"
        binding.prevPageButton.isEnabled = hasPrev
        binding.nextPageButton.isEnabled = hasNext
    }

    private fun loadListings() {
        showLoading(true)
        binding.retryButton.visibility = View.GONE
        binding.statusText.visibility = View.GONE
        var attempt = 0
        val maxAttempts = 3
        fun tryLoad() {
            lifecycleScope.launch {
                try {
                    val response = ApiClient.vacationHomeService.getListings(
                        page = currentPage,
                        propertyType = activeFilters["property_type"],
                        country = activeFilters["country"],
                        minPrice = activeFilters["min_price"]?.toDoubleOrNull(),
                        maxPrice = activeFilters["max_price"]?.toDoubleOrNull(),
                        minBedrooms = activeFilters["min_bedrooms"]?.toIntOrNull(),
                        sortBy = sortBy
                    )
                    if (response.isSuccessful) {
                        val listResponse = response.body()
                        if (listResponse != null) {
                            adapter.submitList(listResponse.data)
                            hasMoreItems = listResponse.pagination.hasNext
                            currentPage = listResponse.pagination.currentPage
                            val totalPages = listResponse.pagination.totalPages
                            updatePaginationControls(currentPage, totalPages, currentPage > 1, hasMoreItems)
                            if (listResponse.data.isEmpty()) {
                                val country = activeFilters["country"]
                                if (!country.isNullOrBlank()) {
                                    binding.statusText.text = "No listings available for this country."
                                } else {
                                    binding.statusText.text = "No listings found."
                                }
                                binding.statusText.visibility = View.VISIBLE
                            } else {
                                binding.statusText.visibility = View.GONE
                            }
                        } else {
                            showMessage("No listings found")
                            updatePaginationControls(1, 1, false, false)
                        }
                        showLoading(false)
                    } else {
                        showError("Error loading listings: ${response.code()}")
                        showLoading(false)
                    }
                } catch (e: Exception) {
                    attempt++
                    if (attempt < maxAttempts) {
                        Toast.makeText(this@VacationHomeListActivity, "Network error, retrying... (${e.message})", Toast.LENGTH_SHORT).show()
                        binding.recyclerView.postDelayed({ tryLoad() }, 1500)
                    } else {
                        showError("Error loading listings: ${e.message ?: "end of stream, try again"}")
                        showLoading(false)
                        binding.retryButton.visibility = View.VISIBLE
                    }
                }
            }
        }
        tryLoad()
    }

    private fun loadMoreItems() {
        if (!isLoading && hasMoreItems) {
            isLoading = true
            currentPage++
            showLoadingMore(true)
            lifecycleScope.launch {
                try {
                    val response = ApiClient.vacationHomeService.getListings(
                        page = currentPage,
                        propertyType = selectedPropertyType,
                        country = selectedCountry,
                        minPrice = minPrice?.toDoubleOrNull(),
                        maxPrice = maxPrice?.toDoubleOrNull(),
                        minBedrooms = minBedrooms?.toIntOrNull(),
                        sortBy = sortBy
                    )
                    if (response.isSuccessful) {
                        val listResponse = response.body()
                        if (listResponse != null && listResponse.data.isNotEmpty()) {
                            adapter.appendList(listResponse.data)
                            hasMoreItems = listResponse.pagination.hasNext
                        } else {
                            hasMoreItems = false
                        }
                    } else {
                        showError("Error loading more listings: ${response.code()}")
                    }
                } catch (e: Exception) {
                    showError("Failed to load more listings: ${e.message}")
                } finally {
                    isLoading = false
                    showLoadingMore(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.statusText.visibility = View.GONE
        }
    }

    private fun showLoadingMore(show: Boolean) {
        binding.bottomProgressBar.visibility = if (show) View.VISIBLE else View.GONE
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
        binding.statusText.visibility = View.VISIBLE
        binding.statusText.text = message
        binding.retryButton.visibility = View.VISIBLE
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateFilterChips() {
        val chipContainer = findViewById<LinearLayout>(R.id.filter_chip_container)
        chipContainer.removeAllViews()
        for ((key, value) in activeFilters) {
            if (value.isNotBlank()) {
                val chip = com.google.android.material.chip.Chip(this).apply {
                    text = "$key: $value"
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        activeFilters.remove(key)
                        updateFilterChips()
                        currentPage = 1
                        loadListings()
                    }
                }
                chipContainer.addView(chip)
            }
        }
    }

    private fun performLogout() {
        // Clear user session (e.g., clear shared preferences)
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
