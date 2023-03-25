package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_APK
import com.ntduc.baseproject.constant.FAVORITE_APP
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
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

            binding.favorite.gone()
            run breaking@{
                Hawk.get(FAVORITE_APK, arrayListOf<String>()).forEach {
                    if (it == baseApk.data) {
                        binding.favorite.visible()
                        return@breaking
                    }
                }
            }

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
                    it(binding.root, baseApk)
                }
            }

            binding.executePendingBindings()
        }
    }

    fun updateItem(baseApk: BaseApk) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseApk.data) {
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemChanged(position)
    }

    fun removeItem(baseApk: BaseApk) {
        var position = -1
        run breaking@{
            currentList.indices.forEach {
                if (currentList[it].data == baseApk.data) {
                    currentList.removeAt(it)
                    position = it
                    return@breaking
                }
            }
        }

        if (position != -1) notifyItemRemoved(position)
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

    private var onMoreListener: ((View, BaseApk) -> Unit)? = null

    fun setOnMoreListener(listener: (View, BaseApk) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseApk) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseApk) -> Unit) {
        onOpenListener = listener
    }
}