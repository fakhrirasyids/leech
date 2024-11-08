package com.fakhrirasyids.leech.services.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fakhrirasyids.leech.data.local.LocalDataSource
import com.fakhrirasyids.leech.data.local.room.LeechDatabase
import com.fakhrirasyids.leech.data.remote.RemoteDataSource
import com.fakhrirasyids.leech.data.remote.network.DownloadState
import com.fakhrirasyids.leech.data.remote.network.LeechApiConfig
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import com.fakhrirasyids.leech.services.notifications.LeechNotificationManager
import com.fakhrirasyids.leech.utils.Constants
import com.fakhrirasyids.leech.utils.ConverterUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

/**
 * DownloadWorker is a CoroutineWorker responsible for handling background download tasks.
 * It manages the download process, updates progress notifications, and handles errors.
 */
internal class DownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val localDataSource by lazy {
        LocalDataSource(
            LeechDatabase.getInstance(context).leechDownloadDao()
        )
    }
    private val remoteDataSource by lazy { RemoteDataSource(LeechApiConfig.getApiService()) }
    private lateinit var leechNotificationManager: LeechNotificationManager

    /**
     * Main method that the WorkManager calls to perform the background work.
     * This method initializes the download task and manages its execution.
     *
     * @return Result.success() if download completes successfully,
     *         Result.failure() if there is an error.
     */
    override suspend fun doWork(): Result {
        val downloadEntity = ConverterUtil.jsonToDownloadEntity(
            inputData.getString(Constants.EXTRA_DOWNLOAD_ENTITY) ?: return Result.failure()
        )

        leechNotificationManager = LeechNotificationManager(
            context = context,
            notificationIcon = inputData.getInt(Constants.EXTRA_NOTIFICATION_ICON, -1),
            notificationImportance = inputData.getInt(Constants.EXTRA_NOTIFICATION_IMPORTANCE, -1),
            leechFileName = downloadEntity.fileName,
            leechId = downloadEntity.id
        )

        return try {
            executeDownload(downloadEntity)
            Result.success()
        } catch (e: Exception) {
            handleDownloadError(downloadEntity)
            Result.failure()
        }
    }

    /**
     * Executes the download process for the specified download entity.
     * Manages the download state, updates notifications, and saves progress in the database.
     *
     * @param downloadEntity The download request containing file URL, path, and other details.
     */
    private suspend fun executeDownload(downloadEntity: LeechDownloadEntity) {
        val fileDestination = File(downloadEntity.filePath, downloadEntity.fileName)
        val headers = ConverterUtil.jsonToHashMap(downloadEntity.headersJsonString)

        val currentDownload = localDataSource.getDownloadItem(downloadEntity.id).firstOrNull()
            ?: return

        if (currentDownload.downloadStatus == Constants.FileDownloadStatus.DOWNLOAD_COMPLETE.name) {
            leechNotificationManager.postSuccessfulDownloadNotification(currentDownload.fileByteSize)
            return
        }

        coroutineScope {
            setForeground(leechNotificationManager.postUpdateNotification())
            remoteDataSource.getDownloadedFile(fileDestination, downloadEntity.url, headers)
                .collect { downloadState ->
                    handleDownloadState(downloadState, downloadEntity)
                }
        }
    }

    /**
     * Handles different states of the download (e.g., Downloading, Success, Error) and
     * updates the download status in the database and notifications accordingly.
     *
     * @param downloadState The current state of the download (downloading, success, error).
     * @param downloadEntity The download entity being processed.
     */
    private suspend fun handleDownloadState(
        downloadState: DownloadState,
        downloadEntity: LeechDownloadEntity
    ) {
        when (downloadState) {
            is DownloadState.Downloading -> {
                val updatedEntity = downloadEntity.copy(
                    fileByteSize = downloadState.totalLength,
                    downloadStatus = Constants.FileDownloadStatus.DOWNLOAD_LOADING.name
                )
                localDataSource.updateDownloadItem(updatedEntity)
                setForeground(
                    leechNotificationManager.postUpdateNotification(
                        downloadState.progress,
                        true
                    )
                )
            }

            is DownloadState.Success -> {
                val completedEntity = downloadEntity.copy(
                    fileByteSize = downloadState.totalLength,
                    downloadStatus = Constants.FileDownloadStatus.DOWNLOAD_COMPLETE.name
                )
                localDataSource.updateDownloadItem(completedEntity)
                leechNotificationManager.postSuccessfulDownloadNotification(downloadState.totalLength)
            }

            is DownloadState.Error -> {
                handleDownloadError(downloadEntity)
                throw Exception(downloadState.error)
            }
        }
    }

    /**
     * Handles errors encountered during the download process by updating the download entity
     * with a failed status, updating the database, and posting a failed notification.
     *
     * @param downloadEntity The download entity that failed.
     */
    private suspend fun handleDownloadError(downloadEntity: LeechDownloadEntity) {
        val failedEntity = downloadEntity.copy(
            downloadStatus = Constants.FileDownloadStatus.DOWNLOAD_FAILED.name
        )
        localDataSource.updateDownloadItem(failedEntity)
        leechNotificationManager.postFailedDownloadNotification()
    }
}
