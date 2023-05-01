package com.ntduc.baseproject.ui.component.main.fragment.home.app

import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_APK
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.constant.KEY_BASE_APK
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuApkDetailBinding
import com.ntduc.baseproject.ui.adapter.ApkAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.installApk
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

class ListApkFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    companion object {

        fun newInstance(isFavorite: Boolean): ListApkFragment {
            val args = Bundle()
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListApkFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var apkAdapter: ApkAdapter
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        apkAdapter = ApkAdapter(requireContext())
        binding.rcv.apply {
            adapter = apkAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        apkAdapter.setOnOpenListener {
            requireActivity().installApk(it.data!!, "${requireContext().packageName}.provider")
        }

        apkAdapter.setOnMoreListener { view, baseApk ->
            showMenu(view, baseApk)
        }
    }

    private fun showMenu(view: View, baseApk: BaseApk) {
        val popupBinding = MenuApkDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        var isFavorite = false
        popupBinding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_APK, arrayListOf<String>()).forEach {
                if (it == baseApk.data) {
                    isFavorite = true
                    popupBinding.txtFavorite.text = getString(R.string.remove_from_favorites)
                    return@breaking
                }
            }
        }

        popupBinding.delete.setOnClickListener {
            File(baseApk.data!!).delete(requireContext())
            viewModel.requestAllApk()
            popupWindow.dismiss()
        }

        popupBinding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseApk)
            } else {
                addFavorite(baseApk)
            }
            viewModel.requestAllApk()
            popupWindow.dismiss()
        }

        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_APK, baseApk)
            navigateToDes(R.id.apkDetailFragment, bundle)
            popupWindow.dismiss()
        }

        popupBinding.moveToSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(baseApk)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/apk")
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
                        viewModel.requestAllApk()
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
    }


    private fun addFavorite(baseApk: BaseApk) {
        val favorites = Hawk.get(FAVORITE_APK, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseApk.data) newFavorites.remove(it)
        }

        newFavorites.add(baseApk.data!!)

        Hawk.put(FAVORITE_APK, newFavorites)
    }

    private fun removeFavorite(baseApk: BaseApk) {
        val favorites = Hawk.get(FAVORITE_APK, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseApk.data) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_APK, newFavorites)
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllApk()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.apkListLiveData, ::handleApkList)
    }

    private fun handleApkList(status: Resource<List<BaseApk>>) {
        when (status) {
            is Resource.Loading -> {
                if (apkAdapter.currentList.isEmpty()){
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery1 = arrayListOf<BaseApk>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery1.add(it)
                    }

                    val listQuery2 = arrayListOf<BaseApk>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_APK, arrayListOf<String>())
                        listQuery1.forEach {
                            if (listFavorite.contains(it.data)) listQuery2.add(it)
                        }
                    } else {
                        listQuery2.addAll(listQuery1)
                    }

                    if (listQuery2.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> listQuery2.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> listQuery2.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> listQuery2.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> listQuery2.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> listQuery2.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> listQuery2.sortedBy { item -> item.size }
                        else -> listOf()
                    }
                    withContext(Dispatchers.Main) {
                        apkAdapter.submitList(result)
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