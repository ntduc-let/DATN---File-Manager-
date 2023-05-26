package com.ntduc.baseproject.ui.component.main.fragment.files

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.KEY_BASE_FOLDER
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentFolderDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.size
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import java.io.File
import java.util.Locale


class FolderDetailFragment : BaseFragment<FragmentFolderDetailBinding>(R.layout.fragment_folder_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        val baseFile: BaseFile? = requireArguments().getParcelable(KEY_BASE_FOLDER)

        if (baseFile == null) {
            findNavController().popBackStack()
            return
        }

        binding.name.text = baseFile.title

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = baseFile.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)

        val file = File(baseFile.data!!)
        binding.size.description.text = "${file.listFiles()?.size ?: 0} item ∙ ${file.size()?.formatBytes()}"

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = baseFile.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }
    }
}