package com.ntduc.baseproject.ui.component.main.fragment.home.image

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.folder.FolderImageFile
import com.ntduc.baseproject.databinding.FragmentListImageInFolderBinding
import com.ntduc.baseproject.databinding.MenuDocumentDetailBinding
import com.ntduc.baseproject.ui.adapter.ImageAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.ImageMoreDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeadersGridLayoutManager
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class ListImageInFolderFragment : BaseFragment<FragmentListImageInFolderBinding>(R.layout.fragment_list_image_in_folder) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var imageAdapter: ImageAdapter
    private var folderImageFile: FolderImageFile? = null

    override fun initView() {
        super.initView()

        if (arguments == null) {
            findNavController().popBackStack()
            return
        }

        folderImageFile = requireArguments().getParcelable(KEY_BASE_FOLDER_IMAGE)

        if (folderImageFile == null) {
            findNavController().popBackStack()
            return
        }

        binding.title.text = folderImageFile!!.baseFile!!.displayName

        imageAdapter = ImageAdapter(requireContext())
        binding.rcv.apply {
            adapter = imageAdapter
            setHasFixedSize(true)
            val stickyHeadersGridLayoutManager: StickyHeadersGridLayoutManager<ImageAdapter> = StickyHeadersGridLayoutManager(requireContext(), 2)
            stickyHeadersGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (imageAdapter.isStickyHeader(position)) 2 else 1
            }
            layoutManager = stickyHeadersGridLayoutManager
        }
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }

        binding.sort.setOnClickShrinkEffectListener {
            val dialog = SortBottomDialogFragment()
            dialog.show(childFragmentManager, "SortDialog")
        }

        imageAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        imageAdapter.setOnMoreListener { view, baseImage ->
            showMenu(view, baseImage)
        }
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.imageListLiveData, ::handleImageList)
    }

    private fun handleImageList(status: Resource<List<BaseImage>>) {
        when (status) {
            is Resource.Loading -> {
                if (imageAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val list = it.filter { File(it.data!!).parent == folderImageFile!!.baseFile!!.data }

                    withContext(Dispatchers.Main) {
                        if (list.isEmpty()) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }

                        withContext(Dispatchers.IO) {
                            var result = listOf<BaseImage>()

                            when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                                SORT_BY_NAME_A_Z -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }
                                    result = filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_NAME_Z_A -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                                    result = filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_DATE_NEW -> {
                                    val temp = list.sortedBy { item -> item.dateModified }.reversed()
                                    result = filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_DATE_OLD -> {
                                    val temp = list.sortedBy { item -> item.dateModified }
                                    result = filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_SIZE_LARGE -> {
                                    val temp = list.sortedBy { item -> item.size }.reversed()
                                    result = filterBy(temp, SIZE_HEAD)
                                }
                                SORT_BY_SIZE_SMALL -> {
                                    val temp = list.sortedBy { item -> item.size }
                                    result = filterBy(temp, SIZE_HEAD)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                imageAdapter.submitList(result)
                                binding.rcv.visible()
                                binding.layoutNoItem.root.gone()
                                binding.layoutLoading.root.gone()
                            }
                        }
                    }
                }
            }
            is Resource.DataError -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.visible()
                binding.layoutLoading.root.gone()
            }
        }
    }


    private fun showMenu(view: View, baseImage: BaseImage) {
        val dialogMore = ImageMoreDialog.newInstance(baseImage)
        dialogMore.setOnRenameListener {
            val dialog = RenameDialog.newInstance(baseImage)
            dialog.setOnOKListener {
                viewModel.requestAllImages()
            }
            dialog.show(childFragmentManager, "RenameDialog")
        }
        dialogMore.setOnInfoListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_IMAGE, baseImage)
            navigateToDes(R.id.imageDetailFragment, bundle)
        }
        dialogMore.show(childFragmentManager, "ImageMoreDialog")
    }

    private fun filterBy(list: List<BaseImage>, typeHead: String): ArrayList<BaseImage> {
        val result = ArrayList<BaseImage>()
        when (typeHead) {
            NAME_HEAD -> {
                list.forEach { baseImage ->
                    val tempNameHead = baseImage.displayName?.substring(0, 1)?.uppercase()
                    val newNameHead = if (tempNameHead?.isAlphabetic == true) tempNameHead else "#"
                    val head = result.filter { it.displayName == newNameHead }
                    if (head.isEmpty()) result.add(BaseImage(id = currentMillis, displayName = newNameHead))
                    result.add(baseImage)
                }
            }
            DATE_HEAD -> {
                list.forEach { baseImage ->
                    val newNameHead = getDateTimeFromMillis(millis = baseImage.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)
                    val head = result.filter { getDateTimeFromMillis(millis = it.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH) == newNameHead }
                    if (head.isEmpty()) result.add(BaseImage(id = currentMillis, displayName = newNameHead))
                    result.add(baseImage)
                }
            }
            SIZE_HEAD -> {
                list.forEach { baseImage ->
                    val newNameHead = when {
                        ((baseImage.size ?: 0) / BYTES_TO_GB) > 0 -> "Larger than 1 GB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 900 -> "900 MB - 1 GB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 800 -> "800 - 900 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 700 -> "700 - 800 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 600 -> "600 - 700 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 500 -> "500 - 600 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 400 -> "400 - 500 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 300 -> "300 - 400 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 200 -> "200 - 300 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 100 -> "100 - 200 MB"
                        ((baseImage.size ?: 0) / BYTES_TO_MB) > 0 -> "1 - 100 MB"
                        else -> "Less than 1 MB"
                    }
                    val head = result.filter {
                        val tempNameHead = when {
                            ((it.size ?: 0) / BYTES_TO_GB) > 0 -> "Larger than 1 GB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 900 -> "900 MB - 1 GB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 800 -> "800 - 900 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 700 -> "700 - 800 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 600 -> "600 - 700 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 500 -> "500 - 600 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 400 -> "400 - 500 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 300 -> "300 - 400 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 200 -> "200 - 300 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 100 -> "100 - 200 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 0 -> "1 - 100 MB"
                            else -> "Less than 1 MB"
                        }

                        tempNameHead == newNameHead
                    }
                    if (head.isEmpty()) result.add(BaseImage(id = currentMillis, displayName = newNameHead))
                    result.add(baseImage)
                }
            }
        }
        return result
    }
}