package com.keofi.poonamashishmehta_votergen.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterListDao {
    @Query("SELECT * FROM voter_lists ORDER BY importedAt DESC")
    fun getAllLists(): Flow<List<VoterListEntity>>

    @Query("SELECT * FROM voter_lists WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<VoterListEntity?>

    @Query("SELECT * FROM voter_lists WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Long): VoterListEntity?

    @Query("SELECT COUNT(*) FROM voter_lists")
    fun getTotalListsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: VoterListEntity): Long

    @Update
    suspend fun update(list: VoterListEntity)

    @Query("UPDATE voter_lists SET name = :newName WHERE id = :id")
    suspend fun rename(id: Long, newName: String)

    @Query("DELETE FROM voter_lists WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM voter_lists")
    suspend fun deleteAll()
}
