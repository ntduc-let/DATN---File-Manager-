package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_AUDIO
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class AudioAdapter(
    val context: Context,
    val lifecycleScope: LifecycleCoroutineScope
) : BindingListAdapter<BaseAudio, AudioAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseAudio: BaseAudio) {

            binding.favorite.gone()
            run breaking@{
                Hawk.get(FAVORITE_AUDIO, arrayListOf<String>()).forEach {
                    if (it == baseAudio.data) {
                        binding.favorite.visible()
                        return@breaking
                    }
                }
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val image = try {
                    val mData = MediaMetadataRetriever()
                    mData.setDataSource(baseAudio.data)
                    val art = mData.embeddedPicture
                    BitmapFactory.decodeByteArray(art, 0, art!!.size)
                } catch (e: Exception) {
                    null
                }

                withContext(Dispatchers.Main){
                    context.loadImg(imgUrl = image, placeHolder = R.color.black, error = FileTypeExtension.getIconFile(baseAudio.data!!), view = binding.ic)
                }
            }

            binding.title.text = baseAudio.displayName
            binding.description.text = "${baseAudio.size?.formatBytes()} ∙ ${getDateTimeFromMillis(millis = baseAudio.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseAudio)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, baseAudio)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun updateItem(baseFile: BaseFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseFile.data) {
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemChanged(position)
    }

    fun removeItem(baseFile: BaseFile) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseFile.data) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<BaseAudio>() {
            override fun areItemsTheSame(oldItem: BaseAudio, newItem: BaseAudio): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseAudio, newItem: BaseAudio): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, BaseAudio) -> Unit)? = null

    fun setOnMoreListener(listener: (View, BaseAudio) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseAudio) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseAudio) -> Unit) {
        onOpenListener = listener
    }
}