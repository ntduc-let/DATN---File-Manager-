package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.folder.FolderAudioFile
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FolderAudioAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListFolderAudioFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var folderAudioAdapter: FolderAudioAdapter

    override fun initView() {
        super.initView()

        folderAudioAdapter = FolderAudioAdapter(requireContext())
        binding.rcv.apply {
            adapter = folderAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        folderAudioAdapter.setOnOpenListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_FOLDER_AUDIO, it)
            navigateToDes(R.id.listAudioInFolderFragment, bundle)
        }

        folderAudioAdapter.setOnMoreListener { view, folderAudioFile ->
            showMenu(view, folderAudioFile)
        }
    }

    private fun showMenu(view: View, folderAudioFile: FolderAudioFile) {
        val popupBinding = MenuFolderDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupBinding.delete.setOnClickListener {
            folderAudioFile.listFile.forEach {
                File(it.data!!).delete(requireContext())
            }
            viewModel.requestAllAudio()
            popupWindow.dismiss()
        }


        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_FOLDER_AUDIO, folderAudioFile)
            navigateToDes(R.id.folderAudioDetailFragment, bundle)
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllAudio()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.audioListLiveData, ::handleAudioList)
    }

    private fun handleAudioList(status: Resource<List<BaseAudio>>) {
        when (status) {
            is Resource.Loading -> {
                if (folderAudioAdapter.currentList.isEmpty()) {
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
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.displayName })
                            }
                            SORT_BY_NAME_Z_A -> {
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.displayName }.reversed())
                            }
                            SORT_BY_DATE_NEW -> {
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.dateModified }.reversed())
                            }
                            SORT_BY_DATE_OLD -> {
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.dateModified })
                            }
                            SORT_BY_SIZE_LARGE -> {
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.size }.reversed())
                            }
                            SORT_BY_SIZE_SMALL -> {
                                folderAudioAdapter.submitList(result.sortedBy { item -> item.baseFile!!.size })
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

    private fun filterFolderFile(list: List<BaseAudio>): ArrayList<FolderAudioFile> {
        val listFolderAudio = ArrayList<FolderAudioFile>()
        var index = 1L
        for (audio in list) {
            val pos = checkFolderByPath(audio, listFolderAudio)
            if (pos >= 0) {
                listFolderAudio[pos].listFile.add(audio)
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

                val folderAudio = FolderAudioFile(folder)
                folderAudio.listFile.add(audio)

                listFolderAudio.add(folderAudio)
            }
        }
        return listFolderAudio
    }

    private fun checkFolderByPath(
        baseAudio: BaseAudio,
        listFolderAudio: ArrayList<FolderAudioFile>
    ): Int {
        for (i in listFolderAudio.indices) {
            if (getPathFolderByPath(baseAudio.data!!) == listFolderAudio[i].baseFile!!.data) {
                return i
            }
        }
        return -1
    }

    private fun getPathFolderByPath(path: String): String {
        return File(path).parent ?: "Unknown"
    }
}