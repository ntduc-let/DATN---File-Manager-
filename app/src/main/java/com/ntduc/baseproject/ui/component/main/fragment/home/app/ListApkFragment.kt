package com.ntduc.baseproject.ui.component.main.fragment.home.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuApkDetailBinding
import com.ntduc.baseproject.ui.adapter.ApkAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import java.io.File


class ListApkFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var apkAdapter: ApkAdapter

    override fun initView() {
        super.initView()

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
            apkAdapter.removeItem(baseApk)
            popupWindow.dismiss()
        }

        popupBinding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseApk)
                apkAdapter.updateItem(baseApk)
            } else {
                addFavorite(baseApk)
                apkAdapter.updateItem(baseApk)
            }
            popupWindow.dismiss()
        }

        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_APK, baseApk)
            navigateToDes(R.id.apkDetailFragment, bundle)
            popupWindow.dismiss()
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
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }
            is Resource.Success -> status.data?.let {
                if (it.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.visible()
                    binding.layoutLoading.root.gone()
                    return
                }

                when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                    SORT_BY_NAME_A_Z -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.title })
                    }
                    SORT_BY_NAME_Z_A -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.title }.reversed())
                    }
                    SORT_BY_DATE_NEW -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.dateModified }.reversed())
                    }
                    SORT_BY_DATE_OLD -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.dateModified })
                    }
                    SORT_BY_SIZE_LARGE -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.size }.reversed())
                    }
                    SORT_BY_SIZE_SMALL -> {
                        apkAdapter.submitList(it.sortedBy { item -> item.size })
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