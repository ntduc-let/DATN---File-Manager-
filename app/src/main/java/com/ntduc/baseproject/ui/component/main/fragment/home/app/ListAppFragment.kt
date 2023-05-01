package com.ntduc.baseproject.ui.component.main.fragment.home.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuAppDetailBinding
import com.ntduc.baseproject.ui.adapter.AppAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListAppFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    companion object {

        fun newInstance(isFavorite: Boolean): ListAppFragment {
            val args = Bundle()
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListAppFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appAdapter: AppAdapter
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        appAdapter = AppAdapter(requireContext())
        binding.rcv.apply {
            adapter = appAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        appAdapter.setOnOpenListener {
            requireActivity().openApp(it.packageName!!)
        }

        appAdapter.setOnMoreListener { view, baseApp ->
            showMenu(view, baseApp)
        }
    }

    private fun showMenu(view: View, baseApp: BaseApp) {
        val popupBinding = MenuAppDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        var isFavorite = false
        popupBinding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_APP, arrayListOf<String>()).forEach {
                if (it == baseApp.packageName) {
                    isFavorite = true
                    popupBinding.txtFavorite.text = getString(R.string.remove_from_favorites)
                    return@breaking
                }
            }
        }

        popupBinding.uninstall.setOnClickListener {
            requireActivity().uninstallApp(baseApp.packageName!!)
            popupWindow.dismiss()
        }

        popupBinding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseApp)
            } else {
                addFavorite(baseApp)
            }
            viewModel.requestAllApp()
            popupWindow.dismiss()
        }

        popupBinding.info.setOnClickListener {
            requireActivity().openSettingApp(baseApp.packageName!!)
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
    }

    private fun addFavorite(baseApp: BaseApp) {
        val favorites = Hawk.get(FAVORITE_APP, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseApp.packageName) newFavorites.remove(it)
        }

        newFavorites.add(baseApp.packageName!!)

        Hawk.put(FAVORITE_APP, newFavorites)
    }

    private fun removeFavorite(baseApp: BaseApp) {
        val favorites = Hawk.get(FAVORITE_APP, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseApp.packageName) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_APP, newFavorites)
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllApp()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.appWithoutSystemListLiveData, ::handleAppList)
    }

    private fun handleAppList(status: Resource<List<BaseApp>>) {
        when (status) {
            is Resource.Loading -> {
                if (appAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseApp>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_APP, arrayListOf<String>())
                        it.forEach {
                            if (listFavorite.contains(it.packageName)) listQuery.add(it)
                        }
                    } else {
                        listQuery.addAll(it)
                    }

                    if (listQuery.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }

                    val temp = arrayListOf<BaseApp>()
                    listQuery.forEach { item ->
                        if (item.packageName != requireContext().packageName) temp.add(item)
                    }
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> temp.sortedBy { item -> item.name }
                        SORT_BY_NAME_Z_A -> temp.sortedBy { item -> item.name }.reversed()
                        SORT_BY_DATE_NEW -> temp.sortedBy { item -> item.firstInstallTime }.reversed()
                        SORT_BY_DATE_OLD -> temp.sortedBy { item -> item.firstInstallTime }
                        SORT_BY_SIZE_LARGE -> temp.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> temp.sortedBy { item -> item.size }
                        else -> listOf()
                    }
                    withContext(Dispatchers.Main) {
                        appAdapter.submitList(result)
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