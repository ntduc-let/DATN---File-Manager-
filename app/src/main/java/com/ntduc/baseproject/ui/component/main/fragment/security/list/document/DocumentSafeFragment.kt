package com.ntduc.baseproject.ui.component.main.fragment.security.list.document

import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.databinding.FragmentDocumentSafeBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.adapter.FragmentDocumentAdapter
import com.ntduc.baseproject.ui.adapter.FragmentDocumentSafeAdapter
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener


class DocumentSafeFragment : BaseFragment<FragmentDocumentSafeBinding>(R.layout.fragment_document_safe) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fragmentDocumentSafeAdapter: FragmentDocumentSafeAdapter


    override fun initView() {
        super.initView()

        fragmentDocumentSafeAdapter = FragmentDocumentSafeAdapter(requireActivity())
        binding.vp.adapter = fragmentDocumentSafeAdapter

        TabLayoutMediator(binding.tab, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.all)
                1 -> tab.text = resources.getString(R.string.pdf)
                2 -> tab.text = resources.getString(R.string.txt)
                3 -> tab.text = resources.getString(R.string.doc)
                4 -> tab.text = resources.getString(R.string.xls)
                5 -> tab.text = resources.getString(R.string.ppt)
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