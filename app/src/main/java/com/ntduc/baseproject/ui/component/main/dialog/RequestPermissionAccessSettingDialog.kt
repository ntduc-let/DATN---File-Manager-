package com.ntduc.baseproject.ui.component.main.dialog

import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.DialogRequestPermissionBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment

class RequestPermissionAccessSettingDialog : BaseDialogFragment<DialogRequestPermissionBinding>(R.layout.dialog_request_permission) {

    private var onAllowListener: (() -> Unit)? = null

    fun setOnAllowListener(listener: (() -> Unit)) {
        onAllowListener = listener
    }

    override fun initView() {
        super.initView()

        binding.content.title.text = getString(R.string.content_request_permission_access_setting)
    }

    override fun addEvent() {
        super.addEvent()

        binding.allow.setOnClickListener {
            onAllowListener?.let { it() }
            dismiss()
        }
    }
}