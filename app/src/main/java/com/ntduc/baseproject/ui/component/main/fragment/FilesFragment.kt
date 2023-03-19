package com.ntduc.baseproject.ui.component.main.fragment

import androidx.fragment.app.activityViewModels
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentFilesBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel

class FilesFragment : BaseFragment<FragmentFilesBinding>(R.layout.fragment_files) {

    private val viewModel: MainViewModel by activityViewModels()

}