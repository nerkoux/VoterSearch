package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.util.HindiTransliterationUtil
import com.keofi.poonamashishmehta_votergen.util.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HindiTransliterationTest {

    @Test
    fun testLatinToDevanagari() {
        val akshat = HindiTransliterationUtil.latinToDevanagari("akshat")
        assertTrue(akshat.contains("अक्षत"))

        val mohit = HindiTransliterationUtil.latinToDevanagari("mohit")
        assertTrue(mohit.contains("मोहित"))

        val puneet = HindiTransliterationUtil.latinToDevanagari("puneet")
        assertTrue(puneet.contains("पुनीत"))

        val punit = HindiTransliterationUtil.latinToDevanagari("punit")
        assertTrue(punit.contains("पुनीत"))

        val vijay = HindiTransliterationUtil.latinToDevanagari("vijay")
        assertTrue(vijay.contains("विजय"))

        val ram = HindiTransliterationUtil.latinToDevanagari("ram")
        assertTrue(ram.contains("राम"))
    }

    @Test
    fun testDevanagariToLatin() {
        assertEquals("akshat", HindiTransliterationUtil.devanagariToLatin("अक्षत"))
        assertEquals("mohit bhargav", HindiTransliterationUtil.devanagariToLatin("मोहित भार्गव"))
        assertEquals("puneet gupta", HindiTransliterationUtil.devanagariToLatin("पुनीत गुप्ता"))
        assertEquals("vijay kumar", HindiTransliterationUtil.devanagariToLatin("विजय कुमार"))
        assertEquals("aman gupta", HindiTransliterationUtil.devanagariToLatin("अमन गुप्ता"))
    }

    @Test
    fun testNormalizeNameIncludesRomanization() {
        val normalized = TextNormalizer.normalizeName("अक्षत")
        assertTrue(normalized.contains("अक्षत"))
        assertTrue(normalized.contains("akshat"))

        val normalizedMohit = TextNormalizer.normalizeName("मोहित भार्गव")
        assertTrue(normalizedMohit.contains("मोहित भार्गव"))
        assertTrue(normalizedMohit.contains("mohit"))
    }
}
