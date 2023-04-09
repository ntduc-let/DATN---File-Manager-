package com.ntduc.baseproject.ui.component.main.fragment.home.video

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentImageBinding
import com.ntduc.baseproject.databinding.FragmentVideoBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentVideoAdapter
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener


class VideoFragment : BaseFragment<FragmentVideoBinding>(R.layout.fragment_video) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragmentVideoAdapter: FragmentVideoAdapter


    override fun initView() {
        super.initView()

        fragmentVideoAdapter = FragmentVideoAdapter(requireActivity())
        binding.vp.adapter = fragmentVideoAdapter

        TabLayoutMediator(binding.tab, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.all)
                1 -> tab.text = resources.getString(R.string.folder)
            }
        }.attach()
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }

        binding.sort.setOnClickShrinkEffectListener {
            val dialog = SortBottomDialogFragment()
            dialog.show(childFragmentManager, "SortDialog")
        }
    }
}