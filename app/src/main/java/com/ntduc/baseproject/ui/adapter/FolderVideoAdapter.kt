package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.folder.FolderVideoFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class FolderVideoAdapter(
    val context: Context
) : BindingListAdapter<FolderVideoFile, FolderVideoAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderVideoFile: FolderVideoFile) {

            binding.favorite.gone()
            binding.ic.setImageResource(R.drawable.ic_folder_24dp)
            binding.title.text = folderVideoFile.baseFile?.displayName
            binding.description.text = "${folderVideoFile.listFile.size} item ∙ ${folderVideoFile.baseFile?.size?.formatBytes()}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(folderVideoFile)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, folderVideoFile)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun removeItem(folderVideoFile: FolderVideoFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].baseFile?.data == folderVideoFile.baseFile?.data) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<FolderVideoFile>() {
            override fun areItemsTheSame(oldItem: FolderVideoFile, newItem: FolderVideoFile): Boolean =
                oldItem.baseFile?.data == newItem.baseFile?.data

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: FolderVideoFile, newItem: FolderVideoFile): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, FolderVideoFile) -> Unit)? = null

    fun setOnMoreListener(listener: (View, FolderVideoFile) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((FolderVideoFile) -> Unit)? = null

    fun setOnOpenListener(listener: (FolderVideoFile) -> Unit) {
        onOpenListener = listener
    }
}