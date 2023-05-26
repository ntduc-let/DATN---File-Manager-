package com.ntduc.baseproject.data

import android.content.Context
import com.ntduc.baseproject.data.dto.base.BaseApk
import com.ntduc.baseproject.data.dto.base.BaseApp
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import com.ntduc.baseproject.data.dto.base.BaseVideo
import com.ntduc.baseproject.data.dto.frames.DataFrames
import com.ntduc.baseproject.data.dto.login.LoginRequest
import com.ntduc.baseproject.data.dto.login.LoginResponse
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.data.dto.recipes.Recipes
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Created by DucNT
 */

interface DataRepositorySource {
    suspend fun requestRecipes(): Flow<Resource<Recipes>>
    suspend fun doLogin(loginRequest: LoginRequest): Flow<Resource<LoginResponse>>
    suspend fun addToFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun removeFromFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun isFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun requestFrames(): Flow<Resource<DataFrames>>
    suspend fun loadApkSafe(context: Context): Flow<Resource<List<File>>>
    suspend fun loadVideoSafe(context: Context): Flow<Resource<List<File>>>
    suspend fun loadDocumentSafe(context: Context): Flow<Resource<List<File>>>
    suspend fun loadImageSafe(context: Context): Flow<Resource<List<File>>>
    suspend fun loadAudioSafe(context: Context): Flow<Resource<List<File>>>
    suspend fun requestAllFolderFile(path: String): Flow<Resource<List<BaseFile>>>
    suspend fun requestAllSearch(key: String): Flow<Resource<List<BaseFile>>>
    suspend fun requestAllRecent(): Flow<Resource<List<BaseFile>>>
    suspend fun requestAllApk(): Flow<Resource<List<BaseApk>>>
    suspend fun requestAllApp(isSystem: Boolean): Flow<Resource<List<BaseApp>>>
    suspend fun requestAllVideos(): Flow<Resource<List<BaseVideo>>>
    suspend fun requestAllDocument(): Flow<Resource<List<BaseFile>>>
    suspend fun requestAllImages(): Flow<Resource<List<BaseImage>>>
    suspend fun requestAllAudio(): Flow<Resource<List<BaseAudio>>>
    suspend fun requestAllPlaylistAudio(): Flow<Resource<List<PlaylistAudioFile>>>
}
