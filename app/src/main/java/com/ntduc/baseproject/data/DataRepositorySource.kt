package com.ntduc.baseproject.data

import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.frames.DataFrames
import kotlinx.coroutines.flow.Flow

/**
 * Created by TruyenIT
 */

interface DataRepositorySource {
    suspend fun requestFrames(): Flow<Resource<DataFrames>>
    suspend fun requestAllFiles(types: List<String>): Flow<Resource<List<BaseFile>>>
}
