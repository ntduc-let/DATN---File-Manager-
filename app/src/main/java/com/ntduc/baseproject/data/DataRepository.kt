package com.ntduc.baseproject.data

import com.ntduc.baseproject.data.dto.base.*
import com.ntduc.baseproject.data.dto.files.Files
import com.ntduc.baseproject.data.remote.RemoteData
import com.ntduc.baseproject.data.dto.frames.DataFrames
import com.ntduc.baseproject.data.dto.login.LoginRequest
import com.ntduc.baseproject.data.dto.login.LoginResponse
import com.ntduc.baseproject.data.dto.recipes.Recipes
import com.ntduc.baseproject.data.local.LocalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


/**
 * Created by TruyenIT
 */

class DataRepository @Inject constructor(private val remoteRepository: RemoteData, private val localRepository: LocalData, private val ioDispatcher: CoroutineContext) :
    DataRepositorySource {

    override suspend fun requestRecipes(): Flow<Resource<Recipes>> {
        return flow {
            emit(remoteRepository.requestRecipes())
        }.flowOn(ioDispatcher)
    }

    override suspend fun doLogin(loginRequest: LoginRequest): Flow<Resource<LoginResponse>> {
        return flow {
            emit(localRepository.doLogin(loginRequest))
        }.flowOn(ioDispatcher)
    }

    override suspend fun addToFavourite(id: String): Flow<Resource<Boolean>> {
        return flow {
            localRepository.getCachedFavourites().let {
                it.data?.toMutableSet()?.let { set ->
                    val isAdded = set.add(id)
                    if (isAdded) {
                        emit(localRepository.cacheFavourites(set))
                    } else {
                        emit(Resource.Success(false))
                    }
                }
                it.errorCode?.let { errorCode ->
                    emit(Resource.DataError<Boolean>(errorCode))
                }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun removeFromFavourite(id: String): Flow<Resource<Boolean>> {
        return flow {
            emit(localRepository.removeFromFavourites(id))
            emit(localRepository.removeFromFavourites(id))
        }.flowOn(ioDispatcher)
    }

    override suspend fun isFavourite(id: String): Flow<Resource<Boolean>> {
        return flow {
            emit(localRepository.isFavourite(id))
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestFrames(): Flow<Resource<DataFrames>> {
        return flow {
            emit(remoteRepository.requestFrames())
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllFiles(types: List<String>): Flow<Resource<Files>> {
        return flow<Resource<Files>> {
            emit(localRepository.requestAllFiles(types))
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllApk(): Flow<Resource<List<BaseFile>>> {
        return flow {
            emit(localRepository.requestAllApk())
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllApp(isSystem: Boolean): Flow<Resource<List<BaseApp>>> {
        return flow {
            emit(localRepository.requestAllApp(isSystem))
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllVideos(): Flow<Resource<List<BaseVideo>>> {
        return flow {
            emit(localRepository.requestAllVideos())
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllDocument(): Flow<Resource<List<BaseFile>>> {
        return flow {
            emit(localRepository.requestAllDocument())
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllImages(): Flow<Resource<List<BaseImage>>> {
        return flow {
            emit(localRepository.requestAllImages())
        }.flowOn(ioDispatcher)
    }

    override suspend fun requestAllAudio(): Flow<Resource<List<BaseAudio>>> {
        return flow {
            emit(localRepository.requestAllAudio())
        }.flowOn(ioDispatcher)
    }
}
