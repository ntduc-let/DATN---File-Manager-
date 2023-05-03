package com.ntduc.baseproject.ui.component.main.fragment.security.list

import android.content.Intent
import android.os.Environment
import android.view.Gravity
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brouken.player.PlayerActivity
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.databinding.FragmentListVideoSafeBinding
import com.ntduc.baseproject.databinding.MenuSafeFolderDetailBinding
import com.ntduc.baseproject.ui.adapter.FileSafeFolderAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.base.BasePopupWindow
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.dialog.LoadingEncryptionDialog
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.utils.activity.getStatusBarHeight
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.context.displayHeight
import com.ntduc.baseproject.utils.dp
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.mimeType
import com.ntduc.baseproject.utils.file.moveTo
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.file.share
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.toast.shortToast
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ListVideoSafeFragment : BaseFragment<FragmentListVideoSafeBinding>(R.layout.fragment_list_video_safe) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var fileSafeFolderAdapter: FileSafeFolderAdapter

    override fun initView() {
        super.initView()

        fileSafeFolderAdapter = FileSafeFolderAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = fileSafeFolderAdapter
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

        fileSafeFolderAdapter.setOnOpenListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.setDataAndType(it.path.toUri(), it.mimeType())
            startActivity(intent)
        }

        fileSafeFolderAdapter.setOnMoreListener { view, file ->
            showMenu(view, file)
        }
    }

    private fun showMenu(view: View, file: File) {
        val popupBinding = MenuSafeFolderDetailBinding.inflate(layoutInflater)
        val popupWindow = BasePopupWindow(popupBinding.root)
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 10f

        popupBinding.delete.setOnClickListener {
            file.delete(requireContext())
            val videoFolder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/video")
            File("${videoFolder.path}/${file.name}").delete(requireContext())
            viewModel.loadVideoSafe(requireContext())
            popupWindow.dismiss()
        }

        popupBinding.share.setOnClickListener {
            file.share(requireContext(), "${requireContext().packageName}.provider")
            popupWindow.dismiss()
        }

        popupBinding.moveOutOfSafeFolder.setOnClickListener {
            popupWindow.dismiss()

            val dialogLoading = LoadingEncryptionDialog()
            dialogLoading.show(childFragmentManager, "LoadingEncryptionDialog")
            lifecycleScope.launch(Dispatchers.IO) {
                val restoreFolder = File(Environment.getExternalStorageDirectory().path + "/${getString(R.string.app_name)}/Restore/video")
                if (!restoreFolder.exists()) {
                    restoreFolder.mkdirs()
                }
                file.moveTo(requireContext(), restoreFolder)

                val folder = File(Environment.getExternalStorageDirectory().path + "/.${getString(R.string.app_name)}/.SafeFolder/video")
                if (!folder.exists()) {
                    folder.mkdirs()
                }
                File("${folder.path}/${file.name}").delete(requireContext())

                withContext(Dispatchers.Main) {
                    shortToast("Chuyển đổi thành công")
                    dialogLoading.dismiss()
                    viewModel.loadVideoSafe(requireContext())
                }
            }
        }

        popupWindow.showAtLocation(view, Gravity.TOP or Gravity.END, 8.dp, view.y.toInt() + (requireActivity().displayHeight - binding.root.height) + requireActivity().getStatusBarHeight + binding.rcv.y.toInt())
    }

    override fun initData() {
        super.initData()

        viewModel.loadVideoSafe(requireContext())
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.videoSafeListLiveData, ::handleVideoList)
    }

    private fun handleVideoList(status: Resource<List<File>>) {
        when (status) {
            is Resource.Loading -> {
                binding.rcv.gone()
                binding.layoutNoItem.root.gone()
                binding.layoutLoading.root.visible()
            }

            is Resource.Success -> status.data?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (it.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            binding.rcv.gone()
                            binding.layoutNoItem.root.visible()
                            binding.layoutLoading.root.gone()
                            return@withContext
                        }
                        return@launch
                    }
                    val result = when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
                        SORT_BY_NAME_A_Z -> it.sortedBy { item -> item.name.uppercase() }
                        SORT_BY_NAME_Z_A -> it.sortedBy { item -> item.name.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> it.sortedBy { item -> item.lastModified() }.reversed()
                        SORT_BY_DATE_OLD -> it.sortedBy { item -> item.lastModified() }
                        SORT_BY_SIZE_LARGE -> it.sortedBy { item -> item.length() }.reversed()
                        SORT_BY_SIZE_SMALL -> it.sortedBy { item -> item.length() }
                        else -> listOf()
                    }
                    withContext(Dispatchers.Main) {
                        fileSafeFolderAdapter.submitList(result)
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