package com.ntduc.baseproject.ui.component.main.dialog

import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.DialogUnlockBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment

class UnlockDialog : BaseDialogFragment<DialogUnlockBinding>(R.layout.dialog_unlock) {

    override fun addEvent() {
        super.addEvent()

        binding.restore.setOnClickListener {
            onRestoreListener?.let {
                it()
            }
        }

        binding.delete.setOnClickListener {
            onDeleteListener?.let {
                it()
            }
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    private var onDeleteListener: (() -> Unit)? = null

    fun setOnDeleteListener(listener: (() -> Unit)) {
        onDeleteListener = listener
    }

    private var onRestoreListener: (() -> Unit)? = null

    fun setOnRestoreListener(listener: (() -> Unit)) {
        onRestoreListener = listener
    }
}