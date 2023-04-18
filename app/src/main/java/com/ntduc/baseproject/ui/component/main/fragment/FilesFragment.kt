package com.ntduc.baseproject.ui.component.main.fragment

import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.root.FolderFile
import com.ntduc.baseproject.data.dto.root.RootFolder
import com.ntduc.baseproject.databinding.FragmentFilesBinding
import com.ntduc.baseproject.databinding.MenuDfcDetailBinding
import com.ntduc.baseproject.ui.adapter.FolderFileAdapter
import com.ntduc.baseproject.ui.adapter.RootFolderAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.base.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FilesFragment : BaseFragment<FragmentFilesBinding>(R.layout.fragment_files) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var rootFolderAdapter: RootFolderAdapter
    private lateinit var folderFileAdapter: FolderFileAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private val rootFolders = arrayListOf<RootFolder>()
    private var listPathFiles: ArrayList<String>? = null

    override fun initView() {
        super.initView()

        rootFolders.add(RootFolder(getString(R.string.default_phone_card), Environment.getExternalStorageDirectory().path))

        rootFolderAdapter = RootFolderAdapter(requireContext())
        rootFolderAdapter.submitList(rootFolders)
        binding.rvRootFile.apply {
            adapter = rootFolderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }

        folderFileAdapter = FolderFileAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = folderFileAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun initData() {
        super.initData()
        viewModel.requestAllFolderFile(rootFolders[rootFolders.size - 1].path)
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.folderFileListLiveData, ::handleFolderFileList)
    }

    override fun addEvent() {
        super.addEvent()

        rootFolderAdapter.setOnClickListener {
            viewModel.requestAllFolderFile(it.path)
        }

        folderFileAdapter.setOnClickListener {
            viewModel.requestAllFolderFile(it.data!!)
            val newRoots = arrayListOf<RootFolder>()
            newRoots.addAll(rootFolderAdapter.currentList)
            newRoots.add(RootFolder(it.displayName!!, it.data!!))
            rootFolderAdapter.submitList(newRoots)
        }

        folderFileAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        folderFileAdapter.setOnMoreItemListener { view, folderFile ->
            showMenuItem(view, folderFile)
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (folderFileAdapter.type == FolderFileAdapter.SELECT || folderFileAdapter.type == FolderFileAdapter.MOVE) {
                    setModeAdapter(FolderFileAdapter.NORMAL)
                } else {
                    val r = rootFolderAdapter.currentList
                    val newRoot = arrayListOf<RootFolder>()
                    newRoot.addAll(r)
                    if (newRoot.size != 1) {
                        newRoot.removeLast()
                        rootFolderAdapter.submitList(newRoot)
                        viewModel.requestAllFolderFile(newRoot.last().path)
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun showMenuItem(view: View, folderFile: FolderFile) {
        val popupBinding = MenuDfcDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

//        popupBinding.select.setOnClickListener {
//            folderFile.isSelected = true
//            if (folderFileAdapter.type == FolderFileAdapter.MOVE) return@setOnClickListener
//            if (folderFileAdapter.type == FolderFileAdapter.NORMAL) {
//                setModeAdapter(FolderFileAdapter.SELECT)
//            }
////            model.addOrRemoveFileSelect(folderFile)
//
//            popupWindow.dismiss()
//        }

        popupBinding.share.setOnClickListener {
            File(folderFile.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

//        popupBinding.move.setOnClickListener {
//            listPathFiles = arrayListOf()
//            listPathFiles?.add(folderFile.data!!)
//            popupWindow.dismiss()
//        }

//        popupBinding.delete.setOnClickListener {
//            if (File(folderFile.data ?: "").exists()) {
//                val dialogDelete = Delete()
//                dialogDelete.setOnDeleteListener {
//                    deleteFiles(listOf(File(folderFile.data!!))) {
//                        val pos = adapterFile.getPosition(folderFile)
//                        if (pos != -1) {
//                            model.listFile.value?.removeAt(pos)
//                            model.listFile.postValue(model.listFile.value)
//                        } else {
//                            shortToast(getString(R.string.error))
//                        }
//                    }
//                }
//
//                dialogDelete.show(supportFragmentManager, "DeleteDocumentDialog")
//            } else {
//                shortToast(getString(R.string.file_does_not_exist))
//            }
//            popupWindow.dismiss()
//        }
//
//        popupBinding.itemMenuManagerRename.root.setOnClickListener {
//            if (File(folderFile.data ?: "").exists()) {
//                val dialogRename = RenameDocumentDialog(folderFile.title ?: "")
//                dialogRename.setOnRenameListener {
//                    val pos = adapterFile.getPosition(folderFile)
//                    renameFile(File(folderFile.data!!), it) { file ->
//                        if (pos != -1) {
//                            folderFile.data = file.path
//                            folderFile.title = file.nameWithoutExtension
//                            folderFile.displayName = file.name
//
//                            binding.rvFiles.post {
//                                adapterFile.reloadItem(pos)
//                            }
//                            model.listFile.value?.get(pos)?.data = file.path
//                            model.listFile.value?.get(pos)?.title = file.nameWithoutExtension
//                            model.listFile.value?.get(pos)?.displayName = file.name
//                        } else {
//                            shortToast(getString(R.string.error))
//                        }
//                    }
//                }
//                dialogRename.show(supportFragmentManager, "RenameDocumentDialog")
//            } else {
//                shortToast(getString(R.string.file_does_not_exist))
//            }
//
//            popupWindow.dismiss()
//        }

        popupWindow.showAtLocation(
            view,
            Gravity.TOP or Gravity.END,
            32,
            view.y.toInt() + binding.rcv.y.toInt() + getStatusBarHeight
        )
    }


    private fun setModeAdapter(isMode: Int) {
        when (isMode) {
            FolderFileAdapter.NORMAL -> {
                if (folderFileAdapter.type == FolderFileAdapter.SELECT
                    || folderFileAdapter.type == FolderFileAdapter.MOVE
                ) {
                    folderFileAdapter.changeMode(FolderFileAdapter.NORMAL)
//                    binding.layoutBottomDpc.layoutBottomMenuDpc.root.visibility = View.GONE
//                    binding.layoutBottomDpc.layoutBtnMoveHere.root.visibility = View.GONE
//                    model.removeAllListFileSelected()
                }
            }
            FolderFileAdapter.SELECT -> {
                if (folderFileAdapter.type == FolderFileAdapter.NORMAL
                    || folderFileAdapter.type == FolderFileAdapter.MOVE
                ) {
                    folderFileAdapter.changeMode(FolderFileAdapter.SELECT)
//                    binding.layoutBottomDpc.layoutBottomMenuDpc.root.visibility = View.VISIBLE
//                    binding.layoutBottomDpc.layoutBtnMoveHere.root.visibility = View.GONE
                }
            }
            FolderFileAdapter.MOVE -> {
                if (folderFileAdapter.type == FolderFileAdapter.NORMAL
                    || folderFileAdapter.type == FolderFileAdapter.SELECT
                ) {
                    folderFileAdapter.changeMode(FolderFileAdapter.MOVE)
//                    binding.layoutBottomDpc.layoutBottomMenuDpc.root.visibility = View.GONE
//                    binding.layoutBottomDpc.layoutBtnMoveHere.root.visibility = View.VISIBLE
//                    model.removeAllListFileSelected()
                }
            }
        }
    }

    private fun handleFolderFileList(status: Resource<List<FolderFile>>) {
        when (status) {
            is Resource.Loading -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (it.isEmpty()){
                        withContext(Dispatchers.Main){
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }

                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> it.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> it.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> it.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> it.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> it.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> it.sortedBy { item -> item.size }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main) {
                        folderFileAdapter.submitList(result)
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