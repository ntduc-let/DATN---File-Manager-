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
import com.ntduc.baseproject.constant.FileTypeExtension
import com.ntduc.baseproject.constant.RECENT_FILE
import com.ntduc.baseproject.constant.SORT_BY
import com.ntduc.baseproject.constant.SORT_BY_DATE_NEW
import com.ntduc.baseproject.constant.SORT_BY_DATE_OLD
import com.ntduc.baseproject.constant.SORT_BY_NAME_A_Z
import com.ntduc.baseproject.constant.SORT_BY_NAME_Z_A
import com.ntduc.baseproject.constant.SORT_BY_SIZE_LARGE
import com.ntduc.baseproject.constant.SORT_BY_SIZE_SMALL
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.databinding.FragmentSearchBinding
import com.ntduc.baseproject.ui.adapter.SearchAdapter
import com.ntduc.baseproject.ui.base.BaseFragment
import com.ntduc.baseproject.ui.component.image.ImageViewerActivity
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

    companion object {
        private const val FILTER_ALL = 0
        private const val FILTER_DOCUMENT = 1
        private const val FILTER_VIDEO = 2
        private const val FILTER_IMAGE = 3
        private const val FILTER_AUDIO = 4
        private const val FILTER_APK = 5

        private const val FILTER_DOCUMENT_ALL = 0
        private const val FILTER_DOCUMENT_PDF = 1
        private const val FILTER_DOCUMENT_TXT = 2
        private const val FILTER_DOCUMENT_DOC = 3
        private const val FILTER_DOCUMENT_XLS = 4
        private const val FILTER_DOCUMENT_PPT = 5
    }

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var searchAdapter: SearchAdapter

    private var typeFilter = FILTER_ALL
    private var typeFilterDocument = FILTER_DOCUMENT_ALL

    override fun initView() {
        super.initView()

        setFilter(FILTER_ALL)

        searchAdapter = SearchAdapter(requireContext(), lifecycleScope)
        binding.rcv.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setFilter(filter: Int) {
        resetFilterAll()

        typeFilter = filter

        when (typeFilter) {
            FILTER_ALL -> {
                binding.all.text.setBackgroundResource(R.color.filter_selected)

                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }

            FILTER_DOCUMENT -> {
                binding.document.text.setBackgroundResource(R.color.filter_selected)
                binding.filterDocument.visible()

                setFilterDocument(FILTER_DOCUMENT_ALL)
            }

            FILTER_VIDEO -> {
                binding.video.text.setBackgroundResource(R.color.filter_selected)

                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }

            FILTER_IMAGE -> {
                binding.image.text.setBackgroundResource(R.color.filter_selected)

                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }

            FILTER_AUDIO -> {
                binding.audio.text.setBackgroundResource(R.color.filter_selected)

                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }

            FILTER_APK -> {
                binding.apk.text.setBackgroundResource(R.color.filter_selected)

                viewModel.requestAllSearch(binding.search.query.trim().toString())
            }
        }
    }

    private fun setFilterDocument(filterDocument: Int) {
        resetFilterDocumentAll()

        typeFilterDocument = filterDocument

        when (typeFilterDocument) {
            FILTER_DOCUMENT_ALL -> binding.documentAll.text.setBackgroundResource(R.color.filter_selected)
            FILTER_DOCUMENT_PDF -> binding.documentPdf.text.setBackgroundResource(R.color.filter_selected)
            FILTER_DOCUMENT_TXT -> binding.documentTxt.text.setBackgroundResource(R.color.filter_selected)
            FILTER_DOCUMENT_DOC -> binding.documentDoc.text.setBackgroundResource(R.color.filter_selected)
            FILTER_DOCUMENT_XLS -> binding.documentXls.text.setBackgroundResource(R.color.filter_selected)
            FILTER_DOCUMENT_PPT -> binding.documentPpt.text.setBackgroundResource(R.color.filter_selected)
        }

        viewModel.requestAllSearch(binding.search.query.trim().toString())
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

        binding.all.root.setOnClickListener {
            setFilter(FILTER_ALL)
        }

        binding.document.root.setOnClickListener {
            setFilter(FILTER_DOCUMENT)

        }

        binding.video.root.setOnClickListener {
            setFilter(FILTER_VIDEO)
        }

        binding.image.root.setOnClickListener {
            setFilter(FILTER_IMAGE)
        }

        binding.audio.root.setOnClickListener {
            setFilter(FILTER_AUDIO)
        }

        binding.apk.root.setOnClickListener {
            setFilter(FILTER_APK)
        }

        binding.documentAll.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_ALL)
        }

        binding.documentPdf.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_PDF)
        }

        binding.documentTxt.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_TXT)
        }

        binding.documentDoc.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_DOC)
        }

        binding.documentXls.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_XLS)
        }

        binding.documentPpt.root.setOnClickListener {
            setFilterDocument(FILTER_DOCUMENT_PPT)
        }

        searchAdapter.setOnOpenListener {
            when (FileTypeExtension.getTypeFile(it.data!!)) {
                FileTypeExtension.DOC, FileTypeExtension.XLS, FileTypeExtension.PPT, FileTypeExtension.PDF, FileTypeExtension.TXT -> {
                    OfficeReaderActivity.openFile(requireContext(), it)
                }

                FileTypeExtension.IMAGE -> {
                    ImageViewerActivity.openFile(requireContext(), arrayListOf(BaseImage(id = it.id, title = it.title, displayName = it.displayName, mimeType = it.mimeType, size = it.size, dateAdded = it.dateAdded, dateModified = it.dateModified, data = it.data, height = null, width = null)), 0)
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
                    val temp = arrayListOf<BaseFile>()
                    temp.addAll(it.filter {
                        when (typeFilter) {
                            FILTER_ALL -> true
                            FILTER_VIDEO -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.VIDEO
                            FILTER_IMAGE -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.IMAGE
                            FILTER_AUDIO -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.AUDIO
                            FILTER_APK -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.APK
                            else -> {
                                when (typeFilterDocument) {
                                    FILTER_DOCUMENT_PDF -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.PDF
                                    FILTER_DOCUMENT_TXT -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.TXT
                                    FILTER_DOCUMENT_DOC -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.DOC
                                    FILTER_DOCUMENT_XLS -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.XLS
                                    FILTER_DOCUMENT_PPT -> FileTypeExtension.getTypeFile(it.data!!) == FileTypeExtension.PPT
                                    else -> {
                                        val type = FileTypeExtension.getTypeFile(it.data!!)
                                        type == FileTypeExtension.PDF || type == FileTypeExtension.TXT || type == FileTypeExtension.DOC || type == FileTypeExtension.XLS || type == FileTypeExtension.PPT
                                    }
                                }
                            }
                        }
                    })


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
                        SORT_BY_NAME_A_Z -> temp.sortedBy { item -> item.displayName?.uppercase() }
                        SORT_BY_NAME_Z_A -> temp.sortedBy { item -> item.displayName?.uppercase() }.reversed()
                        SORT_BY_DATE_NEW -> temp.sortedBy { item -> item.dateModified }.reversed()
                        SORT_BY_DATE_OLD -> temp.sortedBy { item -> item.dateModified }
                        SORT_BY_SIZE_LARGE -> temp.sortedBy { item -> item.size }.reversed()
                        SORT_BY_SIZE_SMALL -> temp.sortedBy { item -> item.size }
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


    private fun resetFilterAll() {
        resetStateFilterAll()
        resetStateFilterDocument()
        resetStateFilterVideo()
        resetStateFilterImage()
        resetStateFilterAudio()
        resetStateFilterApk()

        resetFilterDocumentAll()

        binding.filterDocument.gone()
    }

    private fun resetFilterDocumentAll() {
        resetStateFilterDocumentAll()
        resetStateFilterDocumentPdf()
        resetStateFilterDocumentTxt()
        resetStateFilterDocumentDoc()
        resetStateFilterDocumentXls()
        resetStateFilterDocumentPpt()
    }

    private fun resetStateFilterDocumentPpt() {
        binding.documentPpt.text.text = getString(R.string.ppt)
        binding.documentPpt.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocumentXls() {
        binding.documentXls.text.text = getString(R.string.xls)
        binding.documentXls.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocumentDoc() {
        binding.documentDoc.text.text = getString(R.string.doc)
        binding.documentDoc.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocumentTxt() {
        binding.documentTxt.text.text = getString(R.string.txt)
        binding.documentTxt.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocumentPdf() {
        binding.documentPdf.text.text = getString(R.string.pdf)
        binding.documentPdf.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocumentAll() {
        binding.documentAll.text.text = getString(R.string.all)
        binding.documentAll.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterApk() {
        binding.apk.text.text = getString(R.string.apks)
        binding.apk.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterAudio() {
        binding.audio.text.text = getString(R.string.audio)
        binding.audio.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterImage() {
        binding.image.text.text = getString(R.string.images)
        binding.image.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterVideo() {
        binding.video.text.text = getString(R.string.videos)
        binding.video.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterDocument() {
        binding.document.text.text = getString(R.string.document)
        binding.document.text.setBackgroundResource(R.color.blue_second)
    }

    private fun resetStateFilterAll() {
        binding.all.text.text = getString(R.string.all)
        binding.all.text.setBackgroundResource(R.color.blue_second)
    }
}