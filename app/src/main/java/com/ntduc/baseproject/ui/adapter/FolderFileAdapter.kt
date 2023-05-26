package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseFile
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
    val context: Context, val lifecycleScope: LifecycleCoroutineScope, var type: Int = NORMAL
) : BindingListAdapter<BaseFile, FolderFileAdapter.ItemViewHolder>(diffUtil) {

    private var fileMove: BaseFile? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder = parent.binding<ItemFolderFileBinding>(R.layout.item_folder_file).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(getItem(position), position)

    inner class ItemViewHolder constructor(
        private val binding: ItemFolderFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseFile: BaseFile, position: Int) {
            when (type) {
                NORMAL -> binding.more.visible()
                MOVE -> binding.more.invisible()
            }

            val file = File(baseFile.data!!)
            if (file.isDirectory) {
                binding.ic.setImageResource(R.drawable.ic_folder_24dp)
                binding.description.text = "${file.listFiles()?.size ?: 0} items    " + baseFile.size!!.formatBytes()
            } else {
                binding.description.text = baseFile.size!!.formatBytes() + "    " + getDateTimeFromMillis(baseFile.dateModified!!, "MMM dd, yyyy", Locale.ENGLISH)
                when (FileTypeExtension.getTypeFile(baseFile.data!!)) {
                    FileTypeExtension.APK -> {
                        lifecycleScope.launch(Dispatchers.IO) {
                            val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                context.packageManager.getPackageArchiveInfo(baseFile.data!!, PackageManager.PackageInfoFlags.of(0))
                            } else {
                                context.packageManager.getPackageArchiveInfo(baseFile.data!!, 0)
                            }
                            val icon = if (pi != null) {
                                pi.applicationInfo.sourceDir = baseFile.data!!
                                pi.applicationInfo.publicSourceDir = baseFile.data!!

                                pi.applicationInfo.loadIcon(context.packageManager)
                            } else {
                                null
                            }

                            withContext(Dispatchers.Main) {
                                if (icon != null) {
                                    binding.ic.setImageDrawable(icon)
                                } else {
                                    binding.ic.setImageResource(R.drawable.ic_launcher_foreground)
                                }
                            }
                        }
                    }

                    FileTypeExtension.AUDIO -> {
                        lifecycleScope.launch(Dispatchers.IO) {
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

                    FileTypeExtension.PDF, FileTypeExtension.TXT, FileTypeExtension.DOC, FileTypeExtension.XLS, FileTypeExtension.PPT -> {
                        context.loadImg(
                            imgUrl = baseFile.data!!,
                            view = binding.ic,
                            error = FileTypeExtension.getIconDocument(baseFile.data!!),
                            placeHolder = R.color.black
                        )
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
            }
            binding.title.text = baseFile.displayName

            if (fileMove?.data == baseFile.data){
                binding.bg.setBackgroundResource(R.color.blue_third)
            }else{
                binding.bg.setBackgroundResource(android.R.color.transparent)
            }

            binding.root.setOnClickListener {
                if (File(baseFile.data!!).isDirectory) {
                    if (fileMove?.data == baseFile.data) return@setOnClickListener

                    onClickListener?.let {
                        it(baseFile)
                    }
                } else {
                    onOpenListener?.let {
                        it(baseFile)
                    }
                }
            }

            binding.more.setOnClickListener {
                if (type == NORMAL){
                    onMoreListener?.let {
                        it(binding.root, baseFile)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }

    fun changeMode(type: Int) {
        this.type = type
        notifyDataSetChanged()
    }

    fun setFileMove(baseFile: BaseFile) {
        run breaking@{
            currentList.forEachIndexed { index, file ->
                if (file.data == baseFile.data) {
                    fileMove = file
                    notifyItemChanged(index)
                    return@breaking
                }
            }
        }
    }

    fun removeFileMove() {
        fileMove = null
    }

    fun getFileMove(): BaseFile = fileMove!!

    companion object {
        const val NORMAL = 0
        const val MOVE = 1

        private val diffUtil = object : DiffUtil.ItemCallback<BaseFile>() {
            override fun areItemsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem == newItem
        }
    }

    private var onClickListener: ((BaseFile) -> Unit)? = null

    fun setOnClickListener(listener: (BaseFile) -> Unit) {
        onClickListener = listener
    }

    private var onOpenListener: ((BaseFile) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseFile) -> Unit) {
        onOpenListener = listener
    }

    private var onMoreListener: ((View, BaseFile) -> Unit)? = null

    fun setOnMoreItemListener(listener: (View, BaseFile) -> Unit) {
        onMoreListener = listener
    }
}