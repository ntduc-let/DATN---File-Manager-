package com.ntduc.baseproject.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_APK
import com.ntduc.baseproject.constant.FAVORITE_DOCUMENT
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.ItemDocumentBinding
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import com.skydoves.bindables.BindingListAdapter
import com.skydoves.bindables.binding
import java.util.*


class DocumentAdapter(
    val context: Context
) : BindingListAdapter<BaseFile, DocumentAdapter.ItemViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        parent.binding<ItemDocumentBinding>(R.layout.item_document).let(::ItemViewHolder)

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class ItemViewHolder constructor(
        private val binding: ItemDocumentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(baseFile: BaseFile) {

            binding.favorite.gone()
            run breaking@{
                Hawk.get(FAVORITE_DOCUMENT, arrayListOf<String>()).forEach {
                    if (it == baseFile.data) {
                        binding.favorite.visible()
                        return@breaking
                    }
                }
            }

            binding.ic.setImageResource(FileTypeExtension.getIconDocument(baseFile.data!!))

            binding.title.text = baseFile.displayName
            binding.description.text = "${baseFile.size?.formatBytes()} ∙ ${getDateTimeFromMillis(millis = baseFile.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)}"

            binding.root.setOnClickListener {
                onOpenListener?.let {
                    it(baseFile)
                }
            }

            binding.more.setOnClickShrinkEffectListener {
                onMoreListener?.let {
                    it(binding.root, baseFile)
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
        private val diffUtil = object : DiffUtil.ItemCallback<BaseFile>() {
            override fun areItemsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem.id == newItem.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: BaseFile, newItem: BaseFile): Boolean =
                oldItem == newItem
        }
    }

    private var onMoreListener: ((View, BaseFile) -> Unit)? = null

    fun setOnMoreListener(listener: (View, BaseFile) -> Unit) {
        onMoreListener = listener
    }

    private var onOpenListener: ((BaseFile) -> Unit)? = null

    fun setOnOpenListener(listener: (BaseFile) -> Unit) {
        onOpenListener = listener
    }
}