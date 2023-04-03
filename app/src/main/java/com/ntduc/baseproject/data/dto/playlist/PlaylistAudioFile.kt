package com.ntduc.baseproject.data.dto.playlist

import android.os.Parcelable
import com.ntduc.baseproject.data.dto.base.BaseAudio
import com.ntduc.baseproject.data.dto.base.BaseFile
import kotlinx.parcelize.Parcelize

@Parcelize
open class PlaylistAudioFile(
    var id: Long = 0L,
    var name: String = "",
    var listFile: ArrayList<BaseAudio> = arrayListOf()
) : Parcelable