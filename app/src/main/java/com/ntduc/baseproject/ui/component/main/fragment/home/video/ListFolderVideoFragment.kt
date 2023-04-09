package com.ntduc.baseproject.ui.component.main.fragment.home.video

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.data.dto.folder.FolderVideoFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FolderVideoAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListFolderVideoFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var folderVideoAdapter: FolderVideoAdapter

    override fun initView() {
        super.initView()

        folderVideoAdapter = FolderVideoAdapter(requireContext())
        binding.rcv.apply {
            adapter = folderVideoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        folderVideoAdapter.setOnOpenListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_FOLDER_VIDEO, it)
            navigateToDes(R.id.listVideoInFolderFragment, bundle)
        }

        folderVideoAdapter.setOnMoreListener { view, folderVideoFile ->
            showMenu(view, folderVideoFile)
        }
    }

    private fun showMenu(view: View, folderVideoFile: FolderVideoFile) {
        val popupBinding = MenuFolderDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupBinding.delete.setOnClickListener {
            folderVideoFile.listFile.forEach {
                File(it.data!!).delete(requireContext())
            }
            viewModel.requestAllVideos()
            popupWindow.dismiss()
        }


        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_FOLDER_VIDEO, folderVideoFile)
            navigateToDes(R.id.folderVideoDetailFragment, bundle)
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
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
                if (folderVideoAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val result = filterFolderFile(it)

                    withContext(Dispatchers.Main) {
                        if (result.isEmpty()) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                            SORT_BY_NAME_A_Z -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.displayName })
                            }
                            SORT_BY_NAME_Z_A -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.displayName }.reversed())
                            }
                            SORT_BY_DATE_NEW -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.dateModified }.reversed())
                            }
                            SORT_BY_DATE_OLD -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.dateModified })
                            }
                            SORT_BY_SIZE_LARGE -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.size }.reversed())
                            }
                            SORT_BY_SIZE_SMALL -> {
                                folderVideoAdapter.submitList(result.sortedBy { item -> item.baseFile!!.size })
                            }
                        }

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

    private fun filterFolderFile(list: List<BaseVideo>): ArrayList<FolderVideoFile> {
        val listFolderVideo = ArrayList<FolderVideoFile>()
        var index = 1L
        for (audio in list) {
            val pos = checkFolderByPath(audio, listFolderVideo)
            if (pos >= 0) {
                listFolderVideo[pos].listFile.add(audio)
            } else {
                val f = File(audio.data!!)
                val folder = BaseFile(
                    id = index++,
                    f.parentFile?.name ?: "Unknown",
                    f.parentFile?.name ?: "Unknown",
                    "",
                    f.length(),
                    f.lastModified(),
                    f.lastModified(),
                    getPathFolderByPath(f.path)
                )

                val folderVideo = FolderVideoFile(folder)
                folderVideo.listFile.add(audio)

                listFolderVideo.add(folderVideo)
            }
        }
        return listFolderVideo
    }

    private fun checkFolderByPath(
        baseVideo: BaseVideo,
        listFolderVideo: ArrayList<FolderVideoFile>
    ): Int {
        for (i in listFolderVideo.indices) {
            if (getPathFolderByPath(baseVideo.data!!) == listFolderVideo[i].baseFile!!.data) {
                return i
            }
        }
        return -1
    }

    private fun getPathFolderByPath(path: String): String {
        return File(path).parent ?: "Unknown"
    }
}