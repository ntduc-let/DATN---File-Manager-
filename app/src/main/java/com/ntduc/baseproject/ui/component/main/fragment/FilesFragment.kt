package com.ntduc.baseproject.ui.component.main.fragment

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.KEY_BASE_DOCUMENT
import com.ntduc.baseproject.constant.KEY_BASE_FOLDER
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.root.RootFolder
import com.ntduc.baseproject.databinding.FragmentFilesBinding
import com.ntduc.baseproject.databinding.MenuDfcDetailBinding
import com.ntduc.baseproject.ui.adapter.FolderFileAdapter
import com.ntduc.baseproject.ui.adapter.RootFolderAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.base.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.CreateFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.moveTo
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.security.FileEncryption
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FilesFragment : BaseFragment<FragmentFilesBinding>(R.layout.fragment_files) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var rootFolderAdapter: RootFolderAdapter
    private lateinit var folderFileAdapter: FolderFileAdapter
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var rootFolders = arrayListOf<RootFolder>()
    private val handler = Handler(Looper.getMainLooper())

    override fun initView() {
        super.initView()

        if (rootFolders.isEmpty()) rootFolders.add(RootFolder(getString(R.string.default_phone_card), Environment.getExternalStorageDirectory().path))

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
        viewModel.requestAllFolderFile(rootFolders.last().path)
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
            rootFolders = newRoots
            rootFolderAdapter.submitList(rootFolders)
        }

        folderFileAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        folderFileAdapter.setOnMoreItemListener { view, folderFile ->
            showMenuItem(view, folderFile)
        }

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val r = rootFolderAdapter.currentList
                val newRoot = arrayListOf<RootFolder>()
                newRoot.addAll(r)
                if (newRoot.size != 1) {
                    newRoot.removeLast()
                    rootFolders = newRoot
                    rootFolderAdapter.submitList(rootFolders)
                    viewModel.requestAllFolderFile(rootFolders.last().path)
                } else {
                    if (folderFileAdapter.type == FolderFileAdapter.MOVE) {
                        setModeAdapter(FolderFileAdapter.NORMAL)
                        folderFileAdapter.removeFileMove()
                    } else {
                        findNavController().popBackStack()
                    }
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.layoutLoading.root.setOnClickListener { /*Nothing*/ }
        binding.layoutNoItem.root.setOnClickListener { /*Nothing*/ }

        binding.create.setOnClickShrinkEffectListener {
            val newRoots = arrayListOf<RootFolder>()
            newRoots.addAll(rootFolderAdapter.currentList)
            rootFolders = newRoots

            val dialog = CreateFolderDialog.newInstance(rootFolders.last().path)
            dialog.setOnOKListener {
                viewModel.requestAllFolderFile(rootFolders.last().path)
            }
            dialog.show(childFragmentManager, "CreateFolderDialog")
        }

        binding.move.setOnClickShrinkEffectListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val newRoots = arrayListOf<RootFolder>()
                newRoots.addAll(rootFolderAdapter.currentList)
                rootFolders = newRoots

                val startFile = folderFileAdapter.getFileMove()
                if (startFile.data == rootFolders.last().path) return@launch

                withContext(Dispatchers.Main) {
                    binding.layoutLoading.root.visible()
                }

                File(startFile.data!!).moveTo(requireContext(), File(rootFolders.last().path), true)

                withContext(Dispatchers.Main) {
                    setModeAdapter(FolderFileAdapter.NORMAL)
                    folderFileAdapter.removeFileMove()
                    viewModel.requestAllFolderFile(rootFolders.last().path)
                }
            }
        }
    }

    private fun showMenuItem(view: View, folderFile: BaseFile) {
        val popupBinding = MenuDfcDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        if (File(folderFile.data!!).isDirectory) {
            popupBinding.txtInfo.text = getString(R.string.folder_info)
            popupBinding.moveToSafeFolder.gone()
            popupBinding.share.gone()
        } else {
            popupBinding.txtInfo.text = getString(R.string.file_info)
            popupBinding.moveToSafeFolder.visible()
            popupBinding.share.visible()
        }

        if (FileTypeExtension.getTypeFile(folderFile.data!!) == FileTypeExtension.OTHER) {
            popupBinding.moveToSafeFolder.gone()
        } else {
            popupBinding.moveToSafeFolder.visible()
        }

        popupBinding.share.setOnClickListener {
            File(folderFile.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

        popupBinding.move.setOnClickListener {
            setModeAdapter(FolderFileAdapter.MOVE)
            folderFileAdapter.setFileMove(folderFile)
            popupWindow.dismiss()
        }

        popupBinding.rename.setOnClickListener {
            val dialog = RenameDialog.newInstance(folderFile)
            dialog.setOnOKListener {
                val newRoots = arrayListOf<RootFolder>()
                newRoots.addAll(rootFolderAdapter.currentList)
                rootFolders = newRoots

                viewModel.requestAllFolderFile(rootFolders.last().path)
            }
            dialog.show(childFragmentManager, "RenameDialog")
            popupWindow.dismiss()
        }

        popupBinding.delete.setOnClickListener {
            popupWindow.dismiss()
            lifecycleScope.launch(Dispatchers.IO) {
                val newRoots = arrayListOf<RootFolder>()
                newRoots.addAll(rootFolderAdapter.currentList)
                rootFolders = newRoots

                withContext(Dispatchers.Main) {
                    binding.layoutLoading.root.visible()
                }

                File(folderFile.data!!).delete(requireContext())

                withContext(Dispatchers.Main) {
                    val newRoots = arrayListOf<RootFolder>()
                    newRoots.addAll(rootFolderAdapter.currentList)
                    rootFolders = newRoots

                    viewModel.requestAllFolderFile(rootFolders.last().path)
                }
            }
        }

        popupBinding.info.setOnClickListener {
            if (File(folderFile.data!!).isDirectory) {
                val bundle = Bundle()
                bundle.putParcelable(KEY_BASE_FOLDER, folderFile)
                navigateToDes(R.id.folderDetailFragment, bundle)
                popupWindow.dismiss()
            } else {
                val bundle = Bundle()
                bundle.putParcelable(KEY_BASE_DOCUMENT, folderFile)
                navigateToDes(R.id.documentDetailFragment, bundle)
                popupWindow.dismiss()
            }
        }

        popupBinding.moveToSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(folderFile)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = when (FileTypeExtension.getTypeFile(folderFile.data!!)) {
                        FileTypeExtension.APK -> File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/apk")
                        FileTypeExtension.VIDEO -> File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/video")
                        FileTypeExtension.IMAGE -> File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/image")
                        FileTypeExtension.AUDIO -> File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/audio")
                        else -> File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/document")
                    }
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

                    val newRoots = arrayListOf<RootFolder>()
                    newRoots.addAll(rootFolderAdapter.currentList)
                    rootFolders = newRoots

                    withContext(Dispatchers.Main) {
                        shortToast("Chuyển đổi thành công")
                        dialogLoading.dismiss()
                        viewModel.requestAllFolderFile(rootFolders.last().path)
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
        }

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
                if (folderFileAdapter.type == FolderFileAdapter.MOVE) {
                    folderFileAdapter.changeMode(FolderFileAdapter.NORMAL)
                    binding.move.gone()
                }
            }
            FolderFileAdapter.MOVE -> {
                if (folderFileAdapter.type == FolderFileAdapter.NORMAL) {
                    folderFileAdapter.changeMode(FolderFileAdapter.MOVE)
                    binding.move.visible()
                }
            }
        }
    }

    private fun handleFolderFileList(status: Resource<List<BaseFile>>) {
        when (status) {
            is Resource.Loading -> {
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }

            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (it.isEmpty()) {
                        withContext(Dispatchers.Main) {
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
                        handler.postDelayed({
                            binding.layoutNoItem.root.gone()
                            binding.layoutLoading.root.gone()
                        }, 1000)
                    }
                }
            }

            is Resource.DataError -> {
                binding.layoutNoItem.root.visible()
                binding.layoutLoading.root.gone()
            }
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }
}