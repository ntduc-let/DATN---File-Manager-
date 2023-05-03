package com.ntduc.baseproject.ui.component.main.fragment.home.search

import android.content.Intent
import android.widget.SearchView
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brouken.player.PlayerActivity
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.databinding.FragmentSearchBinding
import com.ntduc.baseproject.ui.adapter.SearchAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.main.MainViewModel
import com.ntduc.baseproject.ui.component.main.fragment.SortBottomDialogFragment
import com.ntduc.baseproject.ui.component.office.OfficeReaderActivity
import com.ntduc.baseproject.utils.clickeffect.setOnClickShrinkEffectListener
import com.ntduc.baseproject.utils.file.open
import com.ntduc.baseproject.utils.observe
import com.ntduc.baseproject.utils.view.gone
import com.ntduc.baseproject.utils.view.visible
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class SearchFragment : BaseFragment<FragmentSearchBinding>(R.layout.fragment_search) {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var searchAdapter: SearchAdapter

    override fun initView() {
        super.initView()

        searchAdapter = SearchAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = searchAdapter
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
            dialog.setOnSortListener {
                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }
            dialog.show(childFragmentManager, "SortDialog")
        }

        searchAdapter.setOnOpenListener {
            when(FileTypeExtension.getTypeFile(it.data!!)){
                FileTypeExtension.DOC, FileTypeExtension.XLS, FileTypeExtension.PPT, FileTypeExtension.PDF, FileTypeExtension.TXT -> {
                    OfficeReaderActivity.openFile(requireContext(), it)
                }
                FileTypeExtension.VIDEO -> {
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.setDataAndType(it.data!!.toUri(), it.mimeType)
                    startActivity(intent)
                }
                else -> {
                    File(it.data!!).open(requireContext(), "${requireContext().packageName}.provider")
                }
            }
            updateRecent(it)
        }

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.requestAllSearch(newText.trim())
                return false
            }
        })
    }

    private fun updateRecent(baseFile: BaseFile) {
        val recent = Hawk.get(RECENT_FILE, arrayListOf<String>())

        val newRecent = arrayListOf<String>()
        newRecent.addAll(recent)

        recent.forEach {
            if (it == baseFile.data) newRecent.remove(it)
        }

        newRecent.add(0, baseFile.data!!)

        Hawk.put(RECENT_FILE, newRecent)
        viewModel.requestAllRecent()
    }

    override fun initData() {
        super.initData()

        viewModel.requestAllSearch()
    }

    override fun addObservers() {
        super.addObservers()
        observe(viewModel.searchListLiveData, ::handleSearchList)
    }

    private fun handleSearchList(status: Resource<List<BaseFile>>) {
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
                        SORT_BY_NAME_A_Z -> it.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> it.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> it.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> it.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> it.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> it.sortedBy { item -> item.size }
                        else -> listOf()
                    }

                    withContext(Dispatchers.Main) {
                        searchAdapter.submitList(result)
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