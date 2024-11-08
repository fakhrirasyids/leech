package com.fakhrirasyids.leech.utils

import java.io.File

internal object StorageUtils {

    @JvmSynthetic
    fun checkDeleteFile(file: File) {
        if (file.exists() && !file.delete()) {
            throw Exception("Failed to delete ${file.absolutePath}")
        }
    }
}