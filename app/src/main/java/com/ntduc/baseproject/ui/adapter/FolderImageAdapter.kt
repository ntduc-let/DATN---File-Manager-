package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.folder.FolderImageFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class FolderImageAdapter(
    val context: Context
) : BindingListAdapter<FolderImageFile, FolderImageAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderImageFile: FolderImageFile) {

            binding.favorite.gone()
            binding.ic.setImageResource(R.drawable.ic_folder_24dp)
            binding.title.text = folderImageFile.baseFile?.displayName
            binding.description.text = "${folderImageFile.listFile.size} item ∙ ${folderImageFile.baseFile?.size?.formatBytes()}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(folderImageFile)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, folderImageFile)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun removeItem(folderImageFile: FolderImageFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].baseFile?.data == folderImageFile.baseFile?.data) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<FolderImageFile>() {
            override fun areItemsTheSame(oldItem: FolderImageFile, newItem: FolderImageFile): Boolean =
                oldItem.baseFile?.data == newItem.baseFile?.data

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: FolderImageFile, newItem: FolderImageFile): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, FolderImageFile) -> Unit)? = null

    fun setOnMoreListener(listener: (View, FolderImageFile) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((FolderImageFile) -> Unit)? = null

    fun setOnOpenListener(listener: (FolderImageFile) -> Unit) {
        onOpenListener = listener
    }
}