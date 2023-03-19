package com.ntduc.baseproject.ui.component.main

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ntduc.baseproject.constant.FileType
import com.ntduc.baseproject.data.DataRepositorySource
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.*
import com.ntduc.baseproject.data.dto.files.Files
import com.ntduc.baseproject.ui.base.BaseViewModel
import com.ntduc.baseproject.ui.component.detail.DetailActivity
import com.ntduc.baseproject.utils.wrapEspressoIdlingResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataRepositorySource
) : BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val apkListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val apkListLiveData: LiveData<Resource<List<BaseFile>>> get() = apkListLiveDataPrivate

    fun requestAllApk(){
        viewModelScope.launch {
            apkListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllApk().collect{
                    apkListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val appWithoutSystemListLiveDataPrivate = MutableLiveData<Resource<List<BaseApp>>>()
    val appWithoutSystemListLiveData: LiveData<Resource<List<BaseApp>>> get() = appWithoutSystemListLiveDataPrivate

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val appListLiveDataPrivate = MutableLiveData<Resource<List<BaseApp>>>()
    val appListLiveData: LiveData<Resource<List<BaseApp>>> get() = appListLiveDataPrivate

    fun requestAllApp(){
        viewModelScope.launch {
            appWithoutSystemListLiveDataPrivate.value = Resource.Loading()
            appListLiveDataPrivate.value = Resource.Loading()

            wrapEspressoIdlingResource {
                repository.requestAllApp(false).collect{
                    appWithoutSystemListLiveDataPrivate.value = it
                }
                repository.requestAllApp(true).collect{
                    appListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val videoListLiveDataPrivate = MutableLiveData<Resource<List<BaseVideo>>>()
    val videoListLiveData: LiveData<Resource<List<BaseVideo>>> get() = videoListLiveDataPrivate

    fun requestAllVideos(){
        viewModelScope.launch {
            videoListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllVideos().collect{
                    videoListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val documentListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val documentListLiveData: LiveData<Resource<List<BaseFile>>> get() = documentListLiveDataPrivate

    fun requestAllDocument(){
        viewModelScope.launch {
            documentListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllDocument().collect{
                    documentListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val imageListLiveDataPrivate = MutableLiveData<Resource<List<BaseImage>>>()
    val imageListLiveData: LiveData<Resource<List<BaseImage>>> get() = imageListLiveDataPrivate

    fun requestAllImages(){
        viewModelScope.launch {
            imageListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllImages().collect{
                    imageListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val audioListLiveDataPrivate = MutableLiveData<Resource<List<BaseAudio>>>()
    val audioListLiveData: LiveData<Resource<List<BaseAudio>>> get() = audioListLiveDataPrivate

    fun requestAllAudio(){
        viewModelScope.launch {
            audioListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllAudio().collect{
                    audioListLiveDataPrivate.value = it
                }
            }
        }
    }
}