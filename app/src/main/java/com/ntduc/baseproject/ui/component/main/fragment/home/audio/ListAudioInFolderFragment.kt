package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.folder.FolderAudioFile
import com.ntduc.baseproject.databinding.FragmentListAudioInFolderBinding
import com.ntduc.baseproject.databinding.MenuDocumentDetailBinding
import com.ntduc.baseproject.ui.adapter.AudioAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.navigateToDes
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListAudioInFolderFragment : BaseFragment<FragmentListAudioInFolderBinding>(R.layout.fragment_list_audio_in_folder) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var audioAdapter: AudioAdapter
    private var folderAudioFile: FolderAudioFile? = null
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        folderAudioFile = requireArguments().getParcelable(KEY_BASE_FOLDER_AUDIO)
        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        if (folderAudioFile == null) {
            findNavController().popBackStack()
            return
        }

        binding.title.text = folderAudioFile!!.baseFile!!.displayName

        audioAdapter = AudioAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = audioAdapter
            layoutManager = LinearLayoutManager(requireContext())
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

        audioAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
        }

        audioAdapter.setOnMoreListener { view, baseFile ->
            showMenu(view, baseFile)
        }
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.audioListLiveData, ::handleAudioList)
    }

    private fun handleAudioList(status: Resource<List<BaseAudio>>) {
        when (status) {
            is Resource.Loading -> {
                if (audioAdapter.currentList.isEmpty()) {
                    binding.rcv.gone()
                    binding.layoutNoItem.root.gone()
                    binding.layoutLoading.root.visible()
                }
            }
            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    val listQuery = arrayListOf<BaseAudio>()
                    if (isFavorite) {
                        val listFavorite = Hawk.get(FAVORITE_AUDIO, arrayListOf<String>())
                        it.forEach {
                            if (listFavorite.contains(it.data)) listQuery.add(it)
                        }
                    } else {
                        listQuery.addAll(it)
                    }

                    val list = listQuery.filter { File(it.data!!).parent == folderAudioFile!!.baseFile!!.data }

                    if (list.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> list.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> list.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> list.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> list.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> list.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> list.sortedBy { item -> item.size }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main) {
                        audioAdapter.submitList(result)
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


    private fun showMenu(view: View, baseAudio: BaseAudio) {
        val popupBinding = MenuDocumentDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        var isFavorite = false
        popupBinding.txtFavorite.text = getString(R.string.add_to_favorites)

        run breaking@{
            Hawk.get(FAVORITE_AUDIO, arrayListOf<String>()).forEach {
                if (it == baseAudio.data) {
                    isFavorite = true
                    popupBinding.txtFavorite.text = getString(R.string.remove_from_favorites)
                    return@breaking
                }
            }
        }

        popupBinding.share.setOnClickListener {
            File(baseAudio.data!!).share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

        popupBinding.rename.setOnClickListener {
            val dialog = RenameDialog.newInstance(baseAudio)
            dialog.setOnOKListener {
                audioAdapter.updateItem(baseAudio)
                viewModel.requestAllAudio()
            }
            dialog.show(childFragmentManager, "RenameDialog")
            popupWindow.dismiss()
        }

        popupBinding.delete.setOnClickListener {
            File(baseAudio.data!!).delete(requireContext())

            viewModel.requestAllAudio()
            popupWindow.dismiss()
        }

        popupBinding.favorite.setOnClickListener {
            if (isFavorite) {
                removeFavorite(baseAudio)
            } else {
                addFavorite(baseAudio)
            }
            viewModel.requestAllAudio()
            popupWindow.dismiss()
        }

        popupBinding.info.setOnClickListener {
            val bundle = Bundle()
            bundle.putParcelable(KEY_BASE_AUDIO, baseAudio)
            navigateToDes(R.id.audioDetailFragment, bundle)
            popupWindow.dismiss()
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight + binding.rcv.y.toInt())
    }


    private fun addFavorite(baseAudio: BaseAudio) {
        val favorites = Hawk.get(FAVORITE_AUDIO, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseAudio.data) newFavorites.remove(it)
        }

        newFavorites.add(baseAudio.data!!)

        Hawk.put(FAVORITE_AUDIO, newFavorites)
    }

    private fun removeFavorite(baseAudio: BaseAudio) {
        val favorites = Hawk.get(FAVORITE_AUDIO, arrayListOf<String>())

        val newFavorites = arrayListOf<String>()
        newFavorites.addAll(favorites)

        favorites.forEach {
            if (it == baseAudio.data) newFavorites.remove(it)
        }

        Hawk.put(FAVORITE_AUDIO, newFavorites)
    }
}