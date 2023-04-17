package com.ntduc.baseproject.data.local

import android.content.Context
import android.content.SharedPreferences
import com.ntduc.baseproject.constant.*
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.*
import com.ntduc.baseproject.data.dto.login.LoginRequest
import com.ntduc.baseproject.data.dto.login.LoginResponse
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.data.error.PASS_WORD_ERROR
import com.ntduc.baseproject.utils.file.getAudios
import com.ntduc.baseproject.utils.file.getFiles
import com.ntduc.baseproject.utils.file.getImages
import com.ntduc.baseproject.utils.file.getVideos
import com.ntduc.baseproject.utils.getApks
import com.ntduc.baseproject.utils.getApps
import com.orhanobut.hawk.Hawk
import javax.inject.Inject

/**
 * Created by TruyenIT
 */

class LocalData @Inject constructor(val context: Context) {

    fun doLogin(loginRequest: LoginRequest): Resource<LoginResponse> {
        if (loginRequest == LoginRequest("ahmed@ahmed.ahmed", "ahmed")) {
            return Resource.Success(
                LoginResponse(
                    "123", "Ahmed", "Mahmoud",
                    "FrunkfurterAlle", "77", "12000", "Berlin",
                    "Germany", "ahmed@ahmed.ahmed"
                )
            )
        }
        return Resource.DataError(PASS_WORD_ERROR)
    }

    fun getCachedFavourites(): Resource<Set<String>> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        return Resource.Success(sharedPref.getStringSet(FAVOURITES_KEY, setOf()) ?: setOf())
    }

    fun isFavourite(id: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val cache = sharedPref.getStringSet(FAVOURITES_KEY, setOf<String>()) ?: setOf()
        return Resource.Success(cache.contains(id))
    }

    fun cacheFavourites(ids: Set<String>): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putStringSet(FAVOURITES_KEY, ids)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun removeFromFavourites(id: String): Resource<Boolean> {
        val sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_FILE_NAME, 0)
        var set = sharedPref.getStringSet(FAVOURITES_KEY, mutableSetOf<String>())?.toMutableSet() ?: mutableSetOf()
        if (set.contains(id)) {
            set.remove(id)
        }
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.clear()
        editor.apply()
        editor.commit()
        editor.putStringSet(FAVOURITES_KEY, set)
        editor.apply()
        val isSuccess = editor.commit()
        return Resource.Success(isSuccess)
    }

    fun requestAllRecent(): Resource<List<BaseFile>> {
        val result = arrayListOf<BaseFile>()

        val listRecent = Hawk.get(RECENT_FILE, listOf<String>())
        val baseFileList = context.getFiles()
        listRecent.forEach {path ->
            run forE@{
                baseFileList.forEach {
                    if (it.data == path) {
                        result.add(it)
                        return@forE
                    }
                }
            }
        }

        return Resource.Success(result)
    }

    fun requestAllApk(): Resource<List<BaseApk>> {
        val baseApkList = context.getApks()
        return Resource.Success(baseApkList)
    }

    fun requestAllApp(isSystem: Boolean = false): Resource<List<BaseApp>> {
        val baseAppList = context.getApps(isSystem)
        return Resource.Success(baseAppList)
    }

    fun requestAllVideos(): Resource<List<BaseVideo>> {
        val baseVideoList = context.getVideos()
        return Resource.Success(baseVideoList)
    }

    fun requestAllDocument(): Resource<List<BaseFile>> {
        val baseFileList = context.getFiles(types = listOf(*FileType.DOCUMENT))
        return Resource.Success(baseFileList)
    }

    fun requestAllImages(): Resource<List<BaseImage>> {
        val baseImageList = context.getImages()
        return Resource.Success(baseImageList)
    }

    fun requestAllAudio(): Resource<List<BaseAudio>> {
        val baseAudioList = context.getAudios()
        return Resource.Success(baseAudioList)
    }

    fun requestAllPlaylistAudio(): Resource<List<PlaylistAudioFile>> {
        val list: List<PlaylistAudioFile> = Hawk.get(PLAYLIST_AUDIO, listOf())
        return Resource.Success(list)
    }
}

