package com.example.badger.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.badger.data.model.SharedList
import com.example.badger.databinding.ItemListBinding

class ListAdapter(
    private val onListClick: (SharedList) -> Unit,
    private val onFavoriteClick: (SharedList, Boolean) -> Unit
) : ListAdapter<SharedList, ListAdapter.ListViewHolder>(ListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ListViewHolder(
        private val binding: ItemListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onListClick(getItem(position))
                }
            }

            binding.favoriteButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val list = getItem(position)
                    onFavoriteClick(list, !list.isFavorite)
                }
            }
        }

        fun bind(list: SharedList) {
            binding.titleText.text = list.title
            binding.itemCountText.text = "${list.items.size} items"
            binding.favoriteButton.setImageResource(
                if (list.isFavorite) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )
        }
    }
}

private class ListDiffCallback : DiffUtil.ItemCallback<SharedList>() {
    override fun areItemsTheSame(oldItem: SharedList, newItem: SharedList): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SharedList, newItem: SharedList): Boolean {
        return oldItem == newItem
    }
}