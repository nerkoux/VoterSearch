package com.keofi.poonamashishmehta_votergen.data.repository

import com.keofi.poonamashishmehta_votergen.data.db.dao.VoterDao
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.util.TextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class VoterRepository(private val voterDao: VoterDao) {

    fun searchVoters(query: String, limit: Int = 100): Flow<List<VoterEntity>> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return flowOf(emptyList())
        }
        val normalized = TextNormalizer.normalize(trimmed)
        val decoded = com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder.decodeHindi(trimmed)
        val decodedQuery = if (decoded != trimmed) decoded else ""

        val hasLatin = trimmed.any { it in 'a'..'z' || it in 'A'..'Z' }
        val transliteratedList = if (hasLatin) {
            com.keofi.poonamashishmehta_votergen.util.HindiTransliterationUtil.latinToDevanagari(trimmed)
        } else emptyList()
        val transliteratedQuery = transliteratedList.firstOrNull() ?: ""

        return voterDao.searchVoters(
            rawQuery = trimmed,
            normalizedQuery = normalized,
            decodedQuery = decodedQuery,
            transliteratedQuery = transliteratedQuery,
            limit = limit
        )
    }

    fun getVoterById(id: Long): Flow<VoterEntity?> = voterDao.getById(id)

    suspend fun getVoterByIdSync(id: Long): VoterEntity? = voterDao.getByIdSync(id)

    fun getVotersByList(listId: Long, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>> =
        voterDao.getVotersByList(listId, limit, offset)

    fun getVotersNeedingReview(listId: Long? = null): Flow<List<VoterEntity>> {
        return if (listId != null) {
            voterDao.getVotersNeedingReviewForList(listId)
        } else {
            voterDao.getVotersNeedingReview()
        }
    }

    fun getTotalVoterCount(): Flow<Int> = voterDao.getTotalVoterCount()

    suspend fun insertVoters(voters: List<VoterEntity>): List<Long> = voterDao.insertAll(voters)

    suspend fun insertVoter(voter: VoterEntity): Long = voterDao.insert(voter)

    suspend fun updateVoter(voter: VoterEntity) = voterDao.update(voter)

    suspend fun deleteVoter(id: Long) = voterDao.deleteById(id)

    suspend fun deleteAllVoters() = voterDao.deleteAll()
}
