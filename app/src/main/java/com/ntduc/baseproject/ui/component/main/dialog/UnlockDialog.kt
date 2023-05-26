package com.ntduc.baseproject.ui.component.main.dialog

import android.os.Bundle
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.DialogUnlockBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible

class UnlockDialog : BaseDialogFragment<DialogUnlockBinding>(R.layout.dialog_unlock) {

    companion object {
        private const val IS_RESTORE = "IS_RESTORE"
        fun newInstance(isRestore: Boolean): UnlockDialog {
            val args = Bundle()
            args.putBoolean(IS_RESTORE, isRestore)

            val fragment = UnlockDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var isRestore: Boolean = false

    override fun initView() {
        super.initView()

        isRestore = requireArguments().getBoolean(IS_RESTORE)
        if (isRestore) {
            binding.restore.visible()
        } else {
            binding.restore.gone()
        }
    }

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