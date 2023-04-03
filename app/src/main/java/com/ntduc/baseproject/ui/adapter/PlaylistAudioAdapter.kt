package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class PlaylistAudioAdapter(
    val context: Context
) : BindingListAdapter<PlaylistAudioFile, PlaylistAudioAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderAudioFile: PlaylistAudioFile) {

            binding.favorite.gone()
            binding.ic.setImageResource(R.drawable.ic_playlist_24dp)
            binding.title.text = folderAudioFile.name
            binding.description.text = "${folderAudioFile.listFile.size} item"

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

    fun removeItem(playlistAudioFile: PlaylistAudioFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].id == playlistAudioFile.id) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<PlaylistAudioFile>() {
            override fun areItemsTheSame(oldItem: PlaylistAudioFile, newItem: PlaylistAudioFile): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: PlaylistAudioFile, newItem: PlaylistAudioFile): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, PlaylistAudioFile) -> Unit)? = null

    fun setOnMoreListener(listener: (View, PlaylistAudioFile) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((PlaylistAudioFile) -> Unit)? = null

    fun setOnOpenListener(listener: (PlaylistAudioFile) -> Unit) {
        onOpenListener = listener
    }
}