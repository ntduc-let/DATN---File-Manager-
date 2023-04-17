package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.ItemRecentFilesBinding
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.loadImg
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentFilesAdapter(
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope
) : BindingListAdapter<BaseFile, RecentFilesAdapter.FilesViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilesViewHolder =
        parent.binding<ItemRecentFilesBinding>(R.layout.item_recent_files).let(::FilesViewHolder)

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class FilesViewHolder constructor(
        private val binding: ItemRecentFilesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseFile: BaseFile) {
            when (FileTypeExtension.getTypeFile(baseFile.data!!)) {
                FileTypeExtension.AUDIO -> {
                    lifecycleScope.launch {
                        val image = try {
                            val mData = MediaMetadataRetriever()
                            mData.setDataSource(baseFile.data)
                            val art = mData.embeddedPicture
                            BitmapFactory.decodeByteArray(art, 0, art!!.size)
                        } catch (e: Exception) {
                            null
                        }

                        withContext(Dispatchers.Main) {
                            context.loadImg(
                                imgUrl = image,
                                view = binding.ic,
                                error = FileTypeExtension.getIconFile(baseFile.data!!),
                                placeHolder = R.color.black
                            )
                        }
                    }
                }
                else -> {
                    context.loadImg(
                        imgUrl = baseFile.data!!,
                        view = binding.ic,
                        error = FileTypeExtension.getIconFile(baseFile.data!!),
                        placeHolder = R.color.black
                    )
                }
            }
            binding.title.text = baseFile.displayName
            binding.size.text = baseFile.size?.formatBytes()

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseFile)
                }
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<BaseFile>() {
            override fun areItemsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem == newItem
        }
    }

    private var onOpenListener: ((BaseFile) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseFile) -> Unit) {
        onOpenListener = listener
    }
}