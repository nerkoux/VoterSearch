package com.keofi.poonamashishmehta_votergen.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "voters",
    indices = [
        Index(value = ["epicNumber"]),
        Index(value = ["normalizedEpic"]),
        Index(value = ["name"]),
        Index(value = ["normalizedName"]),
        Index(value = ["serialNumber"]),
        Index(value = ["partNumber"]),
        Index(value = ["voterListId"])
    ]
)
data class VoterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epicNumber: String = "",
    val normalizedEpic: String = "",
    val name: String = "",
    val nameHindi: String? = null,
    val normalizedName: String = "",
    val relativeName: String? = null,
    val relationship: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val houseNumber: String? = null,
    val partNumber: String? = null,
    val serialNumber: Int? = null,
    val address: String? = null,
    val pollingStation: String? = null,
    val status: String = "Active",
    val voterListId: Long = 0,
    val sourcePdf: String = "",
    val sourcePage: Int = 1,
    val rawOcrText: String = "",
    val confidence: Float = 1.0f,
    val createdAt: Long = System.currentTimeMillis()
)
