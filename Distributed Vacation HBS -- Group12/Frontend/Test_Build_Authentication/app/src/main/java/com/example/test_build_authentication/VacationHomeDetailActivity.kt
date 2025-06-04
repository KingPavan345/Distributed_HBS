/*
 * VacationHomeDetailActivity.kt
 *
 * Displays detailed information about a selected vacation home, including images, reviews, and booking options.
 * Handles user actions related to a specific vacation home.
 *
 * Authors: Pavan Sai Kappiri
 */
package com.example.test_build_authentication

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.test_build_authentication.adapters.ImagePagerAdapter
import com.example.test_build_authentication.adapters.ReviewAdapter
import com.example.test_build_authentication.databinding.ActivityVacationHomeDetailBinding
import com.example.test_build_authentication.models.Review
import com.example.test_build_authentication.models.Pagination
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.app.AlertDialog
import android.widget.EditText
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VacationHomeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVacationHomeDetailBinding
    private lateinit var viewModel: VacationHomeDetailViewModel
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private lateinit var reviewAdapter: ReviewAdapter
    private var vacationHomeId: String? = null
    private var currentReviewPage = 1
    private var totalReviewPages = 1
    private var isLoadingReviews = false
    private var hasMoreReviewPages = true
    private var userReview: Review? = null

    companion object {
        private const val TAG = "VacationHomeDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVacationHomeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[VacationHomeDetailViewModel::class.java]
        setupViews()
        setupToolbar()

        vacationHomeId = intent.getStringExtra("vacation_home_id")
        Log.d(TAG, "DetailActivity: Received vacation_home_id: $vacationHomeId")
        if (vacationHomeId == null) {
            Toast.makeText(this, "Error: No listing ID provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupObservers()
        Log.d(TAG, "DetailActivity: Calling viewModel.loadVacationHome($vacationHomeId)")
        viewModel.loadVacationHome(vacationHomeId!!)

        binding.loadReviewsButton.setOnClickListener {
            currentReviewPage = 1
            hasMoreReviewPages = true
            reviewAdapter.submitList(emptyList())
            viewModel.loadReviews(vacationHomeId!!, currentReviewPage)
        }
        binding.reviewsRecyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoadingReviews && hasMoreReviewPages) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2 && firstVisibleItemPosition >= 0) {
                        loadMoreReviews()
                    }
                }
            }
        })
    }

    private fun setupViews() {
        // Determine login state and userId
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val encryptedPrefs = EncryptedSharedPreferences.create(
            "secure_prefs",
            "secure_prefs",
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val token = encryptedPrefs.getString("auth_token", null)
        val userId = encryptedPrefs.getString("user_id", null)
        val isUserLoggedIn = !token.isNullOrEmpty()

        // Setup reviews recycler view
        reviewAdapter = ReviewAdapter(
            onEditReview = { review -> editReview(review) },
            onDeleteReview = { review -> deleteReview(review) },
            isUserLoggedIn = isUserLoggedIn,
            currentUserId = userId
        )
        binding.reviewsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@VacationHomeDetailActivity)
            adapter = reviewAdapter
        }

        // Setup image pager
        imagePagerAdapter = ImagePagerAdapter(emptyList())
        binding.viewPager.adapter = imagePagerAdapter
        
        // Setup tab layout
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    binding.viewPager.currentItem = position
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tabLayout.getTabAt(position)?.select()
            }
        })

        val fab = findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fab_edit_review)
        fab.visibility = View.GONE // Always hide the add/edit review button
        binding.addReviewButton.visibility = View.GONE // Always hide the old button
    }

    private fun setupObservers() {
        viewModel.vacationHome.observe(this) { vacationHome ->
            vacationHome?.let { homeData ->
                binding.apply {
                    titleText.text = homeData.name
                    priceText.text = getString(R.string.price_format, homeData.price)
                    descriptionText.text = homeData.description
                    locationText.text = homeData.location ?: "N/A"
                    ratingBar.rating = (homeData.rating ?: 0.0).toFloat()
                    reviewCountText.text = getString(R.string.review_count_format, homeData.numberOfReviews)
                    hostNameText.text = getString(R.string.hosted_by_format, homeData.hostName)
                    houseRulesText.text = homeData.houseRules?.takeIf { it.isNotBlank() } ?: "No house rules specified."

                    // Update images
                    val imageUrls = if (!homeData.imageUrl.isNullOrEmpty()) {
                        listOf(homeData.imageUrl)
                    } else {
                        emptyList()
                    }
                    imagePagerAdapter = ImagePagerAdapter(imageUrls)
                    binding.viewPager.adapter = imagePagerAdapter

                    Glide.with(this@VacationHomeDetailActivity)
                        .load(homeData.hostImageUrl)
                        .transform(CircleCrop())
                        .into(hostImage)

                    // Add amenities chips
                    amenitiesChipGroup.removeAllViews()
                    homeData.amenities?.forEach { amenity ->
                        val chip = Chip(this@VacationHomeDetailActivity).apply {
                            text = amenity
                            isCheckable = false
                        }
                        amenitiesChipGroup.addView(chip)
                    }

                    // Update reviews
                    reviewAdapter.submitList(homeData.reviews)
                }
            }
        }

        viewModel.reviews.observe(this) { reviews ->
            android.util.Log.d("DetailActivity", "Reviews loaded: ${reviews.size}")
            if (currentReviewPage == 1) {
                reviewAdapter.submitList(reviews)
            } else {
                val currentList = ArrayList(reviewAdapter.currentList)
                currentList.addAll(reviews)
                reviewAdapter.submitList(currentList)
            }
            isLoadingReviews = false
            binding.reviewBottomProgressBar.visibility = View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.reviewPagination.observe(this) { pagination ->
            pagination?.let {
                totalReviewPages = it.totalPages
                hasMoreReviewPages = it.hasNext
            }
        }

        viewModel.reviewOperationStatus.observe(this) { status ->
            status?.let {
                when {
                    it.contains("added successfully", ignoreCase = true) -> {
                        Toast.makeText(this, "Review added!", Toast.LENGTH_SHORT).show()
                    }
                    it.contains("deleted successfully", ignoreCase = true) -> {
                        Toast.makeText(this, "Review deleted!", Toast.LENGTH_SHORT).show()
                    }
                    it.contains("updated successfully", ignoreCase = true) -> {
                        Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show()
                    }
                    it.isNotBlank() -> {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Vacation Home Details"
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun editReview(review: Review) {
        val encryptedPrefs = EncryptedSharedPreferences.create(
            "secure_prefs", "secure_prefs", this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val token = encryptedPrefs.getString("auth_token", null)
        val userId = encryptedPrefs.getString("user_id", null)
        if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "You must be logged in to edit a review.", Toast.LENGTH_SHORT).show()
            return
        }
        if (review.reviewerId != userId) {
            Toast.makeText(this, "You can only edit your own reviews.", Toast.LENGTH_SHORT).show()
            return
        }
        val editText = EditText(this)
        editText.setText(review.comments)
        AlertDialog.Builder(this)
            .setTitle("Edit Review")
            .setView(editText)
            .setPositiveButton("Update") { _, _ ->
                val updatedReview = review.copy(
                    comments = editText.text.toString(),
                    date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
                )
                viewModel.updateReview(vacationHomeId!!, review.id, updatedReview, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteReview(review: Review) {
        val encryptedPrefs = EncryptedSharedPreferences.create(
            "secure_prefs", "secure_prefs", this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val token = encryptedPrefs.getString("auth_token", null)
        val userId = encryptedPrefs.getString("user_id", null)
        if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "You must be logged in to delete a review.", Toast.LENGTH_SHORT).show()
            return
        }
        if (review.reviewerId != userId) {
            Toast.makeText(this, "You can only delete your own reviews.", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Delete Review")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteReview(vacationHomeId!!, review.id, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadMoreReviews() {
        if (!isLoadingReviews && hasMoreReviewPages) {
            isLoadingReviews = true
            binding.reviewBottomProgressBar.visibility = View.VISIBLE
            currentReviewPage++
            viewModel.loadReviews(vacationHomeId!!, currentReviewPage)
        }
    }

    private fun addReview() {
        val encryptedPrefs = EncryptedSharedPreferences.create(
            "secure_prefs", "secure_prefs", this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val token = encryptedPrefs.getString("auth_token", null)
        val userId = encryptedPrefs.getString("user_id", null)
        if (token.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "You must be logged in to add a review.", Toast.LENGTH_SHORT).show()
            return
        }
        val editText = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add Review")
            .setView(editText)
            .setPositiveButton("Submit") { _, _ ->
                val reviewText = editText.text.toString()
                val review = com.example.test_build_authentication.models.Review(
                    id = java.util.UUID.randomUUID().toString(),
                    comments = reviewText,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
                    listingId = vacationHomeId!!,
                    reviewerId = userId,
                    reviewerName = "You" // Replace with actual user name if available
                )
                viewModel.addReview(vacationHomeId!!, review, token)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}


