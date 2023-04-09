package com.ntduc.baseproject.ui.component.main.fragment.home.image

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.KEY_BASE_AUDIO
import com.ntduc.baseproject.constant.KEY_BASE_IMAGE
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.FragmentImageDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.loadImg
import java.util.*


class ImageDetailFragment : BaseFragment<FragmentImageDetailBinding>(R.layout.fragment_image_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        if (arguments == null) {
            findNavController().popBackStack()
            return
        }

        val baseImage: BaseImage? = requireArguments().getParcelable(KEY_BASE_IMAGE)

        if (baseImage == null) {
            findNavController().popBackStack()
            return
        }

        requireContext().loadImg(imgUrl = baseImage.data, placeHolder = R.color.black, error = FileTypeExtension.getIconDocument(baseImage.data!!), view = binding.image)
        binding.name.text = baseImage.displayName!!.substringBeforeLast(".")

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseImage.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = baseImage.size?.formatBytes()

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseImage.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)

        binding.resolution.title.text = getString(R.string.resolution)
        binding.resolution.ic.setImageResource(R.drawable.ic_resolution)
        binding.resolution.description.text = "${baseImage.height} x ${baseImage.width}"
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }
    }
}