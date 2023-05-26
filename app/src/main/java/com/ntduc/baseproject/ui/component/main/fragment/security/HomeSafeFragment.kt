package com.ntduc.baseproject.ui.component.main.fragment.security

import android.os.Environment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ntduc.baseproject.R
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.databinding.FragmentHomeSafeBinding
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.UnlockDialog
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.moveTo
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

        binding.unlock.setOnClickShrinkEffectListener {
            val dialog = UnlockDialog.newInstance(true)
            dialog.setOnDeleteListener {
                dialog.dismiss()
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")

                lifecycleScope.launch(Dispatchers.IO) {
                    File(Environment.getExternalStorageDirectory().path + "/.${requireContext().getString(R.string.app_name)}/.SafeFolder/").delete(requireContext())

                    withContext(Dispatchers.Main) {
                        dialogLoading.dismiss()
                        findNavController().popBackStack()
                    }
                }
            }
            dialog.setOnRestoreListener {
                dialog.dismiss()
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")

                lifecycleScope.launch(Dispatchers.IO) {
                    restoreApk()
                    restoreVideo()
                    restoreDocument()
                    restoreImage()
                    restoreAudio()

                    File(Environment.getExternalStorageDirectory().path + "/.${requireContext().getString(R.string.app_name)}/.SafeFolder/").delete(requireContext())

                    withContext(Dispatchers.Main) {
                        dialogLoading.dismiss()
                        findNavController().popBackStack()
                    }
                }
            }
            dialog.show(childFragmentManager, "UnlockDialog")
        }
    }

    private fun restoreApk() {
        val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/apk")
        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs()
        }

        val cacheFolder = File(requireContext().filesDir.path + "/.SafeFolder/apk")
        cacheFolder.listFiles()?.forEach {
            it.moveTo(requireContext(), restoreFolder)
        }

        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/apk")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder.listFiles()?.forEach {
            File("${folder.path}/${it.name}").delete(requireContext())
        }
    }

    private fun restoreVideo() {
        val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/video")
        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs()
        }

        val cacheFolder = File(requireContext().filesDir.path + "/.SafeFolder/video")
        cacheFolder.listFiles()?.forEach {
            it.moveTo(requireContext(), restoreFolder)
        }

        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/video")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder.listFiles()?.forEach {
            File("${folder.path}/${it.name}").delete(requireContext())
        }
    }

    private fun restoreDocument() {
        val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/document")
        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs()
        }

        val cacheFolder = File(requireContext().filesDir.path + "/.SafeFolder/document")
        cacheFolder.listFiles()?.forEach {
            it.moveTo(requireContext(), restoreFolder)
        }

        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/document")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder.listFiles()?.forEach {
            File("${folder.path}/${it.name}").delete(requireContext())
        }
    }

    private fun restoreImage() {
        val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/image")
        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs()
        }

        val cacheFolder = File(requireContext().filesDir.path + "/.SafeFolder/image")
        cacheFolder.listFiles()?.forEach {
            it.moveTo(requireContext(), restoreFolder)
        }

        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/image")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder.listFiles()?.forEach {
            File("${folder.path}/${it.name}").delete(requireContext())
        }
    }

    private fun restoreAudio() {
        val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/audio")
        if (!restoreFolder.exists()) {
            restoreFolder.mkdirs()
        }

        val cacheFolder = File(requireContext().filesDir.path + "/.SafeFolder/audio")
        cacheFolder.listFiles()?.forEach {
            it.moveTo(requireContext(), restoreFolder)
        }

        val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/audio")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        folder.listFiles()?.forEach {
            File("${folder.path}/${it.name}").delete(requireContext())
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