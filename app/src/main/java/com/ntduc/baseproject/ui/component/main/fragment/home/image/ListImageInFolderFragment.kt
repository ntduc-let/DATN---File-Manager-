package com.ntduc.baseproject.ui.component.main.fragment.home.image

import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.DATE_HEAD
import com.ntduc.baseproject.constant.FAVORITE_IMAGE
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.constant.KEY_BASE_FOLDER_IMAGE
import com.ntduc.baseproject.constant.KEY_BASE_IMAGE
import com.ntduc.baseproject.constant.NAME_HEAD
import com.ntduc.baseproject.constant.RECENT_FILE
import com.ntduc.baseproject.constant.SIZE_HEAD
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.folder.FolderImageFile
import com.ntduc.baseproject.databinding.FragmentListImageInFolderBinding
import com.ntduc.baseproject.ui.adapter.ImageAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.ImageMoreDialog
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.BYTES_TO_GB
import com.ntduc.baseproject.utils.BYTES_TO_MB
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.currentMillis
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.isAlphabetic
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.security.FileEncryption
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeadersGridLayoutManager
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class ListImageInFolderFragment : BaseFragment<FragmentListImageInFolderBinding>(R.layout.fragment_list_image_in_folder) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var imageAdapter: ImageAdapter
    private var folderImageFile: FolderImageFile? = null
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        folderImageFile = requireArguments().getParcelable(KEY_BASE_FOLDER_IMAGE)
        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

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
            updateRecent(it)
        }

        imageAdapter.setOnMoreListener { view, baseImage ->
            showMenu(view, baseImage)
        }
    }

    private fun updateRecent(baseImage: BaseImage) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseImage.data) newRecent.remove(it)
        }

        newRecent.add(0, baseImage.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
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
                    val listQuery1 = arrayListOf<BaseImage>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery1.add(it)
                    }

                    val listQuery2 = arrayListOf<BaseImage>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_IMAGE, arrayListOf<String>())
                        listQuery1.forEach {
                            if (listFavorite.contains(it.data)) listQuery2.add(it)
                        }
                    } else {
                        listQuery2.addAll(listQuery1)
                    }

                    val list = listQuery2.filter { File(it.data!!).parent == folderImageFile!!.baseFile!!.data }

                    withContext(Dispatchers.Main) {
                        if (list.isEmpty()) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }

                        withContext(Dispatchers.IO) {
                            val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                                SORT_BY_NAME_A_Z -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }
                                    filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_NAME_Z_A -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                                    filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_DATE_NEW -> {
                                    val temp = list.sortedBy { item -> item.dateModified }.reversed()
                                    filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_DATE_OLD -> {
                                    val temp = list.sortedBy { item -> item.dateModified }
                                    filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_SIZE_LARGE -> {
                                    val temp = list.sortedBy { item -> item.size }.reversed()
                                    filterBy(temp, SIZE_HEAD)
                                }
                                SORT_BY_SIZE_SMALL -> {
                                    val temp = list.sortedBy { item -> item.size }
                                    filterBy(temp, SIZE_HEAD)
                                }
                                else -> listOf()
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
        dialogMore.setOnMoveSafeFolderListener {
            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(it)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/image")
                    if (!folder.exists()) {
                        folder.mkdirs()
                    }
                    FileEncryption.encryptToFile(
                        "$pin$pin$pin$pin",
                        "abcdefghptreqwrf",
                        FileInputStream(File(baseFileEncryption.data!!)),
                        FileOutputStream(File("${folder.path}/${baseFileEncryption.displayName}"))
                    )

                    File(baseFileEncryption.data!!).delete(requireContext())

                    withContext(Dispatchers.Main) {
                        shortToast("Chuyển đổi thành công")
                        dialogLoading.dismiss()
                        viewModel.requestAllImages()
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
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