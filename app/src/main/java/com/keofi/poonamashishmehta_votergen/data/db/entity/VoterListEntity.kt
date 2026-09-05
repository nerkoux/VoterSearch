package com.keofi.poonamashishmehta_votergen.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voter_lists")
data class VoterListEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val sourceFilePath: String = "",
    val totalPages: Int = 0,
    val totalVoters: Int = 0,
    val highConfidence: Int = 0,
    val needsReview: Int = 0,
    val epicDetected: Int = 0,
    val importedAt: Long = System.currentTimeMillis(),
    val status: String = "Completed"
)
