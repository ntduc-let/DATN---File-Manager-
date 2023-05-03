package com.ntduc.baseproject.ui.component.main.fragment.home.video

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.brouken.player.PlayerActivity
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.DATE_HEAD
import com.ntduc.baseproject.constant.FAVORITE_VIDEO
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.constant.KEY_BASE_VIDEO
import com.ntduc.baseproject.constant.NAME_HEAD
import com.ntduc.baseproject.constant.RECENT_FILE
import com.ntduc.baseproject.constant.SIZE_HEAD
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.databinding.FragmentListImageBinding
import com.ntduc.baseproject.ui.adapter.VideoAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.main.dialog.VideoMoreDialog
import com.ntduc.baseproject.utils.BYTES_TO_GB
import com.ntduc.baseproject.utils.BYTES_TO_MB
import com.ntduc.baseproject.utils.BYTES_TO_TB
import com.ntduc.baseproject.utils.currentMillis
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.getDateTimeFromMillis
import com.ntduc.baseproject.utils.isAlphabetic
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.security.FileEncryption
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeadersGridLayoutManager
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale

class ListVideoFragment : BaseFragment<FragmentListImageBinding>(R.layout.fragment_list_image) {

    companion object {
        fun newInstance(isFavorite: Boolean): ListVideoFragment {
            val args = Bundle()
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListVideoFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var videoAdapter: VideoAdapter
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        videoAdapter = VideoAdapter(requireContext())
        binding.rcv.apply {
            adapter = videoAdapter
            setHasFixedSize(true)
            val stickyHeadersGridLayoutManager: StickyHeadersGridLayoutManager<VideoAdapter> = StickyHeadersGridLayoutManager(requireContext(), 2)
            stickyHeadersGridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = if (videoAdapter.isStickyHeader(position)) 2 else 1
            }
            layoutManager = stickyHeadersGridLayoutManager
        }
    }

    override fun addEvent() {
        super.addEvent()

        videoAdapter.setOnOpenListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.setDataAndType(it.data!!.toUri(), it.mimeType)
            startActivity(intent)

            updateRecent(it)
        }

