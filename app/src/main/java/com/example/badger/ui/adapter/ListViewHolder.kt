package com.example.badger.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.badger.data.model.SharedList
import com.example.badger.databinding.ItemListBinding

class ListViewHolder private constructor(
    private val binding: ItemListBinding,
    private val onListClick: (SharedList) -> Unit,
    private val onFavoriteClick: (SharedList, Boolean) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(list: SharedList) {
        binding.titleText.text = list.title
        binding.itemCountText.text = "${list.items.size} items"
        binding.favoriteButton.setImageResource(
            if (list.isFavorite) android.R.drawable.star_big_on
            else android.R.drawable.star_big_off
        )

        binding.root.setOnClickListener { onListClick(list) }
        binding.favoriteButton.setOnClickListener { onFavoriteClick(list, !list.isFavorite) }
    }

    companion object {
        fun from(
            parent: ViewGroup,
            onListClick: (SharedList) -> Unit,
            onFavoriteClick: (SharedList, Boolean) -> Unit
        ): ListViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ItemListBinding.inflate(layoutInflater, parent, false)
            return ListViewHolder(binding, onListClick, onFavoriteClick)
        }
    }
}
