package com.ntduc.baseproject.ui.component.main.fragment.home.audio

import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.databinding.FragmentListAppBinding
import com.ntduc.baseproject.databinding.MenuDocumentDetailBinding
import com.ntduc.baseproject.ui.adapter.AudioAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.dialog.MoveSafeFolderDialog
import com.ntduc.baseproject.ui.component.main.dialog.RenameDialog
import com.ntduc.baseproject.utils.*
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.security.FileEncryption
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ListAudioFragment : BaseFragment<FragmentListAppBinding>(R.layout.fragment_list_app) {

    companion object {
        fun newInstance(isFavorite: Boolean): ListAudioFragment {
            val args = Bundle()
            args.putBoolean(IS_FAVORITE, isFavorite)

            val fragment = ListAudioFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var audioAdapter: AudioAdapter
    private var isFavorite: Boolean = false

    override fun initView() {
        super.initView()

        isFavorite = requireArguments().getBoolean(IS_FAVORITE, false)

        audioAdapter = AudioAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = audioAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun addEvent() {
        super.addEvent()

        audioAdapter.setOnOpenListener {
            File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
            updateRecent(it)
        }

        audioAdapter.setOnMoreListener { view, baseFile ->
            showMenu(view, baseFile)
        }
    }

    private fun updateRecent(baseAudio: BaseAudio) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseAudio.data) newRecent.remove(it)
        }

        newRecent.add(0, baseAudio.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
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

        popupBinding.moveToSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogSafeFolder = MoveSafeFolderDialog.newInstance(baseAudio)
            dialogSafeFolder.setOnMoveListener { baseFileEncryption, pin ->
                val dialogLoading = LoadingEncryptionDialog()
                dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
                lifecycleScope.launch(Dispatchers.IO) {
                    val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/audio")
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
                        viewModel.requestAllAudio()
                    }
                }
            }
            dialogSafeFolder.show(childFragmentManager, "MoveSafeFolderDialog")
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight)
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
                if (audioAdapter.currentList.isEmpty()) {
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
                        SORT_BY_NAME_A_Z -> listQuery2.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> listQuery2.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> listQuery2.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> listQuery2.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> listQuery2.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> listQuery2.sortedBy { item -> item.size }
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
}