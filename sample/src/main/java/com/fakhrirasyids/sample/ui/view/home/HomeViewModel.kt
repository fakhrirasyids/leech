package com.fakhrirasyids.sample.ui.view.home

import androidx.lifecycle.ViewModel
import com.fakhrirasyids.leech.Leech
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import kotlinx.coroutines.flow.Flow

class HomeViewModel(private val leech: Leech) : ViewModel() {
    fun downloadItem(
        url: String,
        path: String,
        fileName: String,
        headers: HashMap<String, String> = hashMapOf()
    ) {
        leech.download(url, path, fileName, headers)
    }

    fun observeAllDownloadItems(): Flow<MutableList<LeechDownloadEntity>> =
        leech.observeAllDownloadItems()

    fun cancelDownloadItem(downloadId: Int) {
        leech.cancelDownloadItem(downloadId)
    }
}