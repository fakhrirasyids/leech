package com.fakhrirasyids.leech.utils

import android.webkit.URLUtil
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID

internal object ConverterUtil {

    @JvmSynthetic
    fun convertDownloadEntityToJson(downloadEntity: LeechDownloadEntity) =
        Json.encodeToString(downloadEntity)

    @JvmSynthetic
    fun jsonToDownloadEntity(jsonStr: String) = Json.decodeFromString<LeechDownloadEntity>(jsonStr)

    @JvmSynthetic
    fun hashMapToJson(headers: HashMap<String, String>) =
        if (headers.isEmpty()) "" else Json.encodeToString(headers)

    @JvmSynthetic
    fun jsonToHashMap(jsonString: String) =
        if (jsonString.isEmpty()) hashMapOf() else Json.decodeFromString<HashMap<String, String>>(
            jsonString
        )

    @JvmSynthetic
    fun constructDefaultFilename(url: String) =
        UUID.randomUUID().toString() + "-" + URLUtil.guessFileName(url, null, null)

    @JvmSynthetic
    fun getFormattedFileSize(totalLength: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = totalLength.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return String.format(Locale.US, "%.2f %s", size, units[unitIndex])
    }

    @JvmSynthetic
    fun constructUniqueId(url: String, dirPath: String, fileName: String) = runCatching {
        MessageDigest.getInstance("SHA-256")
            .digest("$url/$dirPath/$fileName".toByteArray(Charsets.UTF_8))
            .fold(0) { acc, byte -> (acc * 31) + byte.toInt() }
    }.getOrElse {
        (url.hashCode() * 31 + dirPath.hashCode()) * 31 + fileName.hashCode()
    }
}