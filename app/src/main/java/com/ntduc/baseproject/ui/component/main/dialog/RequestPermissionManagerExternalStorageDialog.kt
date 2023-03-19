package com.ntduc.baseproject.ui.component.main.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.FragmentManager
import com.ntduc.baseproject.databinding.DialogRequestPermissionBinding
import com.ntduc.baseproject.utils.context.displayWidth
import com.permissionx.guolindev.dialog.RationaleDialogFragment
import kotlin.math.roundToInt

class RequestPermissionManagerExternalStorageDialog : RationaleDialogFragment() {
    private lateinit var binding: DialogRequestPermissionBinding

    private var deniedList: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRequestPermissionBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mDialog = dialog
        if (mDialog != null) {
            mDialog.setCanceledOnTouchOutside(false)
            if (mDialog.window != null) {
                mDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mDialog.window!!.setLayout(
                    (requireActivity().displayWidth * 0.9).roundToInt(),
                    WindowManager.LayoutParams.WRAP_CONTENT
                )

                val layoutParams = mDialog.window!!.attributes
                layoutParams.gravity = Gravity.CENTER
                mDialog.window!!.attributes = layoutParams
            }
        }
    }

    fun setDeniedList(deniedList: MutableList<String>) {
        this.deniedList = deniedList
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        if (isAdded) {
            return
        }
        try {
            manager.beginTransaction().remove(this).commitNow()
            super.showNow(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dismiss() {
        if (isAdded) {
            try {
                super.dismissAllowingStateLoss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getPositiveButton(): View = binding.allow

    override fun getNegativeButton(): View? = null

    override fun getPermissionsToRequest(): MutableList<String> = deniedList

}