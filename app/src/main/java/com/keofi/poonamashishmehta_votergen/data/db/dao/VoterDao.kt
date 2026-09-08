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
           OR address LIKE '%' || :rawQuery || '%'
           OR pollingStation LIKE '%' || :rawQuery || '%'
           OR (:decodedQuery != '' AND (
               name LIKE '%' || :decodedQuery || '%'
               OR nameHindi LIKE '%' || :decodedQuery || '%'
               OR relativeName LIKE '%' || :decodedQuery || '%'
               OR normalizedName LIKE '%' || :decodedQuery || '%'
               OR houseNumber LIKE '%' || :decodedQuery || '%'
               OR address LIKE '%' || :decodedQuery || '%'
               OR pollingStation LIKE '%' || :decodedQuery || '%'
           ))
           OR (:transliteratedQuery != '' AND (
               name LIKE '%' || :transliteratedQuery || '%'
               OR nameHindi LIKE '%' || :transliteratedQuery || '%'
               OR relativeName LIKE '%' || :transliteratedQuery || '%'
               OR normalizedName LIKE '%' || :transliteratedQuery || '%'
               OR houseNumber LIKE '%' || :transliteratedQuery || '%'
               OR address LIKE '%' || :transliteratedQuery || '%'
               OR pollingStation LIKE '%' || :transliteratedQuery || '%'
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
                WHEN houseNumber = :rawQuery OR houseNumber LIKE :rawQuery || '%' THEN 7
                WHEN address LIKE '%' || :rawQuery || '%' THEN 8
                ELSE 9
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

    @Query(
        """
        SELECT * FROM voters
        WHERE (:booth = '' OR pollingStation = :booth OR partNumber = :booth)
          AND (
              :rawVoterQuery = ''
              OR normalizedEpic LIKE :normalizedVoterQuery || '%'
              OR normalizedName LIKE '%' || :normalizedVoterQuery || '%'
              OR epicNumber LIKE '%' || :rawVoterQuery || '%'
              OR name LIKE '%' || :rawVoterQuery || '%'
              OR nameHindi LIKE '%' || :rawVoterQuery || '%'
              OR houseNumber LIKE '%' || :rawVoterQuery || '%'
              OR address LIKE '%' || :rawVoterQuery || '%'
              OR pollingStation LIKE '%' || :rawVoterQuery || '%'
              OR (:decodedVoterQuery != '' AND (
                  name LIKE '%' || :decodedVoterQuery || '%'
                  OR nameHindi LIKE '%' || :decodedVoterQuery || '%'
                  OR normalizedName LIKE '%' || :decodedVoterQuery || '%'
                  OR houseNumber LIKE '%' || :decodedVoterQuery || '%'
                  OR address LIKE '%' || :decodedVoterQuery || '%'
                  OR pollingStation LIKE '%' || :decodedVoterQuery || '%'
              ))
              OR (:transliteratedVoterQuery != '' AND (
                  name LIKE '%' || :transliteratedVoterQuery || '%'
                  OR nameHindi LIKE '%' || :transliteratedVoterQuery || '%'
                  OR normalizedName LIKE '%' || :transliteratedVoterQuery || '%'
                  OR houseNumber LIKE '%' || :transliteratedVoterQuery || '%'
                  OR address LIKE '%' || :transliteratedVoterQuery || '%'
                  OR pollingStation LIKE '%' || :transliteratedVoterQuery || '%'
              ))
              OR CAST(serialNumber AS TEXT) = :rawVoterQuery
              OR (
                  :token1 != '' AND :token2 != '' AND (
                      (
                          name LIKE '%' || :token1 || '%' 
                          OR nameHindi LIKE '%' || :token1 || '%' 
                          OR normalizedName LIKE '%' || :token1 || '%'
                      )
                      AND
                      (
                          houseNumber LIKE '%' || :token2 || '%' 
                          OR address LIKE '%' || :token2 || '%' 
                          OR relativeName LIKE '%' || :token2 || '%'
                      )
                  )
              )
              OR (
                  :token1 != '' AND :token2 != '' AND (
                      (
                          houseNumber LIKE '%' || :token1 || '%' 
                          OR address LIKE '%' || :token1 || '%'
                      )
                      AND
                      (
                          name LIKE '%' || :token2 || '%' 
                          OR nameHindi LIKE '%' || :token2 || '%' 
                          OR normalizedName LIKE '%' || :token2 || '%'
                          OR relativeName LIKE '%' || :token2 || '%'
                      )
                  )
              )
          )
          AND (
              :rawRelativeQuery = ''
              OR relativeName LIKE '%' || :rawRelativeQuery || '%'
              OR (:decodedRelativeQuery != '' AND relativeName LIKE '%' || :decodedRelativeQuery || '%')
              OR (:transliteratedRelativeQuery != '' AND relativeName LIKE '%' || :transliteratedRelativeQuery || '%')
          )
        ORDER BY 
            CASE 
                WHEN :rawVoterQuery != '' AND normalizedEpic = :normalizedVoterQuery THEN 1
                WHEN :rawVoterQuery != '' AND normalizedEpic LIKE :normalizedVoterQuery || '%' THEN 2
                WHEN :rawVoterQuery != '' AND (name = :rawVoterQuery OR nameHindi = :rawVoterQuery) THEN 3
                WHEN :rawVoterQuery != '' AND (name LIKE :rawVoterQuery || '%' OR nameHindi LIKE :rawVoterQuery || '%') THEN 4
                WHEN :rawVoterQuery != '' AND normalizedName LIKE :normalizedVoterQuery || '%' THEN 5
                WHEN :rawVoterQuery != '' AND (houseNumber = :rawVoterQuery OR houseNumber LIKE :rawVoterQuery || '%') THEN 6
                WHEN :rawVoterQuery != '' AND address LIKE '%' || :rawVoterQuery || '%' THEN 7
                ELSE 8
            END,
            serialNumber ASC
        LIMIT :limit
        """
    )
    fun searchVotersAdvanced(
        rawVoterQuery: String = "",
        normalizedVoterQuery: String = "",
        decodedVoterQuery: String = "",
        transliteratedVoterQuery: String = "",
        rawRelativeQuery: String = "",
        decodedRelativeQuery: String = "",
        transliteratedRelativeQuery: String = "",
        booth: String = "",
        token1: String = "",
        token2: String = "",
        limit: Int = 150
    ): Flow<List<VoterEntity>>

    @Query("SELECT DISTINCT pollingStation FROM voters WHERE pollingStation IS NOT NULL AND pollingStation != '' ORDER BY pollingStation ASC")
    fun getAllBooths(): Flow<List<String>>

    @Query("SELECT DISTINCT pollingStation FROM voters WHERE voterListId = :listId AND pollingStation IS NOT NULL AND pollingStation != '' ORDER BY pollingStation ASC")
    fun getBoothsForList(listId: Long): Flow<List<String>>

    @Query("SELECT * FROM voters WHERE pollingStation = :pollingStation ORDER BY serialNumber ASC LIMIT :limit OFFSET :offset")
    fun getVotersByBooth(pollingStation: String, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>>

    @Query("SELECT * FROM voters WHERE voterListId = :listId AND pollingStation = :pollingStation ORDER BY serialNumber ASC LIMIT :limit OFFSET :offset")
    fun getVotersByBoothInList(listId: Long, pollingStation: String, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>>

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
    fun insertAllSync(voters: List<VoterEntity>): List<Long>

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
