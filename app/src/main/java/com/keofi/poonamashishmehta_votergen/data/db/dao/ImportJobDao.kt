package com.keofi.poonamashishmehta_votergen.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keofi.poonamashishmehta_votergen.data.db.entity.ImportJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportJobDao {
    @Query("SELECT * FROM import_jobs WHERE status = 'Processing' ORDER BY startedAt DESC LIMIT 1")
    fun getActiveJob(): Flow<ImportJobEntity?>

    @Query("SELECT * FROM import_jobs WHERE id = :id LIMIT 1")
    fun getJobById(id: Long): Flow<ImportJobEntity?>

    @Query("SELECT * FROM import_jobs ORDER BY startedAt DESC LIMIT 20")
    fun getRecentJobs(): Flow<List<ImportJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: ImportJobEntity): Long

    @Update
    suspend fun update(job: ImportJobEntity)

    @Query("UPDATE import_jobs SET status = :status, errorMessage = :error, completedAt = :completedAt WHERE id = :id")
    suspend fun finishJob(id: Long, status: String, error: String?, completedAt: Long)

    @Query("DELETE FROM import_jobs WHERE voterListId = :listId")
    suspend fun deleteByListId(listId: Long)

    @Query("DELETE FROM import_jobs")
    suspend fun deleteAll()
}
