package com.ntduc.baseproject.ui.component.main.fragment.home.document

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuDocumentDetailBinding
import com.ntduc.baseproject.ui.adapter.DocumentAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import java.io.File

class ListDocumentFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var documentAdapter: DocumentAdapter
    private var type = TYPE_ALL

    companion object {
        private const val TYPE = "TYPE"

        const val TYPE_ALL = 1
        const val TYPE_PDF = 2
        const val TYPE_TXT = 3
        const val TYPE_DOC = 4
        const val TYPE_XLS = 5
        const val TYPE_PPT = 6

        fun newInstance(type: Int): ListDocumentFragment {
            val args = Bundle()
            args.putInt(TYPE, type)

            val fragment = ListDocumentFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun initView() {
        super.initView()

        if (arguments == null) {
            findNavController().popBackStack()
            return
        }

        type = requireArguments().getInt(TYPE, TYPE_ALL)

        documentAdapter = DocumentAdapter(requireContext())
        binding.rcv.apply {
            adapter = documentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        documentAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        documentAdapter.setOnMoreListener { view, baseFile ->
            showMenu(view, baseFile)
        }
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
                val list: List<BaseFile> = when (type) {
                    TYPE_ALL -> {
                        it
                    }
                    TYPE_PDF -> {
                        it.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.PDF }
                    }
                    TYPE_TXT -> {
                        it.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.TXT }
                    }
                    TYPE_DOC -> {
                        it.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.DOC }
                    }
                    TYPE_XLS -> {
                        it.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.XLS }
                    }
                    TYPE_PPT -> {
                        it.filter { item -> FileTypeExtension.getTypeFile(item.data!!) == FileTypeExtension.PPT }
                    }
                    else -> {
                        it
                    }
                }

                if (list.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.visible()
                    binding.layoutLoading.root.gone()
                    return
                }

                when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                    SORT_BY_NAME_A_Z -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.displayName })
                    }
                    SORT_BY_NAME_Z_A -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.displayName }.reversed())
                    }
                    SORT_BY_DATE_NEW -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.dateModified }.reversed())
                    }
                    SORT_BY_DATE_OLD -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.dateModified })
                    }
                    SORT_BY_SIZE_LARGE -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.size }.reversed())
                    }
                    SORT_BY_SIZE_SMALL -> {
                        documentAdapter.submitList(list.sortedBy { item -> item.size })
                    }
                }

                binding.rcv.visible()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.gone()
            }
            is Resource.DataError -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.visible()
                binding.layoutLoading.root.gone()
            }
        }
    }
}