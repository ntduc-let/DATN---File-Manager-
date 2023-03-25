package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*

class AppAdapter(
    val context: Context
) : BindingListAdapter<BaseApp, AppAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseApp: BaseApp) {

            binding.ic.setImageDrawable(baseApp.icon)
            binding.title.text = baseApp.name
            binding.description.text = "${baseApp.size?.formatBytes()} ∙ ${getDateTimeFromMillis(millis = baseApp.firstInstallTime ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseApp)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(baseApp)
                }
            }

            binding.executePendingBindings()
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<BaseApp>() {
            override fun areItemsTheSame(oldItem: BaseApp, newItem: BaseApp): Boolean =
                oldItem.packageName == newItem.packageName

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseApp, newItem: BaseApp): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((BaseApp) -> Unit)? = null

    fun setOnMoreListener(listener: (BaseApp) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseApp) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseApp) -> Unit) {
        onOpenListener = listener
    }
}