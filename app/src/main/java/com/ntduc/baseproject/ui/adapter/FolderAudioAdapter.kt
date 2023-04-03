package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.folder.FolderAudioFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class FolderAudioAdapter(
    val context: Context
) : BindingListAdapter<FolderAudioFile, FolderAudioAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderAudioFile: FolderAudioFile) {

            binding.favorite.gone()
            binding.ic.setImageResource(R.drawable.ic_folder_24dp)
            binding.title.text = folderAudioFile.baseFile?.displayName
            binding.description.text = "${folderAudioFile.listFile.size} item ∙ ${folderAudioFile.baseFile?.size?.formatBytes()}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(folderAudioFile)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, folderAudioFile)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun removeItem(folderAudioFile: FolderAudioFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].baseFile?.data == folderAudioFile.baseFile?.data) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<FolderAudioFile>() {
            override fun areItemsTheSame(oldItem: FolderAudioFile, newItem: FolderAudioFile): Boolean =
                oldItem.baseFile?.data == newItem.baseFile?.data

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: FolderAudioFile, newItem: FolderAudioFile): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, FolderAudioFile) -> Unit)? = null

    fun setOnMoreListener(listener: (View, FolderAudioFile) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((FolderAudioFile) -> Unit)? = null

    fun setOnOpenListener(listener: (FolderAudioFile) -> Unit) {
        onOpenListener = listener
    }
}