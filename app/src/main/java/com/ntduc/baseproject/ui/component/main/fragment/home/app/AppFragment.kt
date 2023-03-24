package com.ntduc.baseproject.ui.component.main.fragment.home.app

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentAppBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentAppAdapter
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener


class AppFragment : BaseFragment<FragmentAppBinding>(R.layout.fragment_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragmentAppAdapter: FragmentAppAdapter


    override fun initView() {
        super.initView()

        fragmentAppAdapter = FragmentAppAdapter(requireActivity())
        binding.vp.adapter = fragmentAppAdapter

        TabLayoutMediator(binding.tab, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.apps)
                1 -> tab.text = resources.getString(R.string.apks)
            }
        }.attach()
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }
    }
}