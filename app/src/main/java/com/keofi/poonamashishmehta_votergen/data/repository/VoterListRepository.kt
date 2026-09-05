package com.keofi.poonamashishmehta_votergen.data.repository

import com.keofi.poonamashishmehta_votergen.data.db.dao.ImportJobDao
import com.keofi.poonamashishmehta_votergen.data.db.dao.VoterDao
import com.keofi.poonamashishmehta_votergen.data.db.dao.VoterListDao
import com.keofi.poonamashishmehta_votergen.data.db.entity.ImportJobEntity
import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterListEntity
import kotlinx.coroutines.flow.Flow

class VoterListRepository(
    private val voterListDao: VoterListDao,
    private val voterDao: VoterDao,
    private val importJobDao: ImportJobDao
) {
    fun getAllLists(): Flow<List<VoterListEntity>> = voterListDao.getAllLists()

    fun getListById(id: Long): Flow<VoterListEntity?> = voterListDao.getById(id)

    suspend fun getListByIdSync(id: Long): VoterListEntity? = voterListDao.getByIdSync(id)

    fun getTotalListsCount(): Flow<Int> = voterListDao.getTotalListsCount()

    suspend fun createList(list: VoterListEntity): Long = voterListDao.insert(list)

    suspend fun updateList(list: VoterListEntity) = voterListDao.update(list)

    suspend fun renameList(id: Long, newName: String) = voterListDao.rename(id, newName)

    suspend fun deleteList(id: Long) {
        importJobDao.deleteByListId(id)
        voterDao.deleteByListId(id)
        voterListDao.delete(id)
        com.keofi.poonamashishmehta_votergen.VoterApp.instance.pdfImportManager.resetState()
    }

    suspend fun deleteAllData() {
        importJobDao.deleteAll()
        voterDao.deleteAll()
        voterListDao.deleteAll()
        com.keofi.poonamashishmehta_votergen.VoterApp.instance.pdfImportManager.resetState()
    }

    // Import job tracking
    fun getActiveJob(): Flow<ImportJobEntity?> = importJobDao.getActiveJob()

    fun getJobById(id: Long): Flow<ImportJobEntity?> = importJobDao.getJobById(id)

    suspend fun createImportJob(job: ImportJobEntity): Long = importJobDao.insert(job)

    suspend fun updateImportJob(job: ImportJobEntity) = importJobDao.update(job)

    suspend fun finishJob(id: Long, status: String, error: String?, completedAt: Long = System.currentTimeMillis()) {
        importJobDao.finishJob(id, status, error, completedAt)
    }
}
