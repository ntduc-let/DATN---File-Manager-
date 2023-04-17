package com.ntduc.baseproject.ui.component.main.fragment.home.image

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.databinding.FragmentImageBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentAudioAdapter
import com.ntduc.baseproject.ui.adapter.FragmentImageAdapter
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener


class ImageFragment : BaseFragment<FragmentImageBinding>(R.layout.fragment_image) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragmentImageAdapter: FragmentImageAdapter


    override fun initView() {
        super.initView()

        val isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)
        if (isFavorite) binding.title.text = "${getString(R.string.favorite)} ${getString(R.string.images)}"

        fragmentImageAdapter = FragmentImageAdapter(requireActivity(), isFavorite)
        binding.vp.adapter = fragmentImageAdapter

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