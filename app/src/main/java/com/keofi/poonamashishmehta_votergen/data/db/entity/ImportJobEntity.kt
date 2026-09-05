package com.keofi.poonamashishmehta_votergen.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "import_jobs")
data class ImportJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voterListId: Long = 0,
    val listName: String = "",
    val currentPage: Int = 0,
    val totalPages: Int = 0,
    val votersDetected: Int = 0,
    val status: String = "Processing", // "Pending", "Processing", "Completed", "Failed", "Cancelled"
    val errorMessage: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
