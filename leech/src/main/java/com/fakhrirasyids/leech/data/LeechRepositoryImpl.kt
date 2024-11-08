package com.fakhrirasyids.leech.data

import android.app.NotificationManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.fakhrirasyids.leech.data.local.LocalDataSource
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import com.fakhrirasyids.leech.domain.repositories.LeechRepository
import com.fakhrirasyids.leech.utils.Constants
import com.fakhrirasyids.leech.utils.ConverterUtil
import com.fakhrirasyids.leech.services.workmanager.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Implementation of LeechRepository, providing methods for managing download tasks, observing
 * download states, and handling cancellations. It leverages WorkManager to manage background
 * download tasks and uses a local data source for persistence.
 *
 * @param localDataSource Data source for accessing local database operations.
 * @param workManager WorkManager instance for managing background tasks.
 * @param notificationIcon Icon resource ID for notifications.
 * @param notificationImportance Importance level for notifications.
 */
internal class LeechRepositoryImpl(
    private val localDataSource: LocalDataSource,
    private val workManager: WorkManager,
    private val notificationIcon: Int,
    private val notificationImportance: Int = NotificationManager.IMPORTANCE_DEFAULT,
) : LeechRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initiates a download task by enqueuing a WorkManager job with the specified download entity.
     *
     * @param leechDownloadEntity The download entity containing information about the file to download.
     */
    @JvmSynthetic
    override fun downloadItem(leechDownloadEntity: LeechDownloadEntity) {
        object : DownloadCoordinator<LeechDownloadEntity>() {
            override fun getDownloadItem() = leechDownloadEntity

            override fun insertSavedDownloadItem(data: LeechDownloadEntity) {
                coroutineScope.launch { localDataSource.insertDownloadItem(leechDownloadEntity) }
            }

            override fun searchDownloadItemFromDB(data: LeechDownloadEntity): Flow<LeechDownloadEntity?> {
                return localDataSource.getDownloadItem(data.id)
            }

            override fun createCall(data: LeechDownloadEntity) {
                val inputData = Data.Builder()
                    .putString(Constants.EXTRA_DOWNLOAD_ENTITY, ConverterUtil.convertDownloadEntityToJson(data))
                    .putInt(Constants.EXTRA_NOTIFICATION_ICON, notificationIcon)
                    .putInt(Constants.EXTRA_NOTIFICATION_IMPORTANCE, notificationImportance)
                    .build()

                val constraints = Constraints
                    .Builder()
                    .build()

                val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(inputData)
                    .addTag(TAG_LEECH_DOWNLOAD_WORKER)
                    .setConstraints(constraints)
                    .build()

                workManager.enqueueUniqueWork(
                    data.id.toString(),
                    ExistingWorkPolicy.KEEP,
                    downloadWorkRequest
                )
            }
        }.run()
    }

    /**
     * Observes all download items in the database.
     *
     * @return A Flow emitting the current list of all download entities.
     */
    @JvmSynthetic
    override fun observeAllDownloadItems(): Flow<MutableList<LeechDownloadEntity>> {
        return localDataSource.getAllDownloadItems()
    }

    /**
     * Observes a specific download item by its ID.
     *
     * @param leechId The unique ID of the download item.
     * @return A Flow emitting updates for the specified download entity.
     */
    @JvmSynthetic
    override fun observeDownloadItem(leechId: Int): Flow<LeechDownloadEntity?> {
        return localDataSource.getDownloadItem(leechId)
    }

    /**
     * Cancels all active download tasks and clears all related data in the database.
     */
    @JvmSynthetic
    override fun cancelAllDownloadItems() {
        coroutineScope.launch {
            workManager.cancelAllWork()
            localDataSource.deleteAllDownloadItems()
        }
    }

    /**
     * Cancels a specific download task and removes its data from the database.
     *
     * @param leechId The unique ID of the download item to cancel.
     */
    @JvmSynthetic
    override fun cancelDownloadItem(leechId: Int) {
        coroutineScope.launch {
            workManager.cancelUniqueWork(leechId.toString())
            localDataSource.deleteDownloadItem(leechId)
        }
    }

    companion object {
        const val TAG_LEECH_DOWNLOAD_WORKER = "Leech Download Worker"
    }
}