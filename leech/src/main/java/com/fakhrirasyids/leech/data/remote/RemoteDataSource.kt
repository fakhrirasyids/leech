package com.fakhrirasyids.leech.data.remote

import com.fakhrirasyids.leech.data.remote.network.DownloadState
import com.fakhrirasyids.leech.data.remote.network.LeechApiService
import com.fakhrirasyids.leech.utils.StorageUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.IOException

internal class RemoteDataSource(
    private val leechApiService: LeechApiService
) {

    /**
     * Downloads a file from a specified URL and saves it to the provided file destination.
     * Emits the download progress through a Flow of DownloadState.
     *
     * @param fileDestination The File object where the downloaded data will be saved.
     * @param urlToFile The URL of the file to be downloaded.
     * @param headers Optional headers to be added to the download request.
     * @return A Flow emitting DownloadState, which includes downloading progress, success, or error.
     */
    @JvmSynthetic
    suspend fun getDownloadedFile(
        fileDestination: File,
        urlToFile: String,
        headers: MutableMap<String, String> = mutableMapOf(),
    ): Flow<DownloadState> {
        return flow {
            emit(DownloadState.Downloading(0, 0))

            try {
                val downloadResponse = leechApiService.getDownload(urlToFile, headers)
                val totalBytes = downloadResponse.contentLength()

                if (totalBytes <= 0) {
                    throw IOException("Invalid content length")
                }

                downloadResponse.byteStream().use { inputStream ->
                    fileDestination.outputStream().use { outputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var progressBytes = 0L
                        var bytesRead = inputStream.read(buffer)
                        var lastEmitTime = System.currentTimeMillis()

                        while (bytesRead >= 0) {
                            outputStream.write(buffer, 0, bytesRead)
                            progressBytes += bytesRead
                            bytesRead = inputStream.read(buffer)

                            val currentTime = System.currentTimeMillis()

                            val currentProgress = ((progressBytes * 100) / totalBytes).toInt()

                            if (currentTime - lastEmitTime >= TIME_THROTTLING_TRIGGER) {
                                emit(DownloadState.Downloading(currentProgress, progressBytes))
                                lastEmitTime = currentTime
                            }
                        }
                    }
                }

                emit(DownloadState.Success(totalBytes))
            } catch (e: Exception) {
                StorageUtils.checkDeleteFile(fileDestination)
                emit(DownloadState.Error(e))
            }
        }
    }

    companion object {
        private const val TIME_THROTTLING_TRIGGER = 1000
    }
}