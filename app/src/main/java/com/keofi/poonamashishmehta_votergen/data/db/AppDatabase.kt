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

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        VoterEntity::class,
        VoterListEntity::class,
        ImportJobEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voterDao(): VoterDao
    abstract fun voterListDao(): VoterListDao
    abstract fun importJobDao(): ImportJobDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_voters_relativeName` ON `voters` (`relativeName`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_voters_pollingStation` ON `voters` (`pollingStation`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voter_search_database.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
