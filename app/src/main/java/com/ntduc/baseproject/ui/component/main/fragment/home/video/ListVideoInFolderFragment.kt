package com.ntduc.baseproject.ui.component.main.fragment.home.video

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.data.dto.folder.FolderVideoFile
import com.ntduc.baseproject.databinding.FragmentListVideoInFolderBinding
import com.ntduc.baseproject.ui.adapter.VideoAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.main.dialog.VideoMoreDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.ntduc.recyclerviewsticky.StickyHeadersGridLayoutManager
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

class ListVideoInFolderFragment : BaseFragment<FragmentListVideoInFolderBinding>(R.layout.fragment_list_video_in_folder) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var videoAdapter: VideoAdapter
    private var folderVideoFile: FolderVideoFile? = null

    override fun initView() {
        super.initView()

        if (arguments == null) {
            findNavController().popBackStack()
            return
        }

        folderVideoFile = requireArguments().getParcelable(KEY_BASE_FOLDER_VIDEO)

        if (folderVideoFile == null) {
            findNavController().popBackStack()
            return
        }

        binding.title.text = folderVideoFile!!.baseFile!!.displayName

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

        binding.back.setOnClickShrinkEffectListener {
            findNavController().popBackStack()
        }

        binding.sort.setOnClickShrinkEffectListener {
            val dialog = SortBottomDialogFragment()
            dialog.show(childFragmentManager, "SortDialog")
        }

        videoAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        videoAdapter.setOnMoreListener { view, baseVideo ->
            showMenu(view, baseVideo)
        }
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
                    val list = it.filter { File(it.data!!).parent == folderVideoFile!!.baseFile!!.data }

                    withContext(Dispatchers.Main) {
                        if (list.isEmpty()) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }

                        withContext(Dispatchers.IO) {
                            var result = listOf<BaseVideo>()

                            when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                                SORT_BY_NAME_A_Z -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }
                                    result = filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_NAME_Z_A -> {
                                    val temp = list.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                                    result = filterBy(temp, NAME_HEAD)
                                }
                                SORT_BY_DATE_NEW -> {
                                    val temp = list.sortedBy { item -> item.dateModified }.reversed()
                                    result = filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_DATE_OLD -> {
                                    val temp = list.sortedBy { item -> item.dateModified }
                                    result = filterBy(temp, DATE_HEAD)
                                }
                                SORT_BY_SIZE_LARGE -> {
                                    val temp = list.sortedBy { item -> item.size }.reversed()
                                    result = filterBy(temp, SIZE_HEAD)
                                }
                                SORT_BY_SIZE_SMALL -> {
                                    val temp = list.sortedBy { item -> item.size }
                                    result = filterBy(temp, SIZE_HEAD)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                videoAdapter.submitList(result)
                                binding.rcv.visible()
                                binding.layoutNoItem.root.gone()
                                binding.layoutLoading.root.gone()
                            }
                        }
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
        dialogMore.show(childFragmentManager, "VideoMoreDialog")
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