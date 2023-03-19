package com.ntduc.baseproject.ui.component.main.fragment

import androidx.fragment.app.activityViewModels
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentCleanBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel

class CleanFragment : BaseFragment<FragmentCleanBinding>(R.layout.fragment_clean) {

    private val viewModel: MainViewModel by activityViewModels()

}