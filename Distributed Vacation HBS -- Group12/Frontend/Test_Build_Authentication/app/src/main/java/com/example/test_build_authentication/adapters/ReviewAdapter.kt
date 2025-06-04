/*
 * ReviewAdapter.kt
 *
 * RecyclerView adapter for displaying reviews in a list.
 */
package com.example.test_build_authentication.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test_build_authentication.R
import com.example.test_build_authentication.models.Review
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val onEditReview: (Review) -> Unit,
    private val onDeleteReview: (Review) -> Unit,
    private val isUserLoggedIn: Boolean = false,
    private val currentUserId: String? = null
) : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reviewerNameText: TextView = itemView.findViewById(R.id.reviewer_name_text)
        private val dateText: TextView = itemView.findViewById(R.id.date_text)
        private val commentsText: TextView = itemView.findViewById(R.id.comments_text)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)

        fun bind(review: Review) {
            reviewerNameText.text = review.reviewerName
            dateText.text = formatDate(review.date)
            commentsText.text = review.comments

            // Only show edit/delete if logged in AND user is the author
            if (isUserLoggedIn && currentUserId != null && currentUserId == review.reviewerId) {
                editButton.visibility = View.VISIBLE
                deleteButton.visibility = View.VISIBLE
                editButton.setOnClickListener { onEditReview(review) }
                deleteButton.setOnClickListener { onDeleteReview(review) }
            } else {
                editButton.visibility = View.GONE
                deleteButton.visibility = View.GONE
            }
        }

        private fun formatDate(date: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                date
            }
        }
    }

    private class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
} 