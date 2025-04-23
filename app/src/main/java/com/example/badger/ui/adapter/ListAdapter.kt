package com.example.badger.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.badger.data.model.SharedList

class ListAdapter(
    private val onListClick: (SharedList) -> Unit,
    private val onFavoriteClick: (SharedList, Boolean) -> Unit
) : ListAdapter<SharedList, ListViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder.from(parent, onListClick, onFavoriteClick)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SharedList>() {
            override fun areItemsTheSame(oldItem: SharedList, newItem: SharedList): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SharedList, newItem: SharedList): Boolean {
                return oldItem == newItem
            }
        }
    }
}
