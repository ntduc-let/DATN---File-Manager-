package com.ntduc.baseproject.ui.component.main.fragment.home.app

import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.ui.adapter.ApkAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk


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

        }

        apkAdapter.setOnMoreListener {

        }
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