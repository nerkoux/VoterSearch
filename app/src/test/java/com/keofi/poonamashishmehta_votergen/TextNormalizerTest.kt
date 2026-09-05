package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.util.TextNormalizer
import org.junit.Assert.assertEquals
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun testNormalizeEpic() {
        assertEquals("ABC1234567", TextNormalizer.normalizeEpic("ABC 1234567"))
        assertEquals("ABC1234567", TextNormalizer.normalizeEpic("abc-1234567"))
        assertEquals("ABC1234567", TextNormalizer.normalizeEpic("  ABC1234567  "))
        assertEquals("UP01012123456", TextNormalizer.normalizeEpic("UP/01/012/123456"))
    }

    @Test
    fun testNormalizeName() {
        assertEquals("ramesh kumar", TextNormalizer.normalizeName("  Ramesh   Kumar  "))
        val hindiNorm = TextNormalizer.normalizeName("रमेश कुमार")
        org.junit.Assert.assertTrue(hindiNorm.contains("रमेश कुमार"))
        org.junit.Assert.assertTrue(hindiNorm.contains("ramesh kumar"))
    }

    @Test
    fun testNormalizeQuery() {
        assertEquals("ABC1234567", TextNormalizer.normalize("abc 1234567"))
        assertEquals("ramesh", TextNormalizer.normalize("  Ramesh  "))
    }
}
