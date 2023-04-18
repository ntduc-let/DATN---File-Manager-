package com.ntduc.baseproject.data

import com.ntduc.baseproject.data.dto.base.*
import com.ntduc.baseproject.data.dto.frames.DataFrames
import com.ntduc.baseproject.data.dto.login.LoginRequest
import com.ntduc.baseproject.data.dto.login.LoginResponse
import com.ntduc.baseproject.data.dto.playlist.PlaylistAudioFile
import com.ntduc.baseproject.data.dto.recipes.Recipes
import com.ntduc.baseproject.data.dto.root.FolderFile
import com.ntduc.baseproject.data.dto.root.RootFolder
import kotlinx.coroutines.flow.Flow

/**
 * Created by TruyenIT
 */

interface DataRepositorySource {
    suspend fun requestRecipes(): Flow<Resource<Recipes>>
    suspend fun doLogin(loginRequest: LoginRequest): Flow<Resource<LoginResponse>>
    suspend fun addToFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun removeFromFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun isFavourite(id: String): Flow<Resource<Boolean>>
    suspend fun requestFrames(): Flow<Resource<DataFrames>>
    suspend fun requestAllFolderFile(path: String): Flow<Resource<List<FolderFile>>>
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
