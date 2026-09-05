package com.keofi.poonamashishmehta_votergen.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.keofi.poonamashishmehta_votergen.data.db.dao.ImportJobDao
import com.keofi.poonamashishmehta_votergen.data.db.dao.VoterDao
import com.keofi.poonamashishmehta_votergen.data.db.dao.VoterListDao
import com.keofi.poonamashishmehta_votergen.data.db.entity.ImportJobEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity

@Database(
    entities = [
        VoterEntity::class,
        VoterListEntity::class,
        ImportJobEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voterDao(): VoterDao
    abstract fun voterListDao(): VoterListDao
    abstract fun importJobDao(): ImportJobDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voter_search_database.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
