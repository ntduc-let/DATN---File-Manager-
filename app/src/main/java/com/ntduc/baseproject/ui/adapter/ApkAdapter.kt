package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class ApkAdapter(
    val context: Context
) : BindingListAdapter<BaseApk, ApkAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseApk: BaseApk) {

            if (baseApk.icon != null) {
                binding.ic.setImageDrawable(baseApk.icon)
            } else {
                binding.ic.setImageResource(R.drawable.ic_launcher_foreground)
            }

            binding.title.text = baseApk.title
            binding.description.text = "${baseApk.size?.formatBytes()} ∙ ${getDateTimeFromMillis(millis = baseApk.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseApk)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(baseApk)
                }
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<BaseApk>() {
            override fun areItemsTheSame(oldItem: BaseApk, newItem: BaseApk): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseApk, newItem: BaseApk): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((BaseApk) -> Unit)? = null

    fun setOnMoreListener(listener: (BaseApk) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseApk) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseApk) -> Unit) {
        onOpenListener = listener
    }
}