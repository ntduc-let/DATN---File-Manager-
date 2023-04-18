package com.ntduc.baseproject.data.dto.root

import android.os.Parcelable
import com.ntduc.baseproject.data.dto.base.BaseFile
import kotlinx.parcelize.Parcelize

@Parcelize
open class FolderFile(
    override var id: Long? = null,
    override var title: String? = null,
    override var displayName: String? = null,
    override var mimeType: String? = null,
    override var size: Long? = null,
    override var dateAdded: Long? = null,
    override var dateModified: Long? = null,
    override var data: String? = null,
    open var isSelected: Boolean = false
) : BaseFile(id, title, displayName, mimeType, size, dateAdded, dateModified, data), Parcelable