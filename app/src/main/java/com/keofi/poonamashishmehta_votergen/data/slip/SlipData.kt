package com.keofi.poonamashishmehta_votergen.data.slip

import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipSettings
import com.keofi.poonamashishmehta_votergen.data.preferences.SlipType

data class SlipData(
    val voterName: String,
    val voterNameHindi: String? = null,
    val epicNumber: String,
    val relativeName: String? = null,
    val relationship: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val partNumber: String? = null,
    val serialNumber: Int? = null,
    val houseNumber: String? = null,
    val pollingStation: String? = null,
    val candidateName: String = "Poonam Ashish Mehta",
    val tagline: String = "Voter Information Slip",
    val slipType: SlipType = SlipType.CUSTOM_IMAGE,
    val headerImageUri: String? = null
) {
    companion object {
        fun fromVoter(voter: VoterEntity, settings: SlipSettings): SlipData {
            return SlipData(
                voterName = voter.name,
                voterNameHindi = voter.nameHindi,
                epicNumber = voter.epicNumber,
                relativeName = voter.relativeName,
                relationship = voter.relationship,
                age = voter.age,
                gender = voter.gender,
                partNumber = voter.partNumber,
                serialNumber = voter.serialNumber,
                houseNumber = voter.houseNumber,
                pollingStation = voter.pollingStation,
                candidateName = settings.candidateName,
                tagline = settings.tagline,
                slipType = settings.slipType,
                headerImageUri = settings.headerImageUri
            )
        }
    }
}
