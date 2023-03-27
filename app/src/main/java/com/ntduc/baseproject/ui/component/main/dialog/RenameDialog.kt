package com.ntduc.baseproject.ui.component.main.dialog

import android.os.Bundle
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.DialogRenameBinding
import com.ntduc.baseproject.databinding.DialogRequestPermissionBinding
import com.ntduc.baseproject.ui.base.BaseDialogFragment
import com.ntduc.baseproject.utils.file.renameTo
import com.ntduc.baseproject.utils.toast.shortToast
import java.io.File

class RenameDialog : BaseDialogFragment<DialogRenameBinding>(R.layout.dialog_rename) {

    companion object {
        private const val DATA = "DATA"

        fun newInstance(baseFile: BaseFile): RenameDialog {
            val args = Bundle()
            args.putParcelable(DATA, baseFile)

            val fragment = RenameDialog()
            fragment.arguments = args
            return fragment
        }
    }

    private var baseFile: BaseFile? = null

    override fun initView() {
        super.initView()

        baseFile = requireArguments().getParcelable(DATA)
        binding.name.setText(baseFile!!.displayName!!.substringBeforeLast("."))
    }

    override fun addEvent() {
        super.addEvent()

        binding.ok.setOnClickListener {
            if (binding.name.text.isEmpty()) return@setOnClickListener

            val newFile = File(baseFile!!.data!!).renameTo(requireContext(), binding.name.text.toString().trim())
            if (newFile == null) {
                shortToast("Please try again!")
                return@setOnClickListener
            }

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