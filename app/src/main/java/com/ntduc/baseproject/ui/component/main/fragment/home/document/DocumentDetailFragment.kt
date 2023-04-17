package com.ntduc.baseproject.ui.component.main.fragment.home.document

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.KEY_BASE_DOCUMENT
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentApkDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.loadImg
import java.util.*


class DocumentDetailFragment : BaseFragment<FragmentApkDetailBinding>(R.layout.fragment_apk_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        val baseFile: BaseFile? = requireArguments().getParcelable(KEY_BASE_DOCUMENT)

        if (baseFile == null) {
            findNavController().popBackStack()
            return
        }

        requireContext().loadImg(imgUrl = baseFile.data, placeHolder = R.color.black, error = FileTypeExtension.getIconDocument(baseFile.data!!), view = binding.image)
        binding.name.text = baseFile.displayName!!.substringBeforeLast(".")

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseFile.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = baseFile.size?.formatBytes()

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseFile.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener{
            findNavController().popBackStack()
        }
    }
}