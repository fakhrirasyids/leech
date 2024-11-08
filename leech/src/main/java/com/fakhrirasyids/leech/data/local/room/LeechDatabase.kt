package com.fakhrirasyids.leech.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fakhrirasyids.leech.domain.models.LeechDownloadEntity

@Database(entities = [LeechDownloadEntity::class], version = 1, exportSchema = false)
internal abstract class LeechDatabase : RoomDatabase() {
    abstract fun leechDownloadDao(): LeechDownloadDao

    companion object {
        @Volatile
        private var INSTANCE: LeechDatabase? = null

        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LeechDatabase::class.java, "leech.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
