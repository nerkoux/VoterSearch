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

    fun searchVotersAdvanced(
        voterQuery: String = "",
        relativeQuery: String = "",
        booth: String = "",
        limit: Int = 150
    ): Flow<List<VoterEntity>> {
        val trimmedVoter = voterQuery.trim()
        val trimmedRelative = relativeQuery.trim()
        val trimmedBooth = booth.trim()

        if (trimmedVoter.isEmpty() && trimmedRelative.isEmpty() && trimmedBooth.isEmpty()) {
            return flowOf(emptyList())
        }

        val normalizedVoter = if (trimmedVoter.isNotEmpty()) TextNormalizer.normalize(trimmedVoter) else ""
        val decodedVoter = if (trimmedVoter.isNotEmpty()) {
            val dec = com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder.decodeHindi(trimmedVoter)
            if (dec != trimmedVoter) dec else ""
        } else ""
        val transliteratedVoter = if (trimmedVoter.isNotEmpty() && trimmedVoter.any { it in 'a'..'z' || it in 'A'..'Z' }) {
            com.keofi.poonamashishmehta_votergen.util.HindiTransliterationUtil.latinToDevanagari(trimmedVoter).firstOrNull() ?: ""
        } else ""

        val decodedRelative = if (trimmedRelative.isNotEmpty()) {
            val dec = com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder.decodeHindi(trimmedRelative)
            if (dec != trimmedRelative) dec else ""
        } else ""
        val transliteratedRelative = if (trimmedRelative.isNotEmpty() && trimmedRelative.any { it in 'a'..'z' || it in 'A'..'Z' }) {
            com.keofi.poonamashishmehta_votergen.util.HindiTransliterationUtil.latinToDevanagari(trimmedRelative).firstOrNull() ?: ""
        } else ""

        val parts = trimmedVoter.split(Regex("\\s+")).filter { it.isNotBlank() }
        val token1 = if (parts.size >= 2) parts[0] else ""
        val token2 = if (parts.size >= 2) parts.subList(1, parts.size).joinToString(" ") else ""

        return voterDao.searchVotersAdvanced(
            rawVoterQuery = trimmedVoter,
            normalizedVoterQuery = normalizedVoter,
            decodedVoterQuery = decodedVoter,
            transliteratedVoterQuery = transliteratedVoter,
            rawRelativeQuery = trimmedRelative,
            decodedRelativeQuery = decodedRelative,
            transliteratedRelativeQuery = transliteratedRelative,
            booth = trimmedBooth,
            token1 = token1,
            token2 = token2,
            limit = limit
        )
    }

    fun getAllBooths(): Flow<List<String>> = voterDao.getAllBooths()

    fun getBoothsForList(listId: Long): Flow<List<String>> = voterDao.getBoothsForList(listId)

    fun getVotersByBooth(pollingStation: String, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>> =
        voterDao.getVotersByBooth(pollingStation, limit, offset)

    fun getVotersByBoothInList(listId: Long, pollingStation: String, limit: Int = 100, offset: Int = 0): Flow<List<VoterEntity>> =
        voterDao.getVotersByBoothInList(listId, pollingStation, limit, offset)

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

    fun insertVotersSync(voters: List<VoterEntity>): List<Long> = voterDao.insertAllSync(voters)

    suspend fun insertVoter(voter: VoterEntity): Long = voterDao.insert(voter)

    suspend fun updateVoter(voter: VoterEntity) = voterDao.update(voter)

    suspend fun deleteVoter(id: Long) = voterDao.deleteById(id)

    suspend fun deleteAllVoters() = voterDao.deleteAll()
}
