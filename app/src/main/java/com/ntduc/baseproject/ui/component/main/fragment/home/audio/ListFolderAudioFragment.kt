package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVORITE_AUDIO
import com.ntduc.baseproject.constant.IS_FAVORITE
import com.ntduc.baseproject.constant.KEY_BASE_FOLDER_AUDIO
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
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

class ListFolderAudioFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    companion object {
        fun newInstance(isFavorite: Boolean): ListFolderAudioFragment {
            val args = Bundle()
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListFolderAudioFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var folderAudioAdapter: FolderAudioAdapter
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

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
            bundle.putBoolean(IS_FAVORITE, isFavorite)
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
                    val listQuery1 = arrayListOf<BaseAudio>()

                    it.forEach {
                        if (!it.data!!.startsWith(File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}").path)) listQuery1.add(it)
                    }

                    val listQuery2 = arrayListOf<BaseAudio>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_AUDIO, arrayListOf<String>())
                        listQuery1.forEach {
                            if (listFavorite.contains(it.data)) listQuery2.add(it)
                        }
                    } else {
                        listQuery2.addAll(listQuery1)
                    }

                    val temp = filterFolderFile(listQuery2)
                    if (temp.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }

                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> temp.sortedBy { item -> item.baseFile!!.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> temp.sortedBy { item -> item.baseFile!!.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> temp.sortedBy { item -> item.baseFile!!.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> temp.sortedBy { item -> item.baseFile!!.dateModified }
                        SORT_BY_SIZE_LARGE -> temp.sortedBy { item -> item.baseFile!!.size }.reversed()
                        SORT_BY_SIZE_SMALL -> temp.sortedBy { item -> item.baseFile!!.size }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main){
                        folderAudioAdapter.submitList(result)
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