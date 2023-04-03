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
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.databinding.FragmentPlaylistAudioBinding
import com.ntduc.baseproject.databinding.MenuFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FolderAudioAdapter
import com.ntduc.baseproject.ui.adapter.PlaylistAudioAdapter
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

class PlaylistAudioFragment : BaseFragment<FragmentPlaylistAudioBinding>(R.layout.fragment_playlist_audio) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var playlistAudioAdapter: PlaylistAudioAdapter

    override fun initView() {
        super.initView()

        playlistAudioAdapter = PlaylistAudioAdapter(requireContext())
        binding.rcv.apply {
            adapter = playlistAudioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        playlistAudioAdapter.setOnOpenListener {
//            val bundle = Bundle()
//            bundle.putParcelable(KEY_BASE_FOLDER_AUDIO, it)
//            navigateToDes(R.id.listAudioInFolderFragment, bundle)
        }

        playlistAudioAdapter.setOnMoreListener { view, folderAudioFile ->
//            showMenu(view, folderAudioFile)
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

        viewModel.requestAllPlaylistAudio()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.playlistAudioLiveData, ::handlePlaylistAudio)
    }

    private fun handlePlaylistAudio(status: Resource<List<PlaylistAudioFile>>) {
        when (status) {
            is Resource.Loading -> {
                if (playlistAudioAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newList = arrayListOf<PlaylistAudioFile>()
                    it.forEach {playlistAudio ->
                        newList.add(PlaylistAudioFile(playlistAudio.id, playlistAudio.name, playlistAudio.listFile.filter { File(it.data!!).exists() } as ArrayList<BaseAudio>))
                    }

                    withContext(Dispatchers.Main) {
                        if (newList.isEmpty()) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                            SORT_BY_NAME_A_Z -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.name })
                            }
                            SORT_BY_NAME_Z_A -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.name }.reversed())
                            }
                            SORT_BY_DATE_NEW -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.id }.reversed())
                            }
                            SORT_BY_DATE_OLD -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.id })
                            }
                            SORT_BY_SIZE_LARGE -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.listFile.size }.reversed())
                            }
                            SORT_BY_SIZE_SMALL -> {
                                playlistAudioAdapter.submitList(newList.sortedBy { item -> item.listFile.size })
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
            val pos = checkFolderAudioByPath(audio, listFolderAudio)
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

    private fun checkFolderAudioByPath(
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