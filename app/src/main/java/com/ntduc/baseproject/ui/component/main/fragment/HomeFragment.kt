package com.ntduc.baseproject.ui.component.main.fragment

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.constant.RECENT_FILE
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentHomeBinding
import com.ntduc.baseproject.ui.adapter.RecentFilesAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.DeviceUtils
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import java.io.File

class HomeFragment : BaseFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var recentFilesAdapter: RecentFilesAdapter

    override fun initView() {
        super.initView()

        recentFilesAdapter = RecentFilesAdapter(requireContext(), lifecycleScope)
        binding.recent.rcv.apply {
            adapter = recentFilesAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.favorite.apps.ic.setImageResource(R.drawable.ic_app_favorite_32dp)
        binding.favorite.apps.title.text = getString(R.string.apps)
        binding.favorite.videos.ic.setImageResource(R.drawable.ic_video_favorite_32dp)
        binding.favorite.videos.title.text = getString(R.string.videos)
        binding.favorite.document.ic.setImageResource(R.drawable.ic_document_favorite_32dp)
        binding.favorite.document.title.text = getString(R.string.document)
        binding.favorite.images.ic.setImageResource(R.drawable.ic_image_favorite_32dp)
        binding.favorite.images.title.text = getString(R.string.images)
        binding.favorite.audio.ic.setImageResource(R.drawable.ic_music_favorite_32dp)
        binding.favorite.audio.title.text = getString(R.string.audio)
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllRecent()
        loadSizeInternalMemorySize()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.recentListLiveData, ::handleRecentList)
    }

    private fun handleRecentList(status: Resource<List<BaseFile>>) {
        when (status) {
            is Resource.Loading -> {
                if (recentFilesAdapter.currentList.isEmpty()) {
                    binding.recent.root.gone()
                }
            }
            is Resource.Success -> status.data?.let {
                if (it.isEmpty()){
                    binding.recent.root.gone()
                    return@let
                }

                recentFilesAdapter.submitList(it)
                binding.recent.root.visible()
            }
            is Resource.DataError -> {
                binding.recent.root.gone()
            }
        }
    }

    override fun addEvent() {
        super.addEvent()

        recentFilesAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
            updateRecent(it)
        }

        binding.search.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.searchFragment)
        }

        binding.internalStorage.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.detailInternalStorageFragment)
        }

        binding.favorite.apps.root.setOnClickShrinkEffectListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_FAVORITE, true)
            navigateToDes(R.id.appFragment, bundle)
        }

        binding.favorite.videos.root.setOnClickShrinkEffectListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_FAVORITE, true)
            navigateToDes(R.id.videoFragment, bundle)
        }

        binding.favorite.document.root.setOnClickShrinkEffectListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_FAVORITE, true)
            navigateToDes(R.id.documentFragment, bundle)
        }

        binding.favorite.images.root.setOnClickShrinkEffectListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_FAVORITE, true)
            navigateToDes(R.id.imageFragment, bundle)
        }

        binding.favorite.audio.root.setOnClickShrinkEffectListener {
            val bundle = Bundle()
            bundle.putBoolean(IS_FAVORITE, true)
            navigateToDes(R.id.audioFragment, bundle)
        }
    }

    private fun updateRecent(baseFile: BaseFile) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseFile.data) newRecent.remove(it)
        }

        newRecent.add(0, baseFile.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
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