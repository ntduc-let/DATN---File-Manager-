package com.ntduc.baseproject.ui.component.main.fragment.security

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.databinding.FragmentHomeSafeBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.formatBytes
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeSafeFragment : BaseFragment<FragmentHomeSafeBinding>(R.layout.fragment_home_safe) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        binding.apps.ic.setImageResource(R.drawable.ic_app_32dp)
        binding.apps.title.text = getString(R.string.apks)
        binding.apps.progress.gone()

        binding.videos.ic.setImageResource(R.drawable.ic_video_32dp)
        binding.videos.title.text = getString(R.string.videos)
        binding.videos.progress.gone()

        binding.document.ic.setImageResource(R.drawable.ic_document_32dp)
        binding.document.title.text = getString(R.string.document)
        binding.document.progress.gone()

        binding.images.ic.setImageResource(R.drawable.ic_image_32dp)
        binding.images.title.text = getString(R.string.images)
        binding.images.progress.gone()

        binding.audio.ic.setImageResource(R.drawable.ic_music_32dp)
        binding.audio.title.text = getString(R.string.audio)
        binding.audio.progress.gone()
    }

    override fun addEvent() {
        super.addEvent()

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }

        binding.apps.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.listApkSafeFragment)
        }

        binding.audio.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.listAudioSafeFragment)
        }

        binding.document.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.documentSafeFragment)
        }

        binding.images.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.listImageSafeFragment)
        }

        binding.videos.root.setOnClickShrinkEffectListener {
            navigateToDes(R.id.listVideoSafeFragment)
        }
    }

    override fun initData() {
        super.initData()

        viewModel.loadApkSafe(requireContext())
        viewModel.loadVideoSafe(requireContext())
        viewModel.loadDocumentSafe(requireContext())
        viewModel.loadImageSafe(requireContext())
        viewModel.loadAudioSafe(requireContext())
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.apkSafeListLiveData, ::handleApkSafeList)
        observe(viewModel.videoSafeListLiveData, ::handleVideoSafeList)
        observe(viewModel.documentSafeListLiveData, ::handleDocumentSafeList)
        observe(viewModel.imageSafeListLiveData, ::handleImageSafeList)
        observe(viewModel.audioSafeListLiveData, ::handleAudioSafeList)
    }

    private fun handleAudioSafeList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.length()
                    }
                    withContext(Dispatchers.Main) {
                        binding.audio.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleImageSafeList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.length()
                    }
                    withContext(Dispatchers.Main) {
                        binding.images.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleDocumentSafeList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.length()
                    }
                    withContext(Dispatchers.Main) {
                        binding.document.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleVideoSafeList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.length()
                    }
                    withContext(Dispatchers.Main) {
                        binding.videos.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }

    private fun handleApkSafeList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {}
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    var sizeUsed = 0L
                    it.forEach {
                        sizeUsed += it.length()
                    }
                    withContext(Dispatchers.Main) {
                        binding.apps.description.text = "${it.size} items ∙ ${sizeUsed.formatBytes()}"
                    }
                }
            }
            is Resource.DataError -> {}
        }
    }
}