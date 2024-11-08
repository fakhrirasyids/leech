package com.fakhrirasyids.leech.data.remote.network

internal sealed class DownloadState {
    internal data class Downloading(val progress: Int, val totalLength: Long) : DownloadState()
    internal data class Error(val error: Throwable? = null) : DownloadState()
    internal data class Success(val totalLength: Long) : DownloadState()
}
