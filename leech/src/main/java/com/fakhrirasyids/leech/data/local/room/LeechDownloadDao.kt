package com.fakhrirasyids.leech.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface LeechDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadItem(entity: LeechDownloadEntity)

    @Update
    suspend fun updateDownloadItem(entity: LeechDownloadEntity)

    @Query("DELETE FROM leech_downloads WHERE id = :id")
    suspend fun deleteDownloadItem(id: Int)

    @Query("DELETE FROM leech_downloads")
    suspend fun deleteAllDownloadItems()

    @Query("SELECT * FROM leech_downloads WHERE id = :id")
    fun getDownloadItem(id: Int): Flow<LeechDownloadEntity?>

    @Query("SELECT * FROM leech_downloads ORDER BY queuedTime ASC")
    fun getAllDownloadItems(): Flow<MutableList<LeechDownloadEntity>>
}
