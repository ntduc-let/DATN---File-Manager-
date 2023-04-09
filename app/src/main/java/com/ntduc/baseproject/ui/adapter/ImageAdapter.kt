package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_AUDIO
import com.ntduc.baseproject.constant.FAVORITE_IMAGE
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.databinding.ItemHeaderBinding
import com.ntduc.baseproject.databinding.ItemImageBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeaders
import com.orhanobut.hawk.Hawk
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class ImageAdapter(
    val context: Context
) : BindingListAdapter<BaseImage, RecyclerView.ViewHolder>(diffUtil), StickyHeaders, StickyHeaders.ViewSetup {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == HEADER_ITEM) parent.binding<ItemHeaderBinding>(R.layout.item_header).let(::ItemHeaderViewHolder)
        else parent.binding<ItemImageBinding>(R.layout.item_image).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) holder.bind(getItem(position))
        else if (holder is ItemHeaderViewHolder) holder.bind(getItem(position))
    }

    inner class ItemViewHolder constructor(
        private val binding: ItemImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseImage: BaseImage) {

            binding.favorite.gone()
            run breaking@{
                Hawk.get(FAVORITE_IMAGE, arrayListOf<String>()).forEach {
                    if (it == baseImage.data) {
                        binding.favorite.visible()
                        return@breaking
                    }
                }
            }

            context.loadImg(imgUrl = baseImage.data, placeHolder = R.color.black, error = FileTypeExtension.getIconFile(baseImage.data!!), view = binding.ic)

            binding.description.text = "${baseImage.size?.formatBytes()}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseImage)
                }
            }

            binding.root.setOnLongClickListener {
                onMoreListener?.let {
                    it(binding.root, baseImage)
                }
                return@setOnLongClickListener true
            }

            binding.executePendingBindings()
        }
    }

    inner class ItemHeaderViewHolder constructor(
        private val binding: ItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseImage: BaseImage) {
            binding.header.text = baseImage.displayName
            binding.executePendingBindings()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position >= currentList.size || currentList[position].data == null) HEADER_ITEM else super.getItemViewType(position)
    }


    override fun isStickyHeader(position: Int): Boolean {
        return getItemViewType(position) == HEADER_ITEM
    }

    override fun setupStickyHeaderView(stickyHeader: View) {
        ViewCompat.setElevation(stickyHeader, 10F)
    }

    override fun teardownStickyHeaderView(stickyHeader: View) {
        ViewCompat.setElevation(stickyHeader, 0F)
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp: ViewGroup.LayoutParams = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            if (isStickyHeader(holder.layoutPosition)) {
                val p: StaggeredGridLayoutManager.LayoutParams = lp
                p.isFullSpan = true
            }
        }
    }

    fun updateItem(baseImage: BaseImage) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseImage.data) {
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BaseImage>) {
        submitList(newList)
        notifyDataSetChanged()
    }

    companion object {
        private const val HEADER_ITEM = 123

        private val diffUtil = object : DiffUtil.ItemCallback<BaseImage>() {
            override fun areItemsTheSame(oldItem: BaseImage, newItem: BaseImage): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseImage, newItem: BaseImage): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, BaseImage) -> Unit)? = null

    fun setOnMoreListener(listener: (View, BaseImage) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseImage) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseImage) -> Unit) {
        onOpenListener = listener
    }
}