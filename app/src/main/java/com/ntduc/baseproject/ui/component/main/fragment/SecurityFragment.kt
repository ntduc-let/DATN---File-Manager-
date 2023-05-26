package com.ntduc.baseproject.ui.component.main.fragment

import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentSecurityBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.UnlockDialog
import com.ntduc.baseproject.ui.component.main.fragment.security.textwatcher.GenericKeyEvent
import com.ntduc.baseproject.ui.component.main.fragment.security.textwatcher.GenericTextWatcher
import com.ntduc.baseproject.utils.activity.hideKeyboard
import com.ntduc.baseproject.utils.activity.showKeyboard
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.readToString
import com.ntduc.baseproject.utils.file.writeToFile
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SecurityFragment : BaseFragment<FragmentSecurityBinding>(R.layout.fragment_security) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        showKeyboard(binding.otpET1)
        checkNullEdt()
        if (isFolderSafeExists()) {
            binding.login.visible()
            binding.delete.visible()
            binding.create.gone()
        } else {
            binding.login.gone()
            binding.delete.gone()
            binding.create.visible()
        }
    }

    override fun addEvent() {
        super.addEvent()

        binding.login.setOnClickListener {
            val pin = binding.otpET1.text.toString() + binding.otpET2.text.toString() + binding.otpET3.text.toString() + binding.otpET4.text.toString() + binding.otpET5.text.toString() + binding.otpET6.text.toString()

            if (isPasswordVerificationSuccessful(pin)) {
                clearTextPass()

                shortToast("Đăng nhập thành công")

                navigateToDes(R.id.homeSafeFragment)
            } else {
                shortToast("Mật khẩu không chính xác. Hãy thử lại!")
            }
        }

        binding.create.setOnClickListener {
            val pin = binding.otpET1.text.toString() + binding.otpET2.text.toString() + binding.otpET3.text.toString() + binding.otpET4.text.toString() + binding.otpET5.text.toString() + binding.otpET6.text.toString()

            if (pin.length == 6) {
                clearTextPass()
                savePass(pin)

                shortToast("Tạo thư mục bảo mật thành công")

                navigateToDes(R.id.homeSafeFragment)
            } else {
                shortToast("Mật khẩu phải đủ 6 chữ số. Hãy thử lại!")
            }
        }

        binding.delete.setOnClickListener {
            val dialog = UnlockDialog.newInstance(false)
            dialog.setOnDeleteListener {
                dialog.dismiss()
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")

                lifecycleScope.launch(Dispatchers.IO) {
                    File(Environment.getExternalStorageDirectory().path + "/.${requireContext().getString(R.string.app_name)}/.SafeFolder/").delete(requireContext())

                    withContext(Dispatchers.Main) {
                        dialogLoading.dismiss()

                        binding.login.gone()
                        binding.delete.gone()
                        binding.create.visible()
                    }
                }
            }

            dialog.show(childFragmentManager, "UnlockDialog")
        }

        binding.otpET1.addTextChangedListener(GenericTextWatcher(binding.otpET1, binding.otpET2))
        binding.otpET2.addTextChangedListener(GenericTextWatcher(binding.otpET2, binding.otpET3))
        binding.otpET3.addTextChangedListener(GenericTextWatcher(binding.otpET3, binding.otpET4))
        binding.otpET4.addTextChangedListener(GenericTextWatcher(binding.otpET4, binding.otpET5))
        binding.otpET5.addTextChangedListener(GenericTextWatcher(binding.otpET5, binding.otpET6))
        binding.otpET6.addTextChangedListener(GenericTextWatcher(binding.otpET6, null))
        binding.otpET2.setOnKeyListener(GenericKeyEvent(binding.otpET2, binding.otpET1))
        binding.otpET3.setOnKeyListener(GenericKeyEvent(binding.otpET3, binding.otpET2))
        binding.otpET4.setOnKeyListener(GenericKeyEvent(binding.otpET4, binding.otpET3))
        binding.otpET5.setOnKeyListener(GenericKeyEvent(binding.otpET5, binding.otpET4))
        binding.otpET6.setOnKeyListener(GenericKeyEvent(binding.otpET6, binding.otpET5))

        val textWatcher = object : TextWatcher {
            override fun afterTextChanged(arg0: Editable) {
                checkNullEdt()
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
        }
        binding.otpET1.addTextChangedListener(textWatcher)
        binding.otpET2.addTextChangedListener(textWatcher)
        binding.otpET3.addTextChangedListener(textWatcher)
        binding.otpET4.addTextChangedListener(textWatcher)
        binding.otpET5.addTextChangedListener(textWatcher)
        binding.otpET6.addTextChangedListener(textWatcher)
    }

    private fun savePass(pin: String) {
        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder")
        folder.mkdirs()
        val file = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/pass.txt")
        file.createNewFile()

        val text = Base64.encodeToString(pin.toByteArray(), Base64.DEFAULT)
        file.writeToFile(text.toString())
    }

    private fun clearTextPass() {
        binding.otpET1.text.clear()
        binding.otpET2.text.clear()
        binding.otpET3.text.clear()
        binding.otpET4.text.clear()
        binding.otpET5.text.clear()
        binding.otpET6.text.clear()
    }

    private fun isFolderSafeExists(): Boolean {
        val file = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/pass.txt")
        return file.exists() && file.readToString().isNotEmpty()
    }

    private fun checkNullEdt() {
        if (binding.otpET1.text.isNotEmpty() &&
            binding.otpET2.text.isNotEmpty() &&
            binding.otpET3.text.isNotEmpty() &&
            binding.otpET4.text.isNotEmpty() &&
            binding.otpET5.text.isNotEmpty() &&
            binding.otpET6.text.isNotEmpty()
        ) {
            binding.otpET1.clearFocus()
            binding.otpET2.clearFocus()
            binding.otpET3.clearFocus()
            binding.otpET4.clearFocus()
            binding.otpET5.clearFocus()
            binding.otpET6.clearFocus()
            hideKeyboard()
            binding.login.isEnabled = true
            binding.login.setBackgroundColor(resources.getColor(R.color.blue_main))
            binding.create.isEnabled = true
            binding.create.setBackgroundColor(resources.getColor(R.color.blue_main))
        } else {
            binding.login.isEnabled = false
            binding.login.setBackgroundColor(resources.getColor(R.color.blue_third))
            binding.create.isEnabled = false
            binding.create.setBackgroundColor(resources.getColor(R.color.blue_third))
        }
    }

    private fun isPasswordVerificationSuccessful(pin: String): Boolean {
        val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
        val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
        val pass = String(bytes, Charsets.UTF_8)
        return pin == pass
    }
}