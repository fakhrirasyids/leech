package com.fakhrirasyids.leech

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import com.fakhrirasyids.leech.data.LeechRepositoryImpl
import com.fakhrirasyids.leech.data.local.LocalDataSource
import com.fakhrirasyids.leech.data.local.room.LeechDatabase
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import com.fakhrirasyids.leech.domain.repositories.LeechRepository
import com.fakhrirasyids.leech.utils.ConverterUtil
import kotlinx.coroutines.flow.Flow

/**
 * Leech is a singleton class that provides an interface for managing download requests.
 * It communicates with the LeechRepository to perform download operations, observe download
 * states, and handle cancellation requests.
 */
class Leech private constructor(
    private val leechRepository: LeechRepository
) {

    /**
     * Initiates a download request by creating a LeechDownloadEntity and passing it to the repository.
     *
     * @param url The URL of the file to download.
     * @param path The file path where the downloaded file will be saved.
     * @param fileName Optional name for the file, defaults to a name constructed from the URL.
     * @param headers Optional headers for the download request, defaults to an empty map.
     */
    fun download(
        url: String,
        path: String,
        fileName: String = ConverterUtil.constructDefaultFilename(url),
        headers: HashMap<String, String> = hashMapOf()
    ) {
        val downloadRequest = LeechDownloadEntity(
            url = url,
            filePath = path,
            fileName = fileName,
            headersJsonString = ConverterUtil.hashMapToJson(headers),
        )

        leechRepository.downloadItem(downloadRequest)
    }

    /**
     * Observes all download items and returns a Flow of a mutable list of LeechDownloadEntity objects.
     * This allows clients to monitor the state of all downloads in real time.
     *
     * @return Flow that emits a list of all current download entities.
     */
    fun observeAllDownloadItems(): Flow<MutableList<LeechDownloadEntity>> =
        leechRepository.observeAllDownloadItems()

    /**
     * Observes a specific download item by its unique ID and returns a Flow of the corresponding
     * LeechDownloadEntity object. This is useful for tracking a single download's state.
     *
     * @param leechId The unique ID of the download item to observe.
     * @return Flow that emits updates for the specified download entity.
     */
    fun observeDownloadItem(leechId: Int): Flow<LeechDownloadEntity?> =
        leechRepository.observeDownloadItem(leechId)

    /**
     * Cancels all active download items by delegating the operation to the repository.
     */
    fun cancelAllDownloadItems() {
        leechRepository.cancelAllDownloadItems()
    }

    /**
     * Cancels a specific download item by its unique ID by delegating the operation to the repository.
     *
     * @param leechId The unique ID of the download item to cancel.
     */
    fun cancelDownloadItem(leechId: Int) {
        leechRepository.cancelDownloadItem(leechId)
    }

    companion object {
        @Volatile
        private var INSTANCE: Leech? = null

        /**
         * Retrieves the singleton instance of Leech, creating it if necessary. Ensures thread-safe
         * initialization using double-checked locking.
         *
         * @param context The application context required for accessing database and WorkManager.
         * @param notificationIcon The icon resource ID for download notifications.
         * @param notificationImportance The importance level of the notifications, defaults to IMPORTANCE_DEFAULT.
         * @return The singleton instance of Leech.
         */
        fun getInstance(
            context: Context,
            notificationIcon: Int,
            notificationImportance: Int = NotificationManager.IMPORTANCE_DEFAULT
        ): Leech = INSTANCE ?: synchronized(this) {
            Leech(
                LeechRepositoryImpl(
                    localDataSource = LocalDataSource(
                        LeechDatabase.getInstance(context).leechDownloadDao()
                    ),
                    workManager = WorkManager.getInstance(context),
                    notificationIcon = notificationIcon,
                    notificationImportance = notificationImportance
                )
            )
                .also { INSTANCE = it }
        }
    }
}

