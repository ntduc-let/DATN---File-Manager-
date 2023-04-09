package com.ntduc.baseproject.data.dto.folder

import android.os.Parcelable
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseVideo
import kotlinx.parcelize.Parcelize

@Parcelize
open class FolderVideoFile(
    var baseFile: BaseFile? = null,
    var listFile: ArrayList<BaseVideo> = arrayListOf()
) : Parcelable