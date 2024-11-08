package com.fakhrirasyids.leech.data.local

import com.fakhrirasyids.leech.data.local.room.LeechDownloadDao
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity

internal class LocalDataSource(
    private val leechDownloadDao: LeechDownloadDao
) {

    /**
     * Inserts a new download item into the database.
     *
     * @param leechDownloadEntity The entity representing the download item to be inserted.
     */
    suspend fun insertDownloadItem(leechDownloadEntity: LeechDownloadEntity) =
        leechDownloadDao.insertDownloadItem(leechDownloadEntity)

    /**
     * Updates an existing download item in the database.
     *
     * @param leechDownloadEntity The entity representing the download item to be updated.
     */
    suspend fun updateDownloadItem(leechDownloadEntity: LeechDownloadEntity) =
        leechDownloadDao.updateDownloadItem(leechDownloadEntity)

    /**
     * Deletes a specific download item from the database by its ID.
     *
     * @param leechId The unique ID of the download item to be deleted.
     */
    suspend fun deleteDownloadItem(leechId: Int) {
        leechDownloadDao.deleteDownloadItem(leechId)
    }

    /**
     * Retrieves a specific download item from the database by its ID.
     *
     * @param leechId The unique ID of the download item to be retrieved.
     * @return A LiveData or Flow object that represents the download item with the given ID.
     */
    fun getDownloadItem(leechId: Int) =
        leechDownloadDao.getDownloadItem(leechId)

    /**
     * Deletes all download items from the database.
     */
    suspend fun deleteAllDownloadItems() {
        leechDownloadDao.deleteAllDownloadItems()
    }

    /**
     * Retrieves all download items from the database.
     *
     * @return A LiveData or Flow object representing the list of all download items.
     */
    fun getAllDownloadItems() =
        leechDownloadDao.getAllDownloadItems()
}