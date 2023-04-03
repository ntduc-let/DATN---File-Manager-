package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.KEY_BASE_AUDIO
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.databinding.FragmentAudioDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatAsTime
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.loadImg
import java.util.*


class AudioDetailFragment : BaseFragment<FragmentAudioDetailBinding>(R.layout.fragment_audio_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        if (arguments == null){
            findNavController().popBackStack()
            return
        }

        val baseAudio: BaseAudio? = requireArguments().getParcelable(KEY_BASE_AUDIO)

        if (baseAudio == null) {
            findNavController().popBackStack()
            return
        }

        val image = try {
            val mData = MediaMetadataRetriever()
            mData.setDataSource(baseAudio.data)
            val art = mData.embeddedPicture
            BitmapFactory.decodeByteArray(art, 0, art!!.size)
        } catch (e: Exception) {
            null
        }

        requireContext().loadImg(imgUrl = image, placeHolder = R.color.black, error = FileTypeExtension.getIconDocument(baseAudio.data!!), view = binding.image)
        binding.name.text = baseAudio.displayName!!.substringBeforeLast(".")

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseAudio.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = baseAudio.size?.formatBytes()

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseAudio.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)

        binding.time.title.text = getString(R.string.time)
        binding.time.ic.setImageResource(R.drawable.ic_time)
        binding.time.description.text = baseAudio.duration?.formatAsTime()
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener{
            findNavController().popBackStack()
        }
    }
}