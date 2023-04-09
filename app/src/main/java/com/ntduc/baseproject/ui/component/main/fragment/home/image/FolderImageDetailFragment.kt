package com.ntduc.baseproject.ui.component.main.fragment.home.image

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.KEY_BASE_FOLDER_IMAGE
import com.ntduc.baseproject.data.dto.folder.FolderImageFile
import com.ntduc.baseproject.databinding.FragmentFolderDetailBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import java.util.*


class FolderImageDetailFragment : BaseFragment<FragmentFolderDetailBinding>(R.layout.fragment_folder_detail) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        if (arguments == null){
            findNavController().popBackStack()
            return
        }

        val folderImageFile: FolderImageFile? = requireArguments().getParcelable(KEY_BASE_FOLDER_IMAGE)

        if (folderImageFile == null) {
            findNavController().popBackStack()
            return
        }

        binding.name.text = folderImageFile.baseFile!!.displayName

        binding.path.title.text = getString(R.string.path)
        binding.path.ic.setImageResource(R.drawable.ic_path)
        binding.path.description.text = folderImageFile.baseFile!!.data

        binding.size.title.text = getString(R.string.size)
        binding.size.ic.setImageResource(R.drawable.ic_size)
        binding.size.description.text = "${folderImageFile.listFile.size} item ∙ ${folderImageFile.baseFile?.size?.formatBytes()}"

        binding.date.title.text = getString(R.string.date)
        binding.date.ic.setImageResource(R.drawable.ic_date)
        binding.date.description.text = getDateTimeFromMillis(millis = folderImageFile.baseFile!!.dateModified ?: 0, dateFormat = "MMM dd yyyy, hh:mm", locale = Locale.ENGLISH)
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener{
            findNavController().popBackStack()
        }
    }
}