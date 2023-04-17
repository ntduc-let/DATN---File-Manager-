package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.databinding.FragmentAudioBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentAudioAdapter
import com.ntduc.baseproject.ui.adapter.FragmentDocumentAdapter
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener


class AudioFragment : BaseFragment<FragmentAudioBinding>(R.layout.fragment_audio) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragmentAudioAdapter: FragmentAudioAdapter

    override fun initView() {
        super.initView()

        val isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)
        if (isFavorite) binding.title.text = "${getString(R.string.favorite)} ${getString(R.string.audio)}"

        fragmentAudioAdapter = FragmentAudioAdapter(requireActivity(), isFavorite)
        binding.vp.adapter = fragmentAudioAdapter

        TabLayoutMediator(binding.tab, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.all)
                1 -> tab.text = resources.getString(R.string.folder)
//                2 -> tab.text = resources.getString(R.string.playlist)
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