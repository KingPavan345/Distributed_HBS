/*
 * VacationHomeAdapter.kt
 *
 * RecyclerView adapter for displaying vacation home items in a list.
 */
package com.example.test_build_authentication.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.test_build_authentication.R
import com.example.test_build_authentication.VacationHomeDetailActivity
import com.example.test_build_authentication.models.VacationHome

class VacationHomeAdapter(
    private val context: Context,
    private val onItemClick: (VacationHome) -> Unit
) : ListAdapter<VacationHome, VacationHomeAdapter.ViewHolder>(VacationHomeDiffCallback()) {

    companion object {
        private const val TAG = "VacationHomeAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vacation_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val vacationHome = getItem(position)
        try {
            val currentListingId = vacationHome.listingId
            Log.d(TAG, """
                Binding vacation home at position $position:
                - Name: ${vacationHome.name}
                - Listing URL: ${vacationHome.listingUrl}
                - Listing ID: $currentListingId
            """.trimIndent())

            holder.bind(vacationHome)
        } catch (e: Exception) {
            Log.e(TAG, "Error binding vacation home at position $position: ${vacationHome.name}", e)
            holder.showErrorState()
        }
    }

    fun appendList(newItems: List<VacationHome>) {
        val currentList = ArrayList(currentList)
        currentList.addAll(newItems)
        submitList(currentList)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.main_image)
        private val nameTextView: TextView = itemView.findViewById(R.id.title_text)
        private val priceTextView: TextView = itemView.findViewById(R.id.price_text)
        private val locationTextView: TextView = itemView.findViewById(R.id.location_text)
        private val ratingTextView: TextView = itemView.findViewById(R.id.rating_text)

        fun showErrorState() {
            nameTextView.text = "Error loading listing"
            priceTextView.text = ""
            locationTextView.text = ""
            ratingTextView.text = ""
            imageView.setImageResource(R.drawable.placeholder_image)
            itemView.setOnClickListener(null)
        }

        fun bind(vacationHome: VacationHome) {
            try {
                val currentListingId = vacationHome.listingId
                val currentListingUrl = vacationHome.listingUrl
                Log.d(TAG, """
                    Binding vacation home:
                    - Name: ${vacationHome.name}
                    - PictureURL: ${vacationHome.pictureUrl}
                    - Listing URL: $currentListingUrl
                    - Listing ID: $currentListingId
                """.trimIndent())

                nameTextView.text = vacationHome.name
                priceTextView.text = "£${vacationHome.price}/night"
                locationTextView.text = vacationHome.location ?: "Location not available"
                ratingTextView.text = vacationHome.rating?.let { "★ ${it}" } ?: "No ratings"

                // Load image with error handling
                val imageUrl = vacationHome.pictureUrl ?: ""
                if (imageUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(imageUrl)
                        .centerCrop()
                        .error(R.drawable.placeholder_image)
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.placeholder_image)
                }

                itemView.setOnClickListener {
                    try {
                        val id = vacationHome.listingId ?: vacationHome.id
                        Log.d(TAG, "Adapter: Passing to detail activity: listingId=${vacationHome.listingId}, _id=${vacationHome.id}")
                        if (!id.isNullOrEmpty()) {
                            onItemClick(vacationHome)
                        } else {
                            Log.w(TAG, "Cannot view details for vacation home without valid listing ID: ${vacationHome.name} (listingId: ${vacationHome.listingId}, _id: ${vacationHome.id})")
                            Toast.makeText(context, "Error: Unable to view listing details", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error handling click for vacation home: ${vacationHome.name}", e)
                        Toast.makeText(context, "Error: Unable to view listing details", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error binding vacation home: ${vacationHome.name}", e)
                showErrorState()
            }
        }
    }
}

class VacationHomeDiffCallback : DiffUtil.ItemCallback<VacationHome>() {
    override fun areItemsTheSame(oldItem: VacationHome, newItem: VacationHome): Boolean {
        val oldId = oldItem.id
        val newId = newItem.id
        val oldListingId = oldItem.listingId
        val newListingId = newItem.listingId
        
        return oldId == newId || oldListingId == newListingId
    }

    override fun areContentsTheSame(oldItem: VacationHome, newItem: VacationHome): Boolean {
        return oldItem == newItem
    }
}
