package com.example.localfresh.adapters.comprador.favoritos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.localfresh.R
import com.example.localfresh.databinding.ItemFavoriteStoreBinding
import com.example.localfresh.model.comprador.favoritos.StoreFavorite

class FavoritesStoreAdapter(
    private val onItemClick: (StoreFavorite) -> Unit
) : ListAdapter<StoreFavorite, FavoritesStoreAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavoriteStoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFavoriteStoreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(store: StoreFavorite) {
            binding.apply {
                tvStoreName.text = store.store_name
                tvStoreDescription.text = store.store_description

                // Cargar imagen con Glide
                Glide.with(ivStoreLogo.context)
                    .load(store.store_logo)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .into(ivStoreLogo)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoreFavorite>() {
            override fun areItemsTheSame(oldItem: StoreFavorite, newItem: StoreFavorite): Boolean {
                return oldItem.seller_id == newItem.seller_id
            }

            override fun areContentsTheSame(oldItem: StoreFavorite, newItem: StoreFavorite): Boolean {
                return oldItem == newItem
            }
        }
    }
}