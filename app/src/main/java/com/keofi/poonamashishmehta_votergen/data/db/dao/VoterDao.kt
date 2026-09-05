package com.keofi.poonamashishmehta_votergen.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoterDao {
    @Query(
        """
        SELECT * FROM voters
        WHERE normalizedEpic LIKE :normalizedQuery || '%'
           OR normalizedName LIKE '%' || :normalizedQuery || '%'
           OR epicNumber LIKE '%' || :rawQuery || '%'
           OR name LIKE '%' || :rawQuery || '%'
           OR nameHindi LIKE '%' || :rawQuery || '%'
           OR relativeName LIKE '%' || :rawQuery || '%'
           OR houseNumber LIKE '%' || :rawQuery || '%'
           OR (:decodedQuery != '' AND (
               name LIKE '%' || :decodedQuery || '%'
               OR nameHindi LIKE '%' || :decodedQuery || '%'
               OR relativeName LIKE '%' || :decodedQuery || '%'
               OR normalizedName LIKE '%' || :decodedQuery || '%'
           ))
           OR (:transliteratedQuery != '' AND (
               name LIKE '%' || :transliteratedQuery || '%'
               OR nameHindi LIKE '%' || :transliteratedQuery || '%'
               OR relativeName LIKE '%' || :transliteratedQuery || '%'
               OR normalizedName LIKE '%' || :transliteratedQuery || '%'
           ))
           OR CAST(serialNumber AS TEXT) = :rawQuery
        ORDER BY 
            CASE 
                WHEN normalizedEpic = :normalizedQuery THEN 1
                WHEN normalizedEpic LIKE :normalizedQuery || '%' THEN 2
                WHEN name = :rawQuery OR nameHindi = :rawQuery THEN 3
                WHEN name LIKE :rawQuery || '%' OR nameHindi LIKE :rawQuery || '%' THEN 4
                WHEN normalizedName LIKE :normalizedQuery || '%' THEN 5
                WHEN :transliteratedQuery != '' AND (name LIKE :transliteratedQuery || '%' OR nameHindi LIKE :transliteratedQuery || '%') THEN 6
                ELSE 7
            END,
            serialNumber ASC
        LIMIT :limit
        """
    )
    fun searchVoters(
        rawQuery: String,
        normalizedQuery: String,
        decodedQuery: String = "",
        transliteratedQuery: String = "",
        limit: Int = 100
    ): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE normalizedEpic = :normalizedEpic LIMIT 10")
    fun findByEpic(normalizedEpic: String): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE id = :id LIMIT 1")
    fun getById(id: Long): Flow<VoterEntity?>

    @Query("SELECT * FROM voters WHERE id = :id LIMIT 1")
    suspend fun getByIdSync(id: Long): VoterEntity?

    @Query("SELECT * FROM voters WHERE voterListId = :listId ORDER BY serialNumber ASC LIMIT :limit OFFSET :offset")
    fun getVotersByList(listId: Long, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE confidence < 0.75 OR epicNumber = '' ORDER BY id DESC")
    fun getVotersNeedingReview(): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE voterListId = :listId AND (confidence < 0.75 OR epicNumber = '') ORDER BY serialNumber ASC")
    fun getVotersNeedingReviewForList(listId: Long): Flow<List<VoterEntity>>

    @Query("SELECT COUNT(*) FROM voters")
    fun getTotalVoterCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM voters WHERE voterListId = :listId")
    fun getVoterCountForList(listId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(voters: List<VoterEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(voter: VoterEntity): Long

    @Update
    suspend fun update(voter: VoterEntity)

    @Query("DELETE FROM voters WHERE voterListId = :listId")
    suspend fun deleteByListId(listId: Long)

    @Query("DELETE FROM voters WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM voters")
    suspend fun deleteAll()
}
