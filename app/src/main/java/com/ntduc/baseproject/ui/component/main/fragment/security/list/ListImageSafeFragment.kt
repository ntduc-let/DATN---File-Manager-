package com.ntduc.baseproject.ui.component.main.fragment.security.list

import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.FragmentListImageSafeBinding
import com.ntduc.baseproject.databinding.MenuSafeFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FileSafeFolderAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.base.BasePopupWindow
import com.ntduc.baseproject.ui.component.image.ImageViewerActivity
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.currentMillis
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.mimeType
import com.ntduc.baseproject.utils.file.moveTo
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListImageSafeFragment : BaseFragment<FragmentListImageSafeBinding>(R.layout.fragment_list_image_safe) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fileSafeFolderAdapter: FileSafeFolderAdapter

    override fun initView() {
        super.initView()

        fileSafeFolderAdapter = FileSafeFolderAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = fileSafeFolderAdapter
            layoutManager = LinearLayoutManager(requireContext())
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

        fileSafeFolderAdapter.setOnOpenListener { it, list ->
            val listImage = arrayListOf<BaseImage>()
            val currentImage = BaseImage(id = currentMillis, title = it.nameWithoutExtension, displayName = it.name, mimeType = it.mimeType(), size = it.length(), dateAdded = it.lastModified(), dateModified = it.lastModified(), data = it.path, height = null, width = null)
            var currentPosition = 0
            list.indices.forEach {
                listImage.add(BaseImage(id = currentMillis, title = list[it].nameWithoutExtension, displayName = list[it].name, mimeType = list[it].mimeType(), size = list[it].length(), dateAdded = list[it].lastModified(), dateModified = list[it].lastModified(), data = list[it].path, height = null, width = null))
                if (list[it].path == currentImage.data) currentPosition = it
            }
            ImageViewerActivity.openFile(requireContext(), listImage, currentPosition)
        }

        fileSafeFolderAdapter.setOnMoreListener { view, baseImage ->
            showMenu(view, baseImage)
        }
    }

    private fun showMenu(view: View, file: File) {
        val popupBinding = MenuSafeFolderDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupBinding.delete.setOnClickListener {
            file.delete(requireContext())
            val imageFolder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/image")
            File("${imageFolder.path}/${file.name}").delete(requireContext())
            viewModel.loadImageSafe(requireContext())
            popupWindow.dismiss()
        }

        popupBinding.share.setOnClickListener {
            file.share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

        popupBinding.moveOutOfSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogLoading = LoadingEncryptionDialog()
            dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
            lifecycleScope.launch(Dispatchers.IO) {
                val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/image")
                if (!restoreFolder.exists()) {
                    restoreFolder.mkdirs()
                }
                file.moveTo(requireContext(), restoreFolder)

                val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/image")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                File("${folder.path}/${file.name}").delete(requireContext())

                withContext(Dispatchers.Main) {
                    shortToast("Chuyển đổi thành công")
                    dialogLoading.dismiss()
                    viewModel.loadImageSafe(requireContext())
                }
            }
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight + binding.rcv.y.toInt())
    }

    override fun initData() {
        super.initData()

        viewModel.loadImageSafe(requireContext())
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.imageSafeListLiveData, ::handleImageList)
    }

    private fun handleImageList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }

            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (it.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> it.sortedBy { item -> item.name.uppercase() }
                        SORT_BY_NAME_Z_A -> it.sortedBy { item -> item.name.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> it.sortedBy { item -> item.lastModified() }.reversed()
                        SORT_BY_DATE_OLD -> it.sortedBy { item -> item.lastModified() }
                        SORT_BY_SIZE_LARGE -> it.sortedBy { item -> item.length() }.reversed()
                        SORT_BY_SIZE_SMALL -> it.sortedBy { item -> item.length() }
                        else -> listOf()
                    }
                    withContext(Dispatchers.Main) {
                        fileSafeFolderAdapter.submitList(result)
                        binding.rcv.visible()
                        binding.layoutNoItem.root.gone()
                        binding.layoutLoading.root.gone()
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
}