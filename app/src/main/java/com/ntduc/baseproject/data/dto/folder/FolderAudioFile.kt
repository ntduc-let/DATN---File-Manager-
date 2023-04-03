package com.ntduc.baseproject.data.dto.folder

import android.os.Parcelable
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import kotlinx.parcelize.Parcelize

@Parcelize
open class FolderAudioFile(
    var baseFile: BaseFile? = null,
    var listFile: ArrayList<BaseAudio> = arrayListOf()
) : Parcelable