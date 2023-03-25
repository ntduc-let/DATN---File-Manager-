package com.ntduc.baseproject.ui.component.main.fragment.home.app

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.KEY_BASE_APK
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.databinding.FragmentApkDetailBinding
import com.ntduc.baseproject.databinding.FragmentAppBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentAppAdapter
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import java.util.*


class ApkDetailFragment : BaseFragment<FragmentApkDetailBinding>(R.layout.fragment_apk_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        if (arguments == null){
            findNavController().popBackStack()
            return
        }

        val baseApk: BaseApk? = requireArguments().getParcelable(KEY_BASE_APK)

        if (baseApk == null) {
            findNavController().popBackStack()
            return
        }

        binding.image.setImageDrawable(baseApk.icon)
        binding.name.text = baseApk.title

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseApk.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = baseApk.size?.formatBytes()

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseApk.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener{
            findNavController().popBackStack()
        }
    }
}