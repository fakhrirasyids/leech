package com.fakhrirasyids.leech.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fakhrirasyids.leech.utils.Constants
import com.fakhrirasyids.leech.utils.ConverterUtil
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "leech_downloads")
data class LeechDownloadEntity(
    val fileName: String = "",
    val filePath: String = "",
    val url: String = "",
    var headersJsonString: String = "",
    var fileByteSize: Long = 0,
    var queuedTime: Long = System.currentTimeMillis(),
    var downloadStatus: String = Constants.FileDownloadStatus.DOWNLOAD_QUEUED.name,
    @PrimaryKey val id: Int = ConverterUtil.constructUniqueId(url, filePath, fileName)
)