package com.example.localfresh.adapters.comprador

import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.localfresh.databinding.ItemReviewBinding
import com.example.localfresh.model.comprador.reviews.ReviewItem

class ReviewsAdapter(
    private val showReportButton: Boolean = true
) : ListAdapter<ReviewItem, ReviewsAdapter.ReviewViewHolder>(DiffCallback()) {

    interface OnReviewReportClickListener {
        fun onReportClick(reviewId: Int)
    }

    private var reportClickListener: OnReviewReportClickListener? = null

    fun setOnReportClickListener(listener: OnReviewReportClickListener) {
        this.reportClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) : RecyclerView.ViewHolder(binding.root) {
        private var isExpanded = false

        fun bind(item: ReviewItem) {
            with(binding) {
                txtUsername.text = item.user_display_name ?: item.username
                txtDate.text = item.formatted_date
                ratingBar.rating = item.rating.toFloat()

                val comment = item.comment ?: "Sin comentarios"
                txtComment.text = comment

                if (comment.length > 100) {
                    if (!isExpanded) {
                        txtComment.maxLines = 2
                        txtComment.ellipsize = TextUtils.TruncateAt.END
                    } else {
                        txtComment.maxLines = Integer.MAX_VALUE
                        txtComment.ellipsize = null
                    }
                    btnExpandCollapse.visibility = View.VISIBLE
                    btnExpandCollapse.text = if (isExpanded) "Ver menos" else "Ver más"
                    btnExpandCollapse.setOnClickListener {
                        isExpanded = !isExpanded
                        if (isExpanded) {
                            txtComment.maxLines = Integer.MAX_VALUE
                            txtComment.ellipsize = null
                            btnExpandCollapse.text = "Ver menos"
                        } else {
                            txtComment.maxLines = 2
                            txtComment.ellipsize = TextUtils.TruncateAt.END
                            btnExpandCollapse.text = "Ver más"
                        }
                    }
                } else {
                    btnExpandCollapse.visibility = View.GONE
                }

                // Mostrar u ocultar el botón de reportar según el parámetro
                btnFlag.visibility = if (showReportButton) View.VISIBLE else View.GONE

                btnFlag.setOnClickListener {
                    Log.d("ReviewsAdapter", "Botón de reportar clickeado para review_id: ${item.review_id}")
                    reportClickListener?.onReportClick(item.review_id)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReviewItem>() {
        override fun areItemsTheSame(oldItem: ReviewItem, newItem: ReviewItem): Boolean {
            return oldItem.review_id == newItem.review_id
        }

        override fun areContentsTheSame(oldItem: ReviewItem, newItem: ReviewItem): Boolean {
            return oldItem == newItem
        }
    }
}