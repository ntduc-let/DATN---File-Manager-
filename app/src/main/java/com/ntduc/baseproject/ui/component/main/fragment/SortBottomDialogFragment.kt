package com.ntduc.baseproject.ui.component.main.fragment

import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.databinding.DialogSortBinding
import com.ntduc.baseproject.ui.base.BaseBottomSheetDialogFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk

class SortBottomDialogFragment : BaseBottomSheetDialogFragment<DialogSortBinding>(R.layout.dialog_sort) {

    private val viewModel: MainViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        binding.sortByNameAZ.title.text = getString(R.string.name_a_to_z)
        binding.sortByNameZA.title.text = getString(R.string.name_z_to_a)
        binding.sortByDateNew.title.text = getString(R.string.newest)
        binding.sortByDateOld.title.text = getString(R.string.oldest)
        binding.sortBySizeLarge.title.text = getString(R.string.largest)
        binding.sortBySizeSmall.title.text = getString(R.string.smallest)
    }

    override fun initData() {
        super.initData()

        resetSort()
        when (Hawk.get(SORT_BY, SORT_BY_NAME_A_Z)) {
            SORT_BY_NAME_A_Z -> {
                binding.sortByNameAZ.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortByNameAZ.select.visible()
            }
            SORT_BY_NAME_Z_A -> {
                binding.sortByNameZA.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortByNameZA.select.visible()
            }
            SORT_BY_DATE_NEW -> {
                binding.sortByDateNew.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortByDateNew.select.visible()
            }
            SORT_BY_DATE_OLD -> {
                binding.sortByDateOld.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortByDateOld.select.visible()
            }
            SORT_BY_SIZE_LARGE -> {
                binding.sortBySizeLarge.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortBySizeLarge.select.visible()
            }
            SORT_BY_SIZE_SMALL -> {
                binding.sortBySizeLarge.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                binding.sortBySizeLarge.select.visible()
            }
        }
    }

    override fun addEvent() {
        super.addEvent()

        binding.sortByNameAZ.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_NAME_A_Z)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }

        binding.sortByNameZA.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_NAME_Z_A)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }

        binding.sortByDateNew.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_DATE_NEW)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }

        binding.sortByDateOld.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_DATE_OLD)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }

        binding.sortBySizeLarge.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_SIZE_LARGE)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }

        binding.sortBySizeSmall.root.setOnClickListener {
            Hawk.put(SORT_BY, SORT_BY_SIZE_SMALL)
            viewModel.requestAllApp()
            viewModel.requestAllApk()
            viewModel.requestAllDocument()
            viewModel.requestAllAudio()
            dismiss()
        }
    }

    private fun resetSort() {
        binding.sortByNameAZ.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))
        binding.sortByNameZA.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))
        binding.sortByDateNew.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))
        binding.sortByDateOld.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))
        binding.sortBySizeLarge.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))
        binding.sortBySizeSmall.title.setTextColor(ContextCompat.getColor(requireContext(), R.color.monochrome_700))

        binding.sortByNameAZ.select.gone()
        binding.sortByNameZA.select.gone()
        binding.sortByDateNew.select.gone()
        binding.sortByDateOld.select.gone()
        binding.sortBySizeLarge.select.gone()
        binding.sortBySizeSmall.select.gone()
    }
}