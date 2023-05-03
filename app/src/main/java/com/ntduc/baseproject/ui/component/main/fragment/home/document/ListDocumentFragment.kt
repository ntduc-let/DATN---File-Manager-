package com.ntduc.baseproject.ui.component.main.fragment.home.document

import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuDocumentDetailBinding
import com.ntduc.baseproject.ui.adapter.DocumentAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.office.OfficeReaderActivity
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
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

class ListDocumentFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var documentAdapter: DocumentAdapter
    private var type = TYPE_ALL
    private var isFavorite: Boolean = false

    companion object {
        private const val TYPE = "TYPE"

        const val TYPE_ALL = 1
        const val TYPE_PDF = 2
        const val TYPE_TXT = 3
        const val TYPE_DOC = 4
        const val TYPE_XLS = 5
        const val TYPE_PPT = 6

        fun newInstance(type: Int, isFavorite: Boolean): ListDocumentFragment {
            val args = Bundle()
            args.putInt(TYPE, type)
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListDocumentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        super.initView()

        type = requireArguments().getInt(TYPE, TYPE_ALL)
        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        documentAdapter = DocumentAdapter(requireContext())
        binding.rcv.apply {
            adapter = documentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        documentAdapter.setOnOpenListener {
            OfficeReaderActivity.openFile(requireContext(), it)
            updateRecent(it)
        }

        documentAdapter.setOnMoreListener { view, baseFile ->
            showMenu(view, baseFile)
        }
    }

    private fun updateRecent(baseFile: BaseFile) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseFile.data) newRecent.remove(it)
        }

        newRecent.add(0, baseFile.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
    }

    private fun showMenu(view: View, baseFile: BaseFile) {
        val popupBinding = MenuDocumentDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        var isFavorite = false
        popupBinding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_DOCUMENT, arrayListOf<String>()).forEach {
                if (it == baseFile.data) {
                    isFavorite = true
                    popupBinding.txtFavorite.text = getString(R.string.remove_from_favorites)
                    return@breaking
                }
            }
        }

        popupBinding.share.setOnClickListener {
            File(baseFile.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

        popupBinding.rename.setOnClickListener {
            val dialog = RenameDialog.newInstance(baseFile)
            dialog.setOnOKListener {
                documentAdapter.updateItem(baseFile)
                viewModel.requestAllDocument()
            }
            dialog.show(childFragmentManager, "RenameDialog")
            popupWindow.dismiss()
        }

        popupBinding.delete.setOnClickListener {
            File(baseFile.data!!).delete(requireContext())
            viewModel.requestAllDocument()
            popupWindow.dismiss()
        }

        popupBinding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseFile)
            } else {
                addFavorite(baseFile)
            }
            viewModel.requestAllDocument()
            popupWindow.dismiss()
        }

        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_DOCUMENT, baseFile)
            navigateToDes(R.id.documentDetailFragment, bundle)
            popupWindow.dismiss()
        }

        popupBinding.moveToSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(baseFile)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/document")
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
                        viewModel.requestAllDocument()
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
    }


    private fun addFavorite(baseFile: BaseFile) {
        val favorites = Hawk.get(FAVORITE_DOCUMENT, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseFile.data) newFavorites.remove(it)
        }

        newFavorites.add(baseFile.data!!)

        Hawk.put(FAVORITE_DOCUMENT, newFavorites)
    }

    private fun removeFavorite(baseFile: BaseFile) {
        val favorites = Hawk.get(FAVORITE_DOCUMENT, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseFile.data) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_DOCUMENT, newFavorites)
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllDocument()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.documentListLiveData, ::handleDocumentList)
    }

    private fun handleDocumentList(status: Resource<List<BaseFile>>) {
        when (status) {
            is Resource.Loading -> {
                if (documentAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery1 = arrayListOf<BaseFile>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery1.add(it)
                    }

                    val listQuery2 = arrayListOf<BaseFile>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_DOCUMENT, arrayListOf<String>())
                        listQuery1.forEach {
                            if (listFavorite.contains(it.data)) listQuery2.add(it)
                        }
                    } else {
                        listQuery2.addAll(listQuery1)
                    }

                    val list: List<BaseFile> = when (type) {
                        TYPE_ALL -> listQuery2
                        TYPE_PDF -> listQuery2.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.PDF }
                        TYPE_TXT -> listQuery2.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.TXT }
                        TYPE_DOC -> listQuery2.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.DOC }
                        TYPE_XLS -> listQuery2.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.XLS }
                        TYPE_PPT -> listQuery2.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.PPT }
                        else -> listQuery2
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
                        SORT_BY_NAME_A_Z -> list.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> list.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> list.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> list.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> list.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> list.sortedBy { item -> item.size }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main) {
                        documentAdapter.submitList(result)
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