        videoAdapter.setOnMoreListener { view, baseVideo ->
            showMenu(view, baseVideo)
        }
    }

    private fun updateRecent(baseVideo: BaseVideo) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseVideo.data) newRecent.remove(it)
        }

        newRecent.add(0, baseVideo.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
    }

    private fun showMenu(view: View, baseVideo: BaseVideo) {
        val dialogMore = VideoMoreDialog.newInstance(baseVideo)
        dialogMore.setOnRenameListener {
            val dialog = RenameDialog.newInstance(baseVideo)
            dialog.setOnOKListener {
                viewModel.requestAllVideos()
            }
            dialog.show(childFragmentManager, "RenameDialog")
        }
        dialogMore.setOnInfoListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_VIDEO, baseVideo)
            navigateToDes(R.id.videoDetailFragment, bundle)
        }
        dialogMore.setOnMoveSafeFolderListener {
            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(it)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/video")
                    if (!folder.exists()) {
                        folder.mkdirs()
                    }
                    FileEncryption.encryptToFile(
                        "$pin$pin$pin$pin",
                        "abcdefghptreqwrf",
                        FileInputStream(File(baseFileEncryption.data!!)),
                        FileOutputStream(File("${folder.path}/${baseFileEncryption.displayName}"))
                    )

                    File(baseFileEncryption.data!!).delete(requireContext())

                    withContext(Dispatchers.Main) {
                        shortToast("Chuyển đổi thành công")
                        dialogLoading.dismiss()
                        viewModel.requestAllVideos()
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
        }
        dialogMore.show(childFragmentManager, "VideoMoreDialog")
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllVideos()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.videoListLiveData, ::handleVideoList)
    }

    private fun handleVideoList(status: Resource<List<BaseVideo>>) {
        when (status) {
            is Resource.Loading -> {
                if (videoAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery1 = arrayListOf<BaseVideo>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery1.add(it)
                    }

                    val listQuery2 = arrayListOf<BaseVideo>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_VIDEO, arrayListOf<String>())
                        listQuery1.forEach {
                            if (listFavorite.contains(it.data)) listQuery2.add(it)
                        }
                    } else {
                        listQuery2.addAll(listQuery1)
                    }

                    if (listQuery2.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }

                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> {
                            val temp = listQuery2.sortedBy { item -> item.displayName?.uppercase() }
                            filterBy(temp, NAME_HEAD)
                        }
                        SORT_BY_NAME_Z_A -> {
                            val temp = listQuery2.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                            filterBy(temp, NAME_HEAD)
                        }
                        SORT_BY_DATE_NEW -> {
                            val temp = listQuery2.sortedBy { item -> item.dateModified }.reversed()
                            filterBy(temp, DATE_HEAD)
                        }
                        SORT_BY_DATE_OLD -> {
                            val temp = listQuery2.sortedBy { item -> item.dateModified }
                            filterBy(temp, DATE_HEAD)
                        }
                        SORT_BY_SIZE_LARGE -> {
                            val temp = listQuery2.sortedBy { item -> item.size }.reversed()
                            filterBy(temp, SIZE_HEAD)
                        }
                        SORT_BY_SIZE_SMALL -> {
                            val temp = listQuery2.sortedBy { item -> item.size }
                            filterBy(temp, SIZE_HEAD)
                        }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main) {
                        videoAdapter.submitList(result)
                        binding.rcv.visible()
                        binding.layoutNoItem.root.gone()
                        binding.layoutLoading.root.gone()
                    }
                }
            }
            is Resource.DataError -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.visible()
                binding.layoutLoading.root.gone()
            }
        }
    }

    private fun filterBy(list: List<BaseVideo>, typeHead: String): ArrayList<BaseVideo> {
        val result = ArrayList<BaseVideo>()
        when (typeHead) {
            NAME_HEAD -> {
                list.forEach { baseVideo ->
                    val tempNameHead = baseVideo.displayName?.substring(0, 1)?.uppercase()
                    val newNameHead = if (tempNameHead?.isAlphabetic == true) tempNameHead else "#"
                    val head = result.filter { it.displayName == newNameHead }
                    if (head.isEmpty()) result.add(BaseVideo(id = currentMillis, displayName = newNameHead))
                    result.add(baseVideo)
                }
            }
            DATE_HEAD -> {
                list.forEach { baseVideo ->
                    val newNameHead = getDateTimeFromMillis(millis = baseVideo.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH)
                    val head = result.filter { getDateTimeFromMillis(millis = it.dateModified ?: 0, dateFormat = "MMM dd yyyy", locale = Locale.ENGLISH) == newNameHead }
                    if (head.isEmpty()) result.add(BaseVideo(id = currentMillis, displayName = newNameHead))
                    result.add(baseVideo)
                }
            }
            SIZE_HEAD -> {
                list.forEach { baseVideo ->
                    val newNameHead = when {
                        ((baseVideo.size ?: 0) / BYTES_TO_TB) > 0 -> "Larger than 1 TB"
                        ((baseVideo.size ?: 0) / BYTES_TO_GB) > 100 -> "100 GB - 1 TB"
                        ((baseVideo.size ?: 0) / BYTES_TO_GB) > 10 -> "10 - 100 GB"
                        ((baseVideo.size ?: 0) / BYTES_TO_GB) > 0 -> "1 - 10 GB"
                        ((baseVideo.size ?: 0) / BYTES_TO_MB) > 500 -> "500 MB - 1 GB"
                        ((baseVideo.size ?: 0) / BYTES_TO_MB) > 200 -> "200 - 500 MB"
                        ((baseVideo.size ?: 0) / BYTES_TO_MB) > 100 -> "100 - 200 MB"
                        ((baseVideo.size ?: 0) / BYTES_TO_MB) > 0 -> "1 - 100 MB"
                        else -> "Less than 1 MB"
                    }
                    val head = result.filter {
                        val tempNameHead = when {
                            ((it.size ?: 0) / BYTES_TO_TB) > 0 -> "Larger than 1 TB"
                            ((it.size ?: 0) / BYTES_TO_GB) > 100 -> "100 GB - 1 TB"
                            ((it.size ?: 0) / BYTES_TO_GB) > 10 -> "10 - 100 GB"
                            ((it.size ?: 0) / BYTES_TO_GB) > 0 -> "1 - 10 GB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 500 -> "500 MB - 1 GB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 200 -> "200 - 500 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 100 -> "100 - 200 MB"
                            ((it.size ?: 0) / BYTES_TO_MB) > 0 -> "1 - 100 MB"
                            else -> "Less than 1 MB"
                        }

                        tempNameHead == newNameHead
                    }
                    if (head.isEmpty()) result.add(BaseVideo(id = currentMillis, displayName = newNameHead))
                    result.add(baseVideo)
                }
            }
        }
        return result
    }
}