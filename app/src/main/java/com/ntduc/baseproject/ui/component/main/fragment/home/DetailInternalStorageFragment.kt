package com.ntduc.baseproject.ui.component.main.fragment.home

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.*
import com.ntduc.baseproject.databinding.FragmentDetailInternalStorageBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.DeviceUtils
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class DetailInternalStorageFragment : BaseFragment<FragmentDetailInternalStorageBinding>(R.layout.fragment_detail_internal_storage) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        binding.itemStorageDetail.used.title.text = getString(R.string.used)
        binding.itemStorageDetail.available.title.text = getString(R.string.available)
        binding.itemStorageDetail.total.title.text = getString(R.string.total)

        binding.apps.ic.setImageResource(R.drawable.ic_app_32dp)
        binding.apps.title.text = getString(R.string.apps)
        binding.apps.progress.gone()

        binding.videos.ic.setImageResource(R.drawable.ic_video_32dp)
        binding.videos.title.text = getString(R.string.videos)

        binding.document.ic.setImageResource(R.drawable.ic_document_32dp)
        binding.document.title.text = getString(R.string.document)

        binding.images.ic.setImageResource(R.drawable.ic_image_32dp)
        binding.images.title.text = getString(R.string.images)

        binding.audio.ic.setImageResource(R.drawable.ic_music_32dp)
        binding.audio.title.text = getString(R.string.audio)
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }

        binding.apps.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.appFragment, Bundle())
        }

        binding.document.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.documentFragment, Bundle())
        }

        binding.audio.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.audioFragment, Bundle())
        }

        binding.images.root.setOnClickShrinkEffectListener{
            navigateToDes(R.id.imageFragment, Bundle())
        }

        binding.videos.root.setOnClickShrinkEffectListener{
            navigateToDes(R.id.videoFragment, Bundle())
        }
    }

    override fun initData() {
        super.initData()

        loadSizeInternalMemorySize()

        viewModel.requestAllApp()
        viewModel.requestAllVideos()
        viewModel.requestAllDocument()
        viewModel.requestAllImages()
        viewModel.requestAllAudio()
    }

    private fun loadSizeInternalMemorySize() {
        val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
        val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
        val useSize = totalInternalMemorySize - availableInternalMemorySize

        val value = (useSize / totalInternalMemorySize * 100).toInt()
        binding.itemStorageDetail.numberProgress.text = "$value%"
        binding.itemStorageDetail.icProgress.progress = value.toFloat()
        binding.itemStorageDetail.used.size.text = useSize.toLong().formatBytes()
        binding.itemStorageDetail.available.size.text = availableInternalMemorySize.toLong().formatBytes()
        binding.itemStorageDetail.total.size.text = totalInternalMemorySize.toLong().formatBytes()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.appListLiveData, ::handleAppList)
        observe(viewModel.videoListLiveData, ::handleVideoList)
        observe(viewModel.documentListLiveData, ::handleDocumentList)
        observe(viewModel.imageListLiveData, ::handleImageList)
        observe(viewModel.audioListLiveData, ::handleAudioList)
    }

    private fun handleAppList(status: Resource<List<BaseApp>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.size ?: 0
                    }
                    val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
                    val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
                    val useSize = totalInternalMemorySize - availableInternalMemorySize
                    val percentUsed = (sizeUsed.toDouble() / useSize) * 100

                    withContext(Dispatchers.Main) {
                        if (sizeUsed != 0L) {
                            binding.apps.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                            binding.apps.progress.visible()
                            binding.apps.progress.progress = percentUsed.toInt()
                        } else {
                            binding.apps.description.text = "${it.size} items"
                            binding.apps.progress.gone()
                        }
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleVideoList(status: Resource<List<BaseVideo>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseVideo>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery.add(it)
                    }

                    var sizeUsed = 0L
                    listQuery.forEach {
                        sizeUsed += it.size ?: 0
                    }
                    val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
                    val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
                    val useSize = totalInternalMemorySize - availableInternalMemorySize
                    val percentUsed = (sizeUsed.toDouble() / useSize) * 100

                    withContext(Dispatchers.Main) {
                        binding.videos.description.text = "${listQuery.size} items ∙ ${sizeUsed.formatBytes()}"
                        binding.videos.progress.progress = percentUsed.toInt()
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleDocumentList(status: Resource<List<BaseFile>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseFile>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery.add(it)
                    }

                    var sizeUsed = 0L
                    listQuery.forEach {
                        sizeUsed += it.size ?: 0
                    }
                    val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
                    val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
                    val useSize = totalInternalMemorySize - availableInternalMemorySize
                    val percentUsed = (sizeUsed.toDouble() / useSize) * 100

                    withContext(Dispatchers.Main) {
                        binding.document.description.text = "${listQuery.size} items ∙ ${sizeUsed.formatBytes()}"
                        binding.document.progress.progress = percentUsed.toInt()
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleImageList(status: Resource<List<BaseImage>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseImage>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery.add(it)
                    }

                    var sizeUsed = 0L
                    listQuery.forEach {
                        sizeUsed += it.size ?: 0
                    }
                    val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
                    val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
                    val useSize = totalInternalMemorySize - availableInternalMemorySize
                    val percentUsed = (sizeUsed.toDouble() / useSize) * 100

                    withContext(Dispatchers.Main) {
                        binding.images.description.text = "${listQuery.size} items ∙ ${sizeUsed.formatBytes()}"
                        binding.images.progress.progress = percentUsed.toInt()
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleAudioList(status: Resource<List<BaseAudio>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseAudio>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery.add(it)
                    }

                    var sizeUsed = 0L
                    listQuery.forEach {
                        sizeUsed += it.size ?: 0
                    }
                    val availableInternalMemorySize = DeviceUtils.getAvailableInternalMemorySize().toDouble()
                    val totalInternalMemorySize = DeviceUtils.getTotalInternalMemorySize().toDouble()
                    val useSize = totalInternalMemorySize - availableInternalMemorySize
                    val percentUsed = (sizeUsed.toDouble() / useSize) * 100

                    withContext(Dispatchers.Main) {
                        binding.audio.description.text = "${listQuery.size} items ∙ ${sizeUsed.formatBytes()}"
                        binding.audio.progress.progress = percentUsed.toInt()
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }
}