/*
 * DemoListingActivity.kt
 *
 * Demonstrates a simple listing of vacation homes for demo or testing purposes.
 * Useful for UI previews and development.
 */
package com.example.test_build_authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test_build_authentication.adapters.VacationHomeAdapter
import com.example.test_build_authentication.api.ApiClient
import com.example.test_build_authentication.databinding.ActivityDemoListingBinding
import com.example.test_build_authentication.models.VacationHome
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DemoListingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDemoListingBinding
    private lateinit var adapter: VacationHomeAdapter
    private val viewModel: DemoListingViewModel by viewModels()

    companion object {
        private const val TAG = "DemoListingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDemoListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        loadListings()
    }

    private fun setupRecyclerView() {
        adapter = VacationHomeAdapter(this) { vacationHome ->
            // Handle item click
            val intent = Intent(this, VacationHomeDetailActivity::class.java).apply {
                putExtra("vacation_home_id", vacationHome.listingId)
            }
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DemoListingActivity)
            adapter = this@DemoListingActivity.adapter
        }
    }

    private fun setupObservers() {
        viewModel.listings.observe(this) { listings ->
            adapter.submitList(listings)
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadListings() {
        viewModel.loadListings()
    }
}

class DemoListingViewModel : ViewModel() {
    private val _listings = MutableLiveData<List<VacationHome>>()
    val listings: LiveData<List<VacationHome>> = _listings

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val viewModelJob = SupervisorJob()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun loadListings() {
        viewModelScope.launch {
            try {
                val response = ApiClient.vacationHomeService.getListings()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        _listings.value = apiResponse.data
                    } ?: run {
                        _error.value = "No data received from server"
                    }
                } else {
                    _error.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error loading listings: ${e.message}"
            }
        }
    }
}
