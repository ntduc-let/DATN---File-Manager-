package com.ntduc.baseproject.constant

import com.ntduc.baseproject.R
import java.io.File

object FileTypeExtension {
    const val IMAGE = "image"
    const val VIDEO = "video"
    const val AUDIO = "audio"
    const val PDF = "pdf"
    const val TXT = "txt"
    const val DOC = "doc"
    const val XLS = "xls"
    const val PPT = "ppt"
    const val COMPRESSED = "compressed"
    const val APK = "apk"
    const val OTHER = "other"

    fun getTypeFile(path: String): String {
        val extension = File(path).extension
        return when {
            FileType.IMAGE.contains(extension) -> IMAGE
            FileType.VIDEO.contains(extension) -> VIDEO
            FileType.AUDIO.contains(extension) -> AUDIO
            FileType.DOCUMENT_PDF.contains(extension) -> PDF
            FileType.DOCUMENT_TXT.contains(extension) -> TXT
            FileType.DOCUMENT_DOC.contains(extension) -> DOC
            FileType.DOCUMENT_XLS.contains(extension) -> XLS
            FileType.DOCUMENT_PPT.contains(extension) -> PPT
            FileType.APK.contains(extension) -> APK
            else -> OTHER
        }
    }

    fun getIconFile(path: String): Int {
        val file = File(path)
        if (file.isDirectory) return R.drawable.ic_folder_24dp

        val extension = file.extension
        return when {
            FileType.IMAGE.contains(extension) -> R.drawable.ic_image_32dp
            FileType.VIDEO.contains(extension) -> R.drawable.ic_video_32dp
            FileType.AUDIO.contains(extension) -> R.drawable.ic_music_32dp
            FileType.DOCUMENT.contains(extension) -> R.drawable.ic_document_32dp
            FileType.APK.contains(extension) -> R.drawable.ic_app_32dp
            else -> R.drawable.ic_file_24dp
        }
    }

    fun getIconDocument(path: String): Int {
        val file = File(path)
        if (file.isDirectory) return R.drawable.ic_folder_24dp

        val extension = file.extension
        return when {
            FileType.DOCUMENT_PDF.contains(extension) -> R.drawable.ic_pdf
            FileType.DOCUMENT_TXT.contains(extension) -> R.drawable.ic_txt
            FileType.DOCUMENT_DOC.contains(extension) -> R.drawable.ic_doc
            FileType.DOCUMENT_XLS.contains(extension) -> R.drawable.ic_xls
            FileType.DOCUMENT_PPT.contains(extension) -> R.drawable.ic_ppt
            else -> R.drawable.ic_file_24dp
        }
    }
}