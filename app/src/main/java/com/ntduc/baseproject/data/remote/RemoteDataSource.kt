package com.ntduc.baseproject.data.remote

import com.ntduc.baseproject.data.Resource
import com.ntduc.baseproject.data.dto.frames.DataFrames
import com.ntduc.baseproject.data.dto.recipes.Recipes

/**
 * Created by DucNT
 */

internal interface RemoteDataSource {
    suspend fun requestRecipes(): Resource<Recipes>
    suspend fun requestFrames(): Resource<DataFrames>
}
