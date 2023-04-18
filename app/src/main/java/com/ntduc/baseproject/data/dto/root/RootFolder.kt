package com.ntduc.baseproject.data.dto.root

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class RootFolder(
    var name: String = "",
    var path: String = ""
) : Parcelable