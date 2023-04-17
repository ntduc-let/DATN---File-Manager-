package com.ntduc.baseproject.ui.component.main.fragment.home.video

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.KEY_BASE_VIDEO
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.databinding.FragmentVideoDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatAsTime
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.loadImg
import java.util.*


class VideoDetailFragment : BaseFragment<FragmentVideoDetailBinding>(R.layout.fragment_video_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        val baseVideo: BaseVideo? = requireArguments().getParcelable(KEY_BASE_VIDEO)

        if (baseVideo == null) {
            findNavController().popBackStack()
            return
        }

        requireContext().loadImg(imgUrl = baseVideo.data, placeHolder = R.color.black, error = FileTypeExtension.getIconDocument(baseVideo.data!!), view = binding.image)
        binding.name.text = baseVideo.displayName!!.substringBeforeLast(".")

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseVideo.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = baseVideo.size?.formatBytes()

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseVideo.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)

        binding.time.title.text = getString(R.string.time)
        binding.time.ic.setImageResource(R.drawable.ic_time)
        binding.time.description.text = "${baseVideo.duration?.formatAsTime()}"

        binding.resolution.title.text = getString(R.string.resolution)
        binding.resolution.ic.setImageResource(R.drawable.ic_resolution)
        binding.resolution.description.text = "${baseVideo.resolution}"
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }
    }
}