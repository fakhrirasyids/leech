package com.fakhrirasyids.sample.utils

import android.os.Environment

object Constants {
    fun getDownloadPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    }
}