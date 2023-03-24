package com.ntduc.baseproject.ui.component.main.fragment.home.app

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.ui.adapter.AppAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.openApp
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ListAppFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var appAdapter: AppAdapter

    override fun initView() {
        super.initView()

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

        appAdapter.setOnMoreListener {

        }
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
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }
            is Resource.Success -> status.data?.let {
                if (it.isEmpty()){
                    binding.rcv.gone()
                    binding.layoutNoItem.root.visible()
                    binding.layoutLoading.root.gone()
                    return
                }

                lifecycleScope.launch(Dispatchers.IO){
                    val result = arrayListOf<BaseApp>()
                    it.forEach {item ->
                        if (item.packageName != requireContext().packageName) result.add(item)
                    }
                    withContext(Dispatchers.Main){
                        appAdapter.submitList(result.sortedBy { item -> item.name })

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