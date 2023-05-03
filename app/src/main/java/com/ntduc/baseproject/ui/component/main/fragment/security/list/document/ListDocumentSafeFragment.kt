package com.ntduc.baseproject.ui.component.main.fragment.security.list.document

import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuSafeFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FileSafeFolderAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.office.OfficeReaderActivity
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
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

class ListDocumentSafeFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fileSafeFolderAdapter: FileSafeFolderAdapter
    private var type = TYPE_ALL

    companion object {
        private const val TYPE = "TYPE"

        const val TYPE_ALL = 1
        const val TYPE_PDF = 2
        const val TYPE_TXT = 3
        const val TYPE_DOC = 4
        const val TYPE_XLS = 5
        const val TYPE_PPT = 6

        fun newInstance(type: Int): ListDocumentSafeFragment {
            val args = Bundle()
            args.putInt(TYPE, type)

            val fragment = ListDocumentSafeFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        super.initView()

        type = requireArguments().getInt(TYPE, TYPE_ALL)

        fileSafeFolderAdapter = FileSafeFolderAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = fileSafeFolderAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        fileSafeFolderAdapter.setOnOpenListener { it, list ->
            OfficeReaderActivity.openFile(requireContext(), BaseFile(id = currentMillis, title = it.nameWithoutExtension, displayName = it.name, mimeType = it.mimeType(), size = it.length(), dateAdded = it.lastModified(), dateModified = it.lastModified(), data = it.path))
        }

        fileSafeFolderAdapter.setOnMoreListener { view, file ->
            showMenu(view, file)
        }
    }

    private fun showMenu(view: View, file: File) {
        val popupBinding = MenuSafeFolderDetailBinding.inflate(layoutInflater)
        val popupWindow = com.ntduc.baseproject.ui.base.BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupBinding.delete.setOnClickListener {
            file.delete(requireContext())
            val documentFolder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/document")
            File("${documentFolder.path}/${file.name}").delete(requireContext())
            viewModel.loadDocumentSafe(requireContext())
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
                val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/document")
                if (!restoreFolder.exists()) {
                    restoreFolder.mkdirs()
                }
                file.moveTo(requireContext(), restoreFolder)

                val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/document")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                File("${folder.path}/${file.name}").delete(requireContext())

                withContext(Dispatchers.Main) {
                    shortToast("Chuyển đổi thành công")
                    dialogLoading.dismiss()
                    viewModel.loadDocumentSafe(requireContext())
                }
            }
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
    }

    override fun initData() {
        super.initData()

        viewModel.loadDocumentSafe(requireContext())
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.documentSafeListLiveData, ::handleDocumentList)
    }

    private fun handleDocumentList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }

            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val list: List<File> = when (type) {
                        TYPE_ALL -> it
                        TYPE_PDF -> it.filter { item -> FileTypeExtension.getTypeFile(item.path) == FileTypeExtension.PDF }
                        TYPE_TXT -> it.filter { item -> FileTypeExtension.getTypeFile(item.path) == FileTypeExtension.TXT }
                        TYPE_DOC -> it.filter { item -> FileTypeExtension.getTypeFile(item.path) == FileTypeExtension.DOC }
                        TYPE_XLS -> it.filter { item -> FileTypeExtension.getTypeFile(item.path) == FileTypeExtension.XLS }
                        TYPE_PPT -> it.filter { item -> FileTypeExtension.getTypeFile(item.path) == FileTypeExtension.PPT }
                        else -> it
                    }

                    if (list.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }

                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> list.sortedBy { item -> item.name.uppercase() }
                        SORT_BY_NAME_Z_A -> list.sortedBy { item -> item.name.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> list.sortedBy { item -> item.lastModified() }.reversed()
                        SORT_BY_DATE_OLD -> list.sortedBy { item -> item.lastModified() }
                        SORT_BY_SIZE_LARGE -> list.sortedBy { item -> item.length() }.reversed()
                        SORT_BY_SIZE_SMALL -> list.sortedBy { item -> item.length() }
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