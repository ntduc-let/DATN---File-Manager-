package com.ntduc.baseproject.data.dto.folder

import android.os.Parcelable
import com.ntduc.baseproject.data.dto.base.BaseFile
import com.ntduc.baseproject.data.dto.base.BaseImage
import kotlinx.parcelize.Parcelize

@Parcelize
open class FolderImageFile(
    var baseFile: BaseFile? = null,
    var listFile: ArrayList<BaseImage> = arrayListOf()
) : Parcelable