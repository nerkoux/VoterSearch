package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.data.parser.VoterParser
import com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VoterParserTest {

    @Test
    fun testParseStandardVoterBlock() {
        val pageText = """
            Part No: 123
            Polling Station: Primary School Room 2
            
            184
            ABC1234567
            Name: Ramesh Kumar
            Father's Name: Mohan Lal
            House No: 42
            Age: 52 Gender: Male
        """.trimIndent()

        val results = VoterParser.parsePage(
            pageText = pageText,
            sourcePdf = "Ward_123.pdf",
            pageNumber = 1,
            voterListId = 1L
        )

        assertTrue("Expected at least 1 parsed voter", results.isNotEmpty())
        val res = results.first()
        val voter = res.voter

        assertEquals("Ramesh Kumar", voter.name)
        assertEquals("ABC1234567", voter.epicNumber)
        assertEquals("ABC1234567", voter.normalizedEpic)
        assertEquals("Mohan Lal", voter.relativeName)
        assertEquals("Father", voter.relationship)
        assertEquals(52, voter.age)
        assertEquals("Male", voter.gender)
        assertEquals("42", voter.houseNumber)
        assertEquals(184, voter.serialNumber)
        assertEquals("123", voter.partNumber)
        assertEquals("Primary School Room 2", voter.pollingStation)
        assertTrue("Expected high confidence", res.isHighConfidence)
        assertTrue("Confidence should be >= 0.75", voter.confidence >= 0.75f)
    }

    @Test
    fun testParseHindiVoterBlock() {
        val pageText = """
            219
            XYZ7654321
            मतदाता का नाम: रमेश लाल
            पति का नाम: सोहन लाल
            मकान संख्या: 15
            आयु: 45 लिंग: महिला
        """.trimIndent()

        val results = VoterParser.parsePage(
            pageText = pageText,
            sourcePdf = "Ward_123.pdf",
            pageNumber = 2,
            voterListId = 1L
        )

        assertTrue("Expected at least 1 parsed voter", results.isNotEmpty())
        val voter = results.first().voter

        assertEquals("XYZ7654321", voter.epicNumber)
        assertNotNull(voter.nameHindi)
        assertEquals("रमेश लाल", voter.nameHindi)
        assertEquals("Husband", voter.relationship)
        assertEquals("सोहन लाल", voter.relativeName)
        assertEquals(45, voter.age)
        assertEquals("Female", voter.gender)
        assertEquals("15", voter.houseNumber)
        assertEquals(219, voter.serialNumber)
    }

    @Test
    fun testParseRajasthanSecPage() {
        // Exact extracted text from list.pdf page 3 (Ward 123 Jaipur Nagar Nigam)
        val secPageText = """
            रपजय ननवपरचन आययग, रपजसथपन 
             नगरननगम / नगरपररषद / नगरपपनलकप कप नपम : जयपपर 
            नवधपनसमप ककत कक सनखयप एवन नपम:- 53-आदशर नगर
            वपरर सनखयप : 123 मपग सनखयप : 13
             नगरपपनलकप चपनपव ननवपरचक नपमपवलर, 2026   
            जवपरर नगर सस. नन. 4 क,
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  69 पपरष
            2
            दरपक कपमपर
            रप. बर.बर.एल
            RJ/06/042/336003
            Photo is 
            Available
             1 S
            नपम: 
            ललग: 
            मकपन सनखयप: 
            पनत कप नपम:
            आयप:  67 सर
            2
            पदमप
            दरपक कपमपर
            RJ/06/042/336004
            Photo is 
            Available
             2 S
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  41 पपरष
            2
            मकहल कपमपर
            दरपक कपमपर
            MCN2964294
            Photo is 
            Available
             3 S
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  88 पपरष
            4
            लकमण लसर
            तकजरपज
            MCN2386779
            Photo is 
            Available
             5 E
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  73 पपरष
            4ब 11
            ननद लपल कपलरप
            मयतररपम कपलरप
            WUX0784231
            Photo is 
            Available
             13 S
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  27 पपरष
            ४-बर-२१
            अनमनव शरवपसतव
            सनतयष कपमपर शरवपसतव
            WUX1585603
            Photo is 
            Available
             15 S
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  34 सर
            4 क 2
            दरनपननतकप
            दरपक कपमपर
            WUX0993584
            Photo is 
            Available
             27 S
            आयप 1 जनवरर 2026 कक  अनपसपर पतष सनखयप : 3 / 22
        """.trimIndent()

        val results = VoterParser.parsePage(
            pageText = secPageText,
            sourcePdf = "list.pdf",
            pageNumber = 3,
            voterListId = 123L
        )

        assertEquals("Expected 7 voters from sample snippet", 7, results.size)

        // Card 1: Deepak Kumar
        val v1 = results[0].voter
        assertEquals(1, v1.serialNumber)
        assertEquals("RJ/06/042/336003", v1.epicNumber)
        assertEquals("RJ06042336003", v1.normalizedEpic)
        assertEquals("दीपक कुमार", v1.name)
        assertEquals("Father", v1.relationship)
        assertEquals(69, v1.age)
        assertEquals("Male", v1.gender)
        assertEquals("2", v1.houseNumber)
        assertEquals("13", v1.partNumber)
        assertEquals("Active", v1.status)
        assertTrue(results[0].isHighConfidence)

        // Card 2: Padma (Wife of Deepak Kumar)
        val v2 = results[1].voter
        assertEquals(2, v2.serialNumber)
        assertEquals("RJ/06/042/336004", v2.epicNumber)
        assertEquals("पद्मा", v2.name)
        assertEquals("Husband", v2.relationship)
        assertEquals("दीपक कुमार", v2.relativeName)
        assertEquals(67, v2.age)
        assertEquals("Female", v2.gender)

        // Card 3: Mehul Kumar
        val v3 = results[2].voter
        assertEquals(3, v3.serialNumber)
        assertEquals("MCN2964294", v3.epicNumber)
        assertEquals("मेहुल कुमार", v3.name)
        assertEquals("दीपक कुमार", v3.relativeName)
        assertEquals(41, v3.age)
        assertEquals("Male", v3.gender)

        // Card 5: Laxman Singh
        val v4 = results[3].voter
        assertEquals(5, v4.serialNumber)
        assertEquals("MCN2386779", v4.epicNumber)
        assertEquals("लक्ष्मण सिंह", v4.name)
        assertEquals("तेजराज", v4.relativeName)
        assertEquals(88, v4.age)
        assertEquals("Male", v4.gender)

        // Card 13: Nand Lal Kalra
        val v5 = results[4].voter
        assertEquals(13, v5.serialNumber)
        assertEquals("WUX0784231", v5.epicNumber)
        assertEquals("नन्द लाल कालरा", v5.name)
        assertEquals("मोतीराम कालरा", v5.relativeName)
        assertEquals(73, v5.age)

        // Card 15: Abhinav Shrivastava
        val v6 = results[5].voter
        assertEquals(15, v6.serialNumber)
        assertEquals("WUX1585603", v6.epicNumber)
        assertEquals("अभिनव श्रीवास्तव", v6.name)
        assertEquals(27, v6.age)
        assertEquals("4-B-21", v6.houseNumber)

        // Card 27: Deepantika
        val v7 = results[6].voter
        assertEquals(27, v7.serialNumber)
        assertEquals("WUX0993584", v7.epicNumber)
        assertEquals("दीपन्तिका", v7.name)
        assertEquals("दीपक कुमार", v7.relativeName)
        assertEquals(34, v7.age)
        assertEquals("Female", v7.gender)
    }

    @Test
    fun testRajasthanSecDecoder() {
        assertEquals("दीपक कुमार", RajasthanSecDecoder.decodeHindi("दरपक कपमपर"))
        assertEquals("पद्मा", RajasthanSecDecoder.decodeHindi("पदमप"))
        assertEquals("लक्ष्मण सिंह", RajasthanSecDecoder.decodeHindi("लकमण लसर"))
        assertEquals("राजीव सिंधी", RajasthanSecDecoder.decodeHindi("रपजरव लसधर"))
        assertEquals("अशोक सिंधी", RajasthanSecDecoder.decodeHindi("अशयक लसधर"))
        assertEquals("कान्ता कालरा", RajasthanSecDecoder.decodeHindi("कपनतप कपलरप"))
        assertEquals("श्रीवास्तव", RajasthanSecDecoder.decodeHindi("शरवपसतव"))
        assertEquals("चौहान", RajasthanSecDecoder.decodeHindi("चचरपन"))
        assertEquals("जैन", RajasthanSecDecoder.decodeHindi("जसन"))

        assertEquals("4-B-21", RajasthanSecDecoder.decodeHouseNumber("४-बर-२१"))
        assertEquals("4B 11", RajasthanSecDecoder.decodeHouseNumber("4ब 11"))
        assertEquals("अक्षत मेहता", RajasthanSecDecoder.decodeHindi("अकत मकरतप"))
        assertEquals("आशीष मेहता", RajasthanSecDecoder.decodeHindi("आनशष मकरतप"))
        assertEquals("पूनम मेहता", RajasthanSecDecoder.decodeHindi("पपनम मकरतप"))
        assertEquals("चंदनानी", RajasthanSecDecoder.decodeHindi("चनदनपनर"))
        assertEquals("ज्ञानचंदानी", RajasthanSecDecoder.decodeHindi("जपनचनदपनर"))
        assertEquals("खंडेलवाल", RajasthanSecDecoder.decodeHindi("खणरवलवपल"))
        assertEquals("तोमर", RajasthanSecDecoder.decodeHindi("तयमर"))
        assertEquals("राजेश", RajasthanSecDecoder.decodeHindi("रपजकश"))
        assertEquals("अंजू", RajasthanSecDecoder.decodeHindi("अनजप"))
        assertEquals("वंदना", RajasthanSecDecoder.decodeHindi("वनदनप"))
        assertEquals("फाल्गुनी जैन", RajasthanSecDecoder.decodeHindi("फपलगपनर जसन"))

        // Font Variant 2 (Supplementary / Deletion list)
        assertEquals("दीपक कुमार", RajasthanSecDecoder.decodeHindi("दलपक कपमरर"))
        assertEquals("लक्ष्मण सिंह", RajasthanSecDecoder.decodeHindi("लकमण लसह"))
        assertEquals("शान्ति", RajasthanSecDecoder.decodeHindi("ररसनत"))
        assertEquals("राजीव सिंधी", RajasthanSecDecoder.decodeHindi("ररजलर लसधल"))
        assertEquals("अभिनव श्रीवास्तव", RajasthanSecDecoder.decodeHindi("अनभनर शलररसतर"))
        assertEquals("हर्ष वर्धन सिंह", RajasthanSecDecoder.decodeHindi("हषर रधरन लसह"))
        assertEquals("हर्षवर्धन सिंह", RajasthanSecDecoder.decodeHindi("हषररधरन लसह"))
        assertEquals("गीता चौधरी", RajasthanSecDecoder.decodeHindi("गलतर चचधरल"))
        assertEquals("सुनीता मेहता", RajasthanSecDecoder.decodeHindi("सपननतर मकहरर"))
        assertEquals("हेमंत कुमार जैन", RajasthanSecDecoder.decodeHindi("हकमनत कपमरर जसन"))
        assertEquals("सुदर्शन करनावट", RajasthanSecDecoder.decodeHindi("सपदशरन करनपवक"))
        assertEquals("सुदर्शन करनावट", RajasthanSecDecoder.decodeHindi("सपदशरन कनपरवक"))

        // Standard Unicode Devanagari (Mangal / Tiro font output) preserved 100% intact
        assertEquals("सुदर्शन करनावट", RajasthanSecDecoder.decodeHindi("सुदर्शन करनावट"))
        assertEquals("अक्षत मेहता", RajasthanSecDecoder.decodeHindi("अक्षत मेहता"))
        assertEquals("दीपक कुमार", RajasthanSecDecoder.decodeHindi("दीपक कुमार"))
        assertEquals("राजीव शर्मा", RajasthanSecDecoder.decodeHindi("राजीव शर्मा"))
        assertEquals("पुनीत पारीक", RajasthanSecDecoder.decodeHindi("पुनीत पारीक"))
    }

    @Test
    fun testParseAkshatMehtaVoterCard() {
        // Real snippet from Page 439 containing Akshat Mehta (WUX2231975) and Poonam Mehta (WUX0144857)
        val snippet = """
            वपरर सनखयप : 123 मपग सनखयप : 7
             नगरपपनलकप चपनपव ननवपरचक नपमपवलर, 2026
            जवपरर नगर सस. नन. 7 क
            नपम: 
            ललग: 
            मकपन सनखयप: 
            पनत कप नपम:
            आयप:  46 सर
            3/154
            पपनम मकरतप
            आशरष मकरतप
            WUX0144857
            Photo is 
            Available
             257 
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  19 पपरष
            3/154
            अकत मकरतप
            आनशष मकरतप
            WUX2231975
            Photo is 
            Available
             258 
            आयप 1 जनवरर 2026 कक  अनपसपर पतष सनखयप : 439 / 863
        """.trimIndent()

        val results = VoterParser.parsePage(
            pageText = snippet,
            sourcePdf = "list.pdf",
            pageNumber = 439,
            voterListId = 123L
        )

        assertEquals("Expected 2 parsed voters", 2, results.size)

        // Card 257: Poonam Mehta
        val v1 = results[0].voter
        assertEquals("WUX0144857", v1.epicNumber)
        assertEquals(257, v1.serialNumber)
        assertEquals("पूनम मेहता", v1.name)
        assertEquals("Husband", v1.relationship)
        assertEquals("आशीष मेहता", v1.relativeName)
        assertEquals(46, v1.age)
        assertEquals("Female", v1.gender)
        assertEquals("3/154", v1.houseNumber)

        // Card 258: Akshat Mehta (User)
        val v2 = results[1].voter
        assertEquals("WUX2231975", v2.epicNumber)
        assertEquals("WUX2231975", v2.normalizedEpic)
        assertEquals(258, v2.serialNumber)
        assertEquals("अक्षत मेहता", v2.name)
        assertEquals("अक्षत मेहता", v2.nameHindi)
        assertEquals("Father", v2.relationship)
        assertEquals("आशीष मेहता", v2.relativeName)
        assertEquals(19, v2.age)
        assertEquals("Male", v2.gender)
        assertEquals("3/154", v2.houseNumber)

        // Verify normalizedName contains search keys for English and Hindi
        assertTrue(v2.normalizedName.contains("अक्षत"))
        assertTrue(v2.normalizedName.contains("मेहता"))
        assertTrue(v2.normalizedName.contains("akshat"))
        assertTrue(v2.normalizedName.contains("mehta"))
    }

    @Test
    fun testParseMannuKarnawatVoterCard() {
        // Real snippet from Page 439 containing Mannu Karnawat (WUX2230803)
        val snippet = """
            नपम: 
            ललग: 
            मकपन सनखयप: 
            नपतप कप नपम:
            आयप:  20 पपरष
            3/153
            मनप करनपवक
            सपदशरन करनपवक
            WUX2230803
            Photo is 
            Available
             255 
            आयप 1 जनवरर 2026 कक  अनपसपर पतष सनखयप : 439 / 863
        """.trimIndent()

        val results = VoterParser.parsePage(
            pageText = snippet,
            sourcePdf = "list.pdf",
            pageNumber = 439,
            voterListId = 123L
        )

        assertEquals("Expected 1 parsed voter", 1, results.size)

        val v = results[0].voter
        assertEquals("WUX2230803", v.epicNumber)
        assertEquals("WUX2230803", v.normalizedEpic)
        assertEquals(255, v.serialNumber)
        assertEquals("मन्नू करनावट", v.name)
        assertEquals("Father", v.relationship)
        assertEquals("सुदर्शन करनावट", v.relativeName)
        assertEquals(20, v.age)
        assertEquals("Male", v.gender)
        assertEquals("3/153", v.houseNumber)

        assertTrue(v.normalizedName.contains("मन्नू"))
        assertTrue(v.normalizedName.contains("करनावट"))
        assertTrue(v.normalizedName.contains("mannu"))
        assertTrue(v.normalizedName.contains("karnawat"))
    }

    @Test
    fun testRajasthanSecGlyphMap() {
        val glyphMap = com.keofi.poonamashishmehta_votergen.util.RajasthanSecGlyphMap
        assertEquals("ट", glyphMap.mapCode(0x53))
        assertEquals("न्न", glyphMap.mapCode(0x87))
        assertEquals("ू", glyphMap.mapCode(0x67))
        assertEquals("ा", glyphMap.mapCode(0x29))
        assertEquals("व", glyphMap.mapCode(0x32))
        assertEquals("क", glyphMap.mapCode(0x2B))
        assertEquals("र", glyphMap.mapCode(0x22))
        assertEquals("न", glyphMap.mapCode(0x20))
        assertEquals("म", glyphMap.mapCode(0x24))
        assertEquals("स", glyphMap.mapCode(0x3D))
        assertEquals("ु", glyphMap.mapCode(0x2E))
        assertEquals("द", glyphMap.mapCode(0x28))
        assertEquals("श", glyphMap.mapCode(0x3A))
        assertEquals("र्", glyphMap.mapCode(0x33))

        // Reordering test
        assertEquals("कि", glyphMap.normalizeDevanagari("िक"))
        assertEquals("सुदर्शन", glyphMap.normalizeDevanagari("सुदशर्न"))
    }

    @Test
    fun testSlipFileNameGeneration() {
        val slip1 = com.keofi.poonamashishmehta_votergen.data.slip.SlipData(
            voterName = "मन्नू करनावट",
            epicNumber = "WUX2230803"
        )
        val file1 = com.keofi.poonamashishmehta_votergen.share.SlipShareManager.buildSlipFileName(slip1, "pdf")
        assertEquals("Slip_Mannu_Karnawat_WUX2230803.pdf", file1)

        val slip2 = com.keofi.poonamashishmehta_votergen.data.slip.SlipData(
            voterName = "अक्षत मेहता",
            epicNumber = "WUX2231975"
        )
        val file2 = com.keofi.poonamashishmehta_votergen.share.SlipShareManager.buildSlipFileName(slip2, "pdf")
        assertEquals("Slip_Akshat_Mehta_WUX2231975.pdf", file2)

        val slip3 = com.keofi.poonamashishmehta_votergen.data.slip.SlipData(
            voterName = "Poonam Mehta",
            epicNumber = "WUX1234567"
        )
        val file3 = com.keofi.poonamashishmehta_votergen.share.SlipShareManager.buildSlipFileName(slip3, "png")
        assertEquals("Slip_Poonam_Mehta_WUX1234567.png", file3)

        // Ensure no underscore spam
        assertFalse(file1.contains("___"))
        assertFalse(file2.contains("___"))
        assertFalse(file3.contains("___"))

        // Sudarshan Karnawat test
        assertEquals("सुदर्शन करनावट", RajasthanSecDecoder.decodeHindi("सपदशरन कनपरवक"))
        assertEquals("सुदर्शन करनावट", RajasthanSecDecoder.decodeHindi("सपदशरन करनपवक"))
    }

    @Test
    fun testUniversalNameDecoding() {
        assertEquals("अजय सिंह", RajasthanSecDecoder.decodeHindi("अजय लसर"))
        assertEquals("अर्चना जैन", RajasthanSecDecoder.decodeHindi("अचरनप जसन"))
        assertEquals("अर्पित करनावट", RajasthanSecDecoder.decodeHindi("अरपत कनपरवक"))
        assertEquals("अंकित कर्णावत", RajasthanSecDecoder.decodeHindi("अनदकत करणपवत"))
        assertEquals("कमला देवी", RajasthanSecDecoder.decodeHindi("कमलप दकवर"))
        assertEquals("कोमल अरोड़ा", RajasthanSecDecoder.decodeHindi("कयमल अरयरप"))
        assertEquals("चतुर्वेदी", RajasthanSecDecoder.decodeHindi("चतपवरदर"))
        assertEquals("मकान संख्या", RajasthanSecDecoder.decodeHindi("मकपन सनखयप"))
        assertEquals("अक्षत मेहता", RajasthanSecDecoder.decodeHindi("अकत मकरतप"))
        assertEquals("मन्नू करनावट", RajasthanSecDecoder.decodeHindi("मनप करनपवक"))
        assertEquals("लालवानी", RajasthanSecDecoder.decodeHindi("लपलवपनर"))
        assertEquals("चंदनानी", RajasthanSecDecoder.decodeHindi("चनदनपनर"))
    }
}
