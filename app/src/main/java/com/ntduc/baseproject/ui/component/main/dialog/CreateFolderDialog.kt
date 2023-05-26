package com.ntduc.baseproject.ui.component.main.dialog

import android.os.Bundle
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.DialogCreateFolderBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment
import com.ntduc.baseproject.utils.toast.shortToast
import java.io.File

class CreateFolderDialog : BaseDialogFragment<DialogCreateFolderBinding>(R.layout.dialog_create_folder) {

    companion object {
        private const val DATA = "DATA"

        fun newInstance(path: String): CreateFolderDialog {
            val args = Bundle()
            args.putString(DATA, path)

            val fragment = CreateFolderDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var path: String? = null

    override fun initView() {
        super.initView()

        path = requireArguments().getString(DATA)
    }

    override fun addEvent() {
        super.addEvent()

        binding.ok.setOnClickListener {
            val name = binding.name.text.trim()
            if (name.isEmpty()) return@setOnClickListener

            val newFile = File("$path/$name")
            if (newFile.exists()) {
                shortToast("Folder already exists")
                return@setOnClickListener
            }

            newFile.mkdirs()

            onOKListener?.let { it() }
            dismiss()
        }

        binding.cancel.setOnClickListener {
            dismiss()
        }
    }

    private var onOKListener: (() -> Unit)? = null

    fun setOnOKListener(listener: (() -> Unit)) {
        onOKListener = listener
    }
}