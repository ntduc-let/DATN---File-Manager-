package com.ntduc.baseproject.ui.component.main

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ntduc.baseproject.data.DataRepositorySource
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.ui.base.BaseViewModel
import com.ntduc.baseproject.utils.wrapEspressoIdlingResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: DataRepositorySource
) : BaseViewModel() {

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val audioSafeListLiveDataPrivate = MutableLiveData<Resource<List<File>>>()
    val audioSafeListLiveData: LiveData<Resource<List<File>>> get() = audioSafeListLiveDataPrivate

    fun loadAudioSafe(context: Context) {
        viewModelScope.launch {
            audioSafeListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.loadAudioSafe(context).collect {
                    audioSafeListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val imageSafeListLiveDataPrivate = MutableLiveData<Resource<List<File>>>()
    val imageSafeListLiveData: LiveData<Resource<List<File>>> get() = imageSafeListLiveDataPrivate

    fun loadImageSafe(context: Context) {
        viewModelScope.launch {
            imageSafeListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.loadImageSafe(context).collect {
                    imageSafeListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val documentSafeListLiveDataPrivate = MutableLiveData<Resource<List<File>>>()
    val documentSafeListLiveData: LiveData<Resource<List<File>>> get() = documentSafeListLiveDataPrivate

    fun loadDocumentSafe(context: Context) {
        viewModelScope.launch {
            documentSafeListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.loadDocumentSafe(context).collect {
                    documentSafeListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val videoSafeListLiveDataPrivate = MutableLiveData<Resource<List<File>>>()
    val videoSafeListLiveData: LiveData<Resource<List<File>>> get() = videoSafeListLiveDataPrivate

    fun loadVideoSafe(context: Context) {
        viewModelScope.launch {
            videoSafeListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.loadVideoSafe(context).collect {
                    videoSafeListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val apkSafeListLiveDataPrivate = MutableLiveData<Resource<List<File>>>()
    val apkSafeListLiveData: LiveData<Resource<List<File>>> get() = apkSafeListLiveDataPrivate

    fun loadApkSafe(context: Context) {
        viewModelScope.launch {
            apkSafeListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.loadApkSafe(context).collect {
                    apkSafeListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val folderFileListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val folderFileListLiveData: LiveData<Resource<List<BaseFile>>> get() = folderFileListLiveDataPrivate

    fun requestAllFolderFile(path: String) {
        viewModelScope.launch {
            folderFileListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllFolderFile(path).collect {
                    folderFileListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val searchListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val searchListLiveData: LiveData<Resource<List<BaseFile>>> get() = searchListLiveDataPrivate

    fun requestAllSearch(key: String = "") {
        viewModelScope.launch {
            searchListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllSearch(key).collect {
                    searchListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val recentListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val recentListLiveData: LiveData<Resource<List<BaseFile>>> get() = recentListLiveDataPrivate

    fun requestAllRecent() {
        viewModelScope.launch {
            recentListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllRecent().collect {
                    recentListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val apkListLiveDataPrivate = MutableLiveData<Resource<List<BaseApk>>>()
    val apkListLiveData: LiveData<Resource<List<BaseApk>>> get() = apkListLiveDataPrivate

    fun requestAllApk() {
        viewModelScope.launch {
            apkListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllApk().collect {
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

    fun requestAllApp() {
        viewModelScope.launch {
            appWithoutSystemListLiveDataPrivate.value = Resource.Loading()
            appListLiveDataPrivate.value = Resource.Loading()

            wrapEspressoIdlingResource {
                repository.requestAllApp(false).collect {
                    appWithoutSystemListLiveDataPrivate.value = it
                }
                repository.requestAllApp(true).collect {
                    appListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val videoListLiveDataPrivate = MutableLiveData<Resource<List<BaseVideo>>>()
    val videoListLiveData: LiveData<Resource<List<BaseVideo>>> get() = videoListLiveDataPrivate

    fun requestAllVideos() {
        viewModelScope.launch {
            videoListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllVideos().collect {
                    videoListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val documentListLiveDataPrivate = MutableLiveData<Resource<List<BaseFile>>>()
    val documentListLiveData: LiveData<Resource<List<BaseFile>>> get() = documentListLiveDataPrivate

    fun requestAllDocument() {
        viewModelScope.launch {
            documentListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllDocument().collect {
                    documentListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val imageListLiveDataPrivate = MutableLiveData<Resource<List<BaseImage>>>()
    val imageListLiveData: LiveData<Resource<List<BaseImage>>> get() = imageListLiveDataPrivate

    fun requestAllImages() {
        viewModelScope.launch {
            imageListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllImages().collect {
                    imageListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val audioListLiveDataPrivate = MutableLiveData<Resource<List<BaseAudio>>>()
    val audioListLiveData: LiveData<Resource<List<BaseAudio>>> get() = audioListLiveDataPrivate

    fun requestAllAudio() {
        viewModelScope.launch {
            audioListLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllAudio().collect {
                    audioListLiveDataPrivate.value = it
                }
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val playlistAudioLiveDataPrivate = MutableLiveData<Resource<List<PlaylistAudioFile>>>()
    val playlistAudioLiveData: LiveData<Resource<List<PlaylistAudioFile>>> get() = playlistAudioLiveDataPrivate

    fun requestAllPlaylistAudio() {
        viewModelScope.launch {
            playlistAudioLiveDataPrivate.value = Resource.Loading()
            wrapEspressoIdlingResource {
                repository.requestAllPlaylistAudio().collect {
                    playlistAudioLiveDataPrivate.value = it
                }
            }
        }
    }
}