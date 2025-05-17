package com.example.localfresh.adapters.vendedor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.R
import com.example.localfresh.model.comprador.reviews.ReviewItem
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewsVendedorAdapter : RecyclerView.Adapter<ReviewsVendedorAdapter.ReviewViewHolder>() {
    private var reviews = emptyList<ReviewItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review_vendedor, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size

    fun updateReviews(newReviews: List<ReviewItem>) {
        this.reviews = newReviews
        notifyDataSetChanged()
    }

    class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val reviewerName: TextView = view.findViewById(R.id.reviewerName)
        private val reviewRating: RatingBar = view.findViewById(R.id.reviewRating)
        private val reviewComment: TextView = view.findViewById(R.id.reviewComment)
        private val reviewDate: TextView = view.findViewById(R.id.reviewDate)

        fun bind(review: ReviewItem) {
            reviewerName.text = review.user_display_name ?: review.username
            reviewRating.rating = review.rating.toFloat()
            reviewComment.text = review.comment

            // Formatear fecha
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            try {
                val parsedDate = inputFormat.parse(review.created_at)
                reviewDate.text = parsedDate?.let { outputFormat.format(it) }
            } catch (e: Exception) {
                reviewDate.text = review.created_at
            }
        }
    }
}
