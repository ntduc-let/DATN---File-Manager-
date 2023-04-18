package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.root.FolderFile
import com.ntduc.baseproject.databinding.ItemFolderFileBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.view.invisible
import com.ntduc.baseproject.utils.view.visible
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class FolderFileAdapter(
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope,
    var type: Int = NORMAL
) : BindingListAdapter<FolderFile, FolderFileAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemFolderFileBinding>(R.layout.item_folder_file).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    inner class ItemViewHolder constructor(
        private val binding: ItemFolderFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(folderFile: FolderFile, position: Int) {
            when (type) {
                NORMAL -> {
                    binding.checked.invisible()
                    binding.more.visible()
                }
                SELECT -> {
                    binding.checked.isActivated = folderFile.isSelected

                    binding.checked.visible()
                    binding.more.invisible()
                }
                MOVE -> {
                    binding.checked.invisible()
                    binding.more.invisible()
                }
            }

            val file = File(folderFile.data!!)
            if (file.isDirectory) {
                binding.ic.setImageResource(R.drawable.ic_folder_24dp)
            } else {
                when (FileTypeExtension.getTypeFile(folderFile.data!!)) {
                    FileTypeExtension.AUDIO -> {
                        lifecycleScope.launch {
                            val image = try {
                                val mData = MediaMetadataRetriever()
                                mData.setDataSource(folderFile.data)
                                val art = mData.embeddedPicture
                                BitmapFactory.decodeByteArray(art, 0, art!!.size)
                            } catch (e: Exception) {
                                null
                            }

                            withContext(Dispatchers.Main) {
                                context.loadImg(
                                    imgUrl = image,
                                    view = binding.ic,
                                    error = FileTypeExtension.getIconFile(folderFile.data!!),
                                    placeHolder = R.color.black
                                )
                            }
                        }
                    }
                    else -> {
                        context.loadImg(
                            imgUrl = folderFile.data!!,
                            view = binding.ic,
                            error = FileTypeExtension.getIconFile(folderFile.data!!),
                            placeHolder = R.color.black
                        )
                    }
                }
            }
            binding.title.text = folderFile.displayName
            binding.description.text = folderFile.size!!.formatBytes() + "    " + getDateTimeFromMillis(folderFile.dateModified!!, "MMM dd, yyyy", Locale.ENGLISH)
            binding.checked.isActivated = folderFile.isSelected

            binding.root.setOnClickListener {
                if (type == NORMAL || type == MOVE) {
                    if (File(folderFile.data!!).isDirectory) {
                        onClickListener?.let {
                            it(folderFile)
                        }
                    } else {
                        onOpenListener?.let {
                            it(folderFile)
                        }
                    }
                } else if (type == SELECT) {
                    folderFile.isSelected = !folderFile.isSelected

                    binding.checked.isActivated = folderFile.isSelected
                    onSelectListener?.let {
                        it(folderFile)
                    }
                }
            }

            binding.root.setOnLongClickListener {
                folderFile.isSelected = !folderFile.isSelected

                binding.checked.isActivated = folderFile.isSelected
                onSelectListener?.let {
                    it(folderFile)
                }
                return@setOnLongClickListener true
            }

            binding.more.setOnClickListener {
                onMoreListener?.let {
                    it(binding.root, folderFile)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun changeMode(type: Int) {
        this.type = type
        if (type == NORMAL || type == MOVE) {
            currentList.forEach {
                it.isSelected = false
            }
        }
        notifyDataSetChanged()
    }


    fun reloadItem(position: Int) {
        notifyItemChanged(position)
    }

    fun getPosition(folderFile: FolderFile): Int {
        if (currentList.isEmpty()) {
            return -1
        }
        for (i in currentList.indices) {
            val item = currentList[i]
            if (item == folderFile) {
                return i
            }
        }
        return -1
    }

    companion object {
        const val NORMAL = 0
        const val SELECT = 1
        const val MOVE = 2

        private val diffUtil = object : DiffUtil.ItemCallback<FolderFile>() {
            override fun areItemsTheSame(oldItem: FolderFile, newItem: FolderFile): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: FolderFile, newItem: FolderFile): Boolean =
                oldItem == newItem
        }
    }

    private var onClickListener: ((FolderFile) -> Unit)? = null

    fun setOnClickListener(listener: (FolderFile) -> Unit) {
        onClickListener = listener
    }

    private var onOpenListener: ((FolderFile) -> Unit)? = null

    fun setOnOpenListener(listener: (FolderFile) -> Unit) {
        onOpenListener = listener
    }

    private var onSelectListener: ((FolderFile) -> Unit)? = null

    fun setOnSelectListener(listener: (FolderFile) -> Unit) {
        onSelectListener = listener
    }

    private var onMoreListener: ((View, FolderFile) -> Unit)? = null

    fun setOnMoreItemListener(listener: (View, FolderFile) -> Unit) {
        onMoreListener = listener
    }
}