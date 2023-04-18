package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.root.RootFolder
import com.ntduc.baseproject.databinding.ItemRootFolderBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.view.invisible
import com.ntduc.baseproject.utils.view.visible
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class RootFolderAdapter(
    val context: Context
) : BindingListAdapter<RootFolder, RootFolderAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemRootFolderBinding>(R.layout.item_root_folder).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    inner class ItemViewHolder constructor(
        private val binding: ItemRootFolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(rootFolder: RootFolder, position: Int) {
            binding.txtRoot.text = rootFolder.name
            binding.root.setOnClickListener {
                val newList = arrayListOf<RootFolder>()
                newList.addAll(currentList)
                if (position != newList.size - 1) {
                    do {
                        newList.removeAt(position + 1)
                    } while (newList.size > position + 1)
                    submitList(newList)

                    onClickListener?.let {
                        it(rootFolder)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<RootFolder>() {
            override fun areItemsTheSame(oldItem: RootFolder, newItem: RootFolder): Boolean =
                oldItem.path == newItem.path

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: RootFolder, newItem: RootFolder): Boolean =
                oldItem == newItem
        }
    }

    private var onClickListener: ((RootFolder) -> Unit)? = null

    fun setOnClickListener(listener: (RootFolder) -> Unit) {
        onClickListener = listener
    }
}