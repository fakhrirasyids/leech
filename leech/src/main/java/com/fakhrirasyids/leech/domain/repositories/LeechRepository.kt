package com.fakhrirasyids.leech.domain.repositories

import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import kotlinx.coroutines.flow.Flow

internal interface LeechRepository {
    fun downloadItem(leechDownloadEntity: LeechDownloadEntity)

    fun observeAllDownloadItems(): Flow<MutableList<LeechDownloadEntity>>
    fun observeDownloadItem(leechId: Int): Flow<LeechDownloadEntity?>

    fun cancelAllDownloadItems()
    fun cancelDownloadItem(leechId: Int)
}