package com.ntduc.baseproject.ui.component.main.fragment

import android.content.Intent
import android.provider.Settings
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.databinding.FragmentHomeBinding
import com.ntduc.baseproject.ui.adapter.RecentFilesAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.DeviceUtils
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.navigateToDes

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var recentFilesAdapter: RecentFilesAdapter

    override fun initView() {
        super.initView()

        recentFilesAdapter = RecentFilesAdapter(requireContext())
        binding.recent.rcv.apply {
            adapter = recentFilesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun initData() {
        super.initData()

        loadSizeInternalMemorySize()
    }

    override fun addEvent() {
        super.addEvent()

        binding.internalStorage.root.setOnClickShrinkEffectListener{
            navigateToDes(R.id.detailInternalStorageFragment)
        }
    }

    private fun loadSizeInternalMemorySize() {
        val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
        val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
        val useSize = totalInternalMemorySize - availableInternalMemorySize

        val value = (useSize / totalInternalMemorySize * 100).toInt()
        binding.internalStorage.numberProgress.text = "$value%"
        binding.internalStorage.icProgress.progress = value.toFloat()
        binding.internalStorage.description.text = "${useSize.toLong().formatBytes()} of ${totalInternalMemorySize.toLong().formatBytes()} used"
    }
}