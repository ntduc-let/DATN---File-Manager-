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
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*


class FileSafeFolderAdapter(
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope
) : BindingListAdapter<File, FileSafeFolderAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: File) {
            binding.favorite.gone()

            when (FileTypeExtension.getTypeFile(file.path)) {
                FileTypeExtension.APK -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val pi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.packageManager.getPackageArchiveInfo(file.path, PackageManager.PackageInfoFlags.of(0))
                        } else {
                            context.packageManager.getPackageArchiveInfo(file.path, 0)
                        }
                        val icon = if (pi != null) {
                            pi.applicationInfo.sourceDir = file.path
                            pi.applicationInfo.publicSourceDir = file.path

                            pi.applicationInfo.loadIcon(context.packageManager)
                        } else {
                            null
                        }

                        withContext(Dispatchers.Main){
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
                            mData.setDataSource(file.path)
                            val art = mData.embeddedPicture
                            BitmapFactory.decodeByteArray(art, 0, art!!.size)
                        } catch (e: Exception) {
                            null
                        }

                        withContext(Dispatchers.Main) {
                            context.loadImg(
                                imgUrl = image,
                                view = binding.ic,
                                error = FileTypeExtension.getIconFile(file.path),
                                placeHolder = R.color.black
                            )
                        }
                    }
                }
                FileTypeExtension.PDF, FileTypeExtension.TXT, FileTypeExtension.DOC, FileTypeExtension.XLS, FileTypeExtension.PPT -> {
                    context.loadImg(
                        imgUrl = file.path,
                        view = binding.ic,
                        error = FileTypeExtension.getIconDocument(file.path),
                        placeHolder = R.color.black
                    )
                }
                else -> {
                    context.loadImg(
                        imgUrl = file.path,
                        view = binding.ic,
                        error = FileTypeExtension.getIconFile(file.path),
                        placeHolder = R.color.black
                    )
                }
            }
            binding.title.text = file.name

            binding.description.text = "${file.length().formatBytes()} ∙ ${getDateTimeFromMillis(millis = file.lastModified(), dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(file)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, file)
                }
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean =
                oldItem.path == newItem.path

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, File) -> Unit)? = null

    fun setOnMoreListener(listener: (View, File) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((File) -> Unit)? = null

    fun setOnOpenListener(listener: (File) -> Unit) {
        onOpenListener = listener
    }
}