package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.util.HindiTransliterationUtil
import com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddressSearchTest {

    @Test
    fun testAddressLineFormatting() {
        val voterWithBoth = VoterEntity(
            houseNumber = "2-62",
            address = "सैक्टर नं. 2क जवाहर नगर, वार्ड नं. 123"
        )
        val lineBoth = listOfNotNull(
            voterWithBoth.houseNumber?.let { "H.No: $it" },
            voterWithBoth.address
        ).filter { it.isNotBlank() }.joinToString(", ")
        assertEquals("H.No: 2-62, सैक्टर नं. 2क जवाहर नगर, वार्ड नं. 123", lineBoth)

        val voterHouseOnly = VoterEntity(
            houseNumber = "12/A",
            address = null
        )
        val lineHouseOnly = listOfNotNull(
            voterHouseOnly.houseNumber?.let { "H.No: $it" },
            voterHouseOnly.address
        ).filter { it.isNotBlank() }.joinToString(", ")
        assertEquals("H.No: 12/A", lineHouseOnly)

        val voterAddressOnly = VoterEntity(
            houseNumber = null,
            address = "Civil Lines"
        )
        val lineAddressOnly = listOfNotNull(
            voterAddressOnly.houseNumber?.let { "H.No: $it" },
            voterAddressOnly.address
        ).filter { it.isNotBlank() }.joinToString(", ")
        assertEquals("Civil Lines", lineAddressOnly)
    }

    @Test
    fun testAddressTokenSplitting() {
        // Single word query
        val query1 = "2-62"
        val parts1 = query1.split(Regex("\\s+")).filter { it.isNotBlank() }
        val token1A = if (parts1.size >= 2) parts1[0] else ""
        val token1B = if (parts1.size >= 2) parts1.subList(1, parts1.size).joinToString(" ") else ""
        assertEquals("", token1A)
        assertEquals("", token1B)

        // Two words: Name + House number
        val query2 = "Mohit 2-1"
        val parts2 = query2.split(Regex("\\s+")).filter { it.isNotBlank() }
        val token2A = if (parts2.size >= 2) parts2[0] else ""
        val token2B = if (parts2.size >= 2) parts2.subList(1, parts2.size).joinToString(" ") else ""
        assertEquals("Mohit", token2A)
        assertEquals("2-1", token2B)

        // Three words: Name + Locality (e.g. Mohit Jawahar Nagar)
        val query3 = "Mohit Jawahar Nagar"
        val parts3 = query3.split(Regex("\\s+")).filter { it.isNotBlank() }
        val token3A = if (parts3.size >= 2) parts3[0] else ""
        val token3B = if (parts3.size >= 2) parts3.subList(1, parts3.size).joinToString(" ") else ""
        assertEquals("Mohit", token3A)
        assertEquals("Jawahar Nagar", token3B)
    }

    @Test
    fun testAddressDecodingAndTransliteration() {
        // SEC legacy address words
        val decoded = RajasthanSecDecoder.decodeHindi("मकपन सनखयप जवपरर नगर वपरर 123")
        assertTrue(decoded.contains("मकान"))
        assertTrue(decoded.contains("संख्या"))
        assertTrue(decoded.contains("जवाहर"))
        assertTrue(decoded.contains("वार्ड"))

        // English to Devanagari transliteration
        val transliterated = HindiTransliterationUtil.latinToDevanagari("jawahar")
        assertTrue(transliterated.isNotEmpty())
        assertEquals("जवाहर", transliterated.first())
    }
}
