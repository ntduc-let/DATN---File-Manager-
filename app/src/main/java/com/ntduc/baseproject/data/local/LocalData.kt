package com.ntduc.baseproject.data.local

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Base64
import com.ntduc.baseproject.R
import com.ntduc.baseproject.constant.FAVOURITES_KEY
import com.ntduc.baseproject.constant.FileType
import com.ntduc.baseproject.constant.PLAYLIST_AUDIO
import com.ntduc.baseproject.constant.RECENT_FILE
import com.ntduc.baseproject.constant.SHARED_PREFERENCES_FILE_NAME
import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.data.dto.login.LoginRequest
import com.ntduc.baseproject.data.dto.login.LoginResponse
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.data.error.PASS_WORD_ERROR
import com.ntduc.baseproject.utils.currentMillis
import com.ntduc.baseproject.utils.file.delete
import com.ntduc.baseproject.utils.file.getAudios
import com.ntduc.baseproject.utils.file.getFiles
import com.ntduc.baseproject.utils.file.getImages
import com.ntduc.baseproject.utils.file.getVideos
import com.ntduc.baseproject.utils.file.mimeType
import com.ntduc.baseproject.utils.file.readToString
import com.ntduc.baseproject.utils.file.size
import com.ntduc.baseproject.utils.getApks
import com.ntduc.baseproject.utils.getApps
import com.ntduc.baseproject.utils.security.FileEncryption
import com.orhanobut.hawk.Hawk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Created by DucNT
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

    fun loadApkSafe(context: Context): Resource<List<File>> {
        val result = ArrayList<File>()
        try {
            val apkFolder = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/apk")
            if (!apkFolder.exists()) {
                apkFolder.mkdirs()
            }
            val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
            val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
            val pass = String(bytes, Charsets.UTF_8)
            val apkCacheFolder = File(context.filesDir.path + "/.SafeFolder/apk")
            if (!apkCacheFolder.exists()) {
                apkCacheFolder.mkdirs()
            } else {
                apkCacheFolder.listFiles()?.forEach {
                    it.delete(context)
                }
            }
            apkFolder.listFiles()?.forEach {
                val outputFile = File(apkCacheFolder.path + "/${it.name}")
                FileEncryption.decryptToFile(
                    "$pass$pass$pass$pass",
                    "abcdefghptreqwrf",
                    FileInputStream(it),
                    FileOutputStream(outputFile)
                )
                result.add(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Resource.Success(result)
    }

    fun loadVideoSafe(context: Context): Resource<List<File>> {
        val result = ArrayList<File>()
        try {
            val videoFolder = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/video")
            if (!videoFolder.exists()) {
                videoFolder.mkdirs()
            }
            val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
            val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
            val pass = String(bytes, Charsets.UTF_8)
            val videoCacheFolder = File(context.filesDir.path + "/.SafeFolder/video")
            if (!videoCacheFolder.exists()) {
                videoCacheFolder.mkdirs()
            } else {
                videoCacheFolder.listFiles()?.forEach {
                    it.delete(context)
                }
            }
            videoFolder.listFiles()?.forEach {
                val outputFile = File(videoCacheFolder.path + "/${it.name}")
                FileEncryption.decryptToFile(
                    "$pass$pass$pass$pass",
                    "abcdefghptreqwrf",
                    FileInputStream(it),
                    FileOutputStream(outputFile)
                )
                result.add(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Resource.Success(result)
    }

    fun loadDocumentSafe(context: Context): Resource<List<File>> {
        val result = ArrayList<File>()
        try {
            val documentFolder = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/document")
            if (!documentFolder.exists()) {
                documentFolder.mkdirs()
            }
            val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
            val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
            val pass = String(bytes, Charsets.UTF_8)
            val documentCacheFolder = File(context.filesDir.path + "/.SafeFolder/document")
            if (!documentCacheFolder.exists()) {
                documentCacheFolder.mkdirs()
            } else {
                documentCacheFolder.listFiles()?.forEach {
                    it.delete(context)
                }
            }
            documentFolder.listFiles()?.forEach {
                val outputFile = File(documentCacheFolder.path + "/${it.name}")
                FileEncryption.decryptToFile(
                    "$pass$pass$pass$pass",
                    "abcdefghptreqwrf",
                    FileInputStream(it),
                    FileOutputStream(outputFile)
                )
                result.add(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Resource.Success(result)
    }

    fun loadImageSafe(context: Context): Resource<List<File>> {
        val result = ArrayList<File>()
        try {
            val imageFolder = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/image")
            if (!imageFolder.exists()) {
                imageFolder.mkdirs()
            }
            val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
            val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
            val pass = String(bytes, Charsets.UTF_8)
            val imageCacheFolder = File(context.filesDir.path + "/.SafeFolder/image")
            if (!imageCacheFolder.exists()) {
                imageCacheFolder.mkdirs()
            } else {
                imageCacheFolder.listFiles()?.forEach {
                    it.delete(context)
                }
            }
            imageFolder.listFiles()?.forEach {
                val outputFile = File(imageCacheFolder.path + "/${it.name}")
                FileEncryption.decryptToFile(
                    "$pass$pass$pass$pass",
                    "abcdefghptreqwrf",
                    FileInputStream(it),
                    FileOutputStream(outputFile)
                )
                result.add(outputFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Resource.Success(result)
    }

    fun loadAudioSafe(context: Context): Resource<List<File>> {
        val result = ArrayList<File>()
        try {
            val audioFolder = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/audio")
            if (!audioFolder.exists()) {
                audioFolder.mkdirs()
            }
            val passEncrypted = File(Environment.getExternalStorageDirectory().path + "/.${context.getString(R.string.app_name)}/.SafeFolder/pass.txt").readToString()
            val bytes = Base64.decode(passEncrypted, Base64.DEFAULT)
            val pass = String(bytes, Charsets.UTF_8)
            val audioCacheFolder = File(context.filesDir.path + "/.SafeFolder/audio")
            if (!audioCacheFolder.exists()) {
                audioCacheFolder.mkdirs()
            }else{
                audioCacheFolder.listFiles()?.forEach {
                    it.delete(context)
                }
            }
            audioFolder.listFiles()?.forEach {
                val outputFile = File(audioCacheFolder.path + "/${it.name}")
                FileEncryption.decryptToFile(
                    "$pass$pass$pass$pass",
                    "abcdefghptreqwrf",
                    FileInputStream(it),
                    FileOutputStream(outputFile)
                )
                result.add(outputFile)
            }
        }catch (e: Exception){
            e.printStackTrace()
        }

        return Resource.Success(result)
    }

    fun requestAllFolderFile(path: String): Resource<List<BaseFile>> {
        val files = File(path)
        val result = ArrayList<BaseFile>()
        files.listFiles()?.forEach {
            if (!it.isHidden) {
                val folderFile = BaseFile(
                    id = currentMillis,
                    displayName = it.name,
                    title = it.nameWithoutExtension,
                    mimeType = it.mimeType(),
                    size = it.size(),
                    dateAdded = it.lastModified(),
                    dateModified = it.lastModified(),
                    data = it.path
                )
                result.add(folderFile)
            }
        }
        return Resource.Success(result)
    }

    fun requestAllSearch(key: String): Resource<List<BaseFile>> {
        val baseFileList = context.getFiles()
        val result = baseFileList.filter { it.displayName!!.contains(key, true) }
        return Resource.Success(result)
    }

    fun requestAllRecent(): Resource<List<BaseFile>> {
        val result = arrayListOf<BaseFile>()

        val listRecent = Hawk.get(RECENT_FILE, listOf<String>())
        val baseFileList = context.getFiles()
        listRecent.forEach { path ->
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

