package com.ntduc.baseproject.ui.component.main.fragment.home.app

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuApkDetailBinding
import com.ntduc.baseproject.ui.adapter.ApkAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.installApk
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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
                    val listQuery = arrayListOf<BaseApk>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_APK, arrayListOf<String>())
                        it.forEach {
                            if (listFavorite.contains(it.data)) listQuery.add(it)
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
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> listQuery.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> listQuery.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> listQuery.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> listQuery.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> listQuery.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> listQuery.sortedBy { item -> item.size }
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