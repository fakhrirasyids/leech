package com.fakhrirasyids.leech.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal abstract class DownloadCoordinator<ResultType> {
    protected abstract fun insertSavedDownloadItem(data: ResultType)

    protected abstract fun getDownloadItem(): ResultType

    protected abstract fun searchDownloadItemFromDB(data: ResultType): Flow<ResultType?>

    protected abstract fun createCall(data: ResultType)

    fun run() {
        val entity = getDownloadItem()
        var entityFromDb = runBlocking { searchDownloadItemFromDB(entity).first() }

        if (entityFromDb == null) {
            insertSavedDownloadItem(entity)
            entityFromDb = entity
        }

        createCall(entityFromDb ?: entity)
    }
}