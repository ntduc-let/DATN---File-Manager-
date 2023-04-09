package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_VIDEO
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.databinding.ItemHeaderBinding
import com.ntduc.baseproject.databinding.ItemVideoBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeaders
import com.orhanobut.hawk.Hawk
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class VideoAdapter(
    val context: Context
) : BindingListAdapter<BaseVideo, RecyclerView.ViewHolder>(diffUtil), StickyHeaders, StickyHeaders.ViewSetup {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == HEADER_ITEM) parent.binding<ItemHeaderBinding>(R.layout.item_header).let(::ItemHeaderViewHolder)
        else parent.binding<ItemVideoBinding>(R.layout.item_video).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) holder.bind(getItem(position))
        else if (holder is ItemHeaderViewHolder) holder.bind(getItem(position))
    }

    inner class ItemViewHolder constructor(
        private val binding: ItemVideoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseVideo: BaseVideo) {

            binding.favorite.gone()
            run breaking@{
                Hawk.get(FAVORITE_VIDEO, arrayListOf<String>()).forEach {
                    if (it == baseVideo.data) {
                        binding.favorite.visible()
                        return@breaking
                    }
                }
            }

            context.loadImg(imgUrl = baseVideo.data, placeHolder = R.color.black, error = FileTypeExtension.getIconFile(baseVideo.data!!), view = binding.ic)

            binding.description.text = "${baseVideo.duration?.formatAsTime()}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseVideo)
                }
            }

            binding.root.setOnLongClickListener {
                onMoreListener?.let {
                    it(binding.root, baseVideo)
                }
                return@setOnLongClickListener true
            }

            binding.executePendingBindings()
        }
    }

    inner class ItemHeaderViewHolder constructor(
        private val binding: ItemHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseVideo: BaseVideo) {
            binding.header.text = baseVideo.displayName
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

    fun updateItem(baseVideo: BaseVideo) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseVideo.data) {
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemChanged(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newList: List<BaseVideo>) {
        submitList(newList)
        notifyDataSetChanged()
    }

    companion object {
        private const val HEADER_ITEM = 123

        private val diffUtil = object : DiffUtil.ItemCallback<BaseVideo>() {
            override fun areItemsTheSame(oldItem: BaseVideo, newItem: BaseVideo): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseVideo, newItem: BaseVideo): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, BaseVideo) -> Unit)? = null

    fun setOnMoreListener(listener: (View, BaseVideo) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseVideo) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseVideo) -> Unit) {
        onOpenListener = listener
    }
}