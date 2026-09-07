package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.data.importer.SpreadsheetImportManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.BufferedReader
import java.io.StringReader

class SpreadsheetImportTest {

    @Test
    fun testColRefToIndex() {
        assertEquals(0, SpreadsheetImportManager.colRefToIndex("A1"))
        assertEquals(1, SpreadsheetImportManager.colRefToIndex("B1"))
        assertEquals(9, SpreadsheetImportManager.colRefToIndex("J1"))
        assertEquals(16, SpreadsheetImportManager.colRefToIndex("Q1"))
        assertEquals(26, SpreadsheetImportManager.colRefToIndex("AA1"))
    }

    @Test
    fun testCsvParserWithMultilineHeadersAndQuotes() {
        val csvData = """
"स्रोत फ़ाइल
(Source_File)","विधानसभा क्षेत्र
(Constituency)","नगर निकाय
(Local_Body)","वार्ड नं.
(Ward_No)","भाग संख्या
(Part_Booth_No)","अनुभाग / क्षेत्र
(Section_Name)","पृष्ठ संख्या
(Page_No)","क्रम संख्या
(Serial_No)","स्थिति कोड
(Status_Code)","पहचान पत्र क्रमांक
(EPIC_No)","मतदाता का नाम
(Voter_Name)","संबंध
(Relation_Type)","संबंधी का नाम
(Relative_Name)","मकान संख्या
(House_No)","आयु
(Age)","लिंग
(Gender)","स्थिति
(Status)"
pdf-1.pdf,53-आदर्श नगर,जयपुर,123,1,सैक्टर नं. 2क जवाहर नगर,3,1,R,WUX1472125,मोहित भार्गव,पिता,प्रभात भार्गव,2-1,25,पुरुष,Deleted
pdf-1.pdf,53-आदर्श नगर,जयपुर,123,1,सैक्टर नं. 2क जवाहर नगर,3,2,S,WUX1708742,पुनीत गुप्ता,पिता,राजेंद्र गुप्ता,2-62,42,पुरुष,Active
        """.trimIndent()

        val parsedRows = mutableListOf<List<String>>()
        val reader = BufferedReader(StringReader(csvData))
        SpreadsheetImportManager.parseCsv(reader) { _, row ->
            parsedRows.add(row)
        }

        assertEquals(3, parsedRows.size)
        val header = parsedRows[0]
        assertEquals(17, header.size)
        assertTrue(header[0].contains("Source_File"))
        assertTrue(header[9].contains("EPIC_No"))
        assertTrue(header[10].contains("Voter_Name"))

        val mapping = SpreadsheetImportManager.ColumnMapping.detect(header)
        assertEquals(9, mapping.epicNoIdx)
        assertEquals(10, mapping.voterNameIdx)
        assertEquals(11, mapping.relationTypeIdx)
        assertEquals(12, mapping.relativeNameIdx)
        assertEquals(13, mapping.houseNoIdx)
        assertEquals(14, mapping.ageIdx)
        assertEquals(15, mapping.genderIdx)
        assertEquals(16, mapping.statusIdx)

        val voter1 = mapping.toVoterEntity(parsedRows[1], listId = 42L, fallbackSource = "test.csv")
        assertEquals("WUX1472125", voter1.epicNumber)
        assertEquals("WUX1472125", voter1.normalizedEpic)
        assertEquals("मोहित भार्गव", voter1.name)
        assertEquals("मोहित भार्गव", voter1.nameHindi)
        assertEquals("पिता", voter1.relationship)
        assertEquals("प्रभात भार्गव", voter1.relativeName)
        assertEquals("2-1", voter1.houseNumber)
        assertEquals(25, voter1.age)
        assertEquals("पुरुष", voter1.gender)
        assertEquals("Deleted", voter1.status)
        assertEquals(1, voter1.serialNumber)
        assertEquals("1", voter1.partNumber)
        assertEquals(42L, voter1.voterListId)
        assertEquals(1.0f, voter1.confidence)

        val voter2 = mapping.toVoterEntity(parsedRows[2], listId = 42L, fallbackSource = "test.csv")
        assertEquals("WUX1708742", voter2.epicNumber)
        assertEquals("पुनीत गुप्ता", voter2.name)
        assertEquals("Active", voter2.status)
        assertEquals(42, voter2.age)
    }

    @Test
    fun testCsvParserWith18ColumnsAndPollingStation() {
        val csvData = """
"स्रोत फ़ाइल
(Source_File)","विधानसभा क्षेत्र
(Constituency)","नगर निकाय
(Local_Body)","वार्ड नं.
(Ward_No)","भाग संख्या
(Part_Booth_No)","मतदान केंद्र एवं पता
(Polling_Station)","अनुभाग / क्षेत्र
(Section_Name)","पृष्ठ संख्या
(Page_No)","क्रम संख्या
(Serial_No)","स्थिति कोड
(Status_Code)","पहचान पत्र क्रमांक
(EPIC_No)","मतदाता का नाम
(Voter_Name)","संबंध
(Relation_Type)","संबंधी का नाम
(Relative_Name)","मकान संख्या
(House_No)","आयु
(Age)","लिंग
(Gender)","स्थिति
(Status)"
ward_122.pdf,53-आदर्श नगर,जयपुर,122,1,1770 - उच्च माध्यमिक आदर्श विद्या मन्दिर दशहरा मैदान के सामने आदर्श नगर कमरा नं. 5,सैक्टर नं. 1 आदर्श नगर,3,1,,WUX1892702,अदिति जेसवानी,पिता,नरेंद्र जेसवानी,1ए,22,स्त्री,Active
ward_123.pdf,53-आदर्श नगर,जयपुर,123,1,,सैक्टर नं. 2क जवाहर नगर,3,1,S,WUX1708742,पुनीत गुप्ता,पिता,राजेंद्र गुप्ता,2-62,42,पुरुष,Active
        """.trimIndent()

        val parsedRows = mutableListOf<List<String>>()
        val reader = BufferedReader(StringReader(csvData))
        SpreadsheetImportManager.parseCsv(reader) { _, row ->
            parsedRows.add(row)
        }

        assertEquals(3, parsedRows.size)
        val header = parsedRows[0]
        assertEquals(18, header.size)

        val mapping = SpreadsheetImportManager.ColumnMapping.detect(header)
        assertEquals(4, mapping.partNoIdx)
        assertEquals(5, mapping.pollingStationIdx)
        assertEquals(6, mapping.sectionNameIdx)
        assertEquals(10, mapping.epicNoIdx)
        assertEquals(11, mapping.voterNameIdx)

        // Voter 1: Has Polling Station populated
        val voter1 = mapping.toVoterEntity(parsedRows[1], listId = 101L, fallbackSource = "ward_122.csv")
        assertEquals("WUX1892702", voter1.epicNumber)
        assertEquals("अदिति जेसवानी", voter1.name)
        assertEquals("1770 - उच्च माध्यमिक आदर्श विद्या मन्दिर दशहरा मैदान के सामने आदर्श नगर कमरा नं. 5", voter1.pollingStation)

        // Voter 2: Polling Station column is empty string "" -> must be null in entity (displayed as Not Available)
        val voter2 = mapping.toVoterEntity(parsedRows[2], listId = 102L, fallbackSource = "ward_123.csv")
        assertEquals("WUX1708742", voter2.epicNumber)
        assertEquals("पुनीत गुप्ता", voter2.name)
        org.junit.Assert.assertNull(voter2.pollingStation)
    }

    @Test
    fun testCsvParserHandlesUtf8Bom() {
        val bomData = "\uFEFFColA,ColB\nValA,ValB"
        val parsedRows = mutableListOf<List<String>>()
        val reader = BufferedReader(StringReader(bomData))
        SpreadsheetImportManager.parseCsv(reader) { _, row ->
            parsedRows.add(row)
        }

        assertEquals(2, parsedRows.size)
        assertEquals("ColA", parsedRows[0][0])
        assertEquals("ColB", parsedRows[0][1])
        assertEquals("ValA", parsedRows[1][0])
        assertEquals("ValB", parsedRows[1][1])
    }

    @Test
    fun testRealCsvFileParsing() {
        val file = listOf(
            java.io.File("../voter_list_template.csv"),
            java.io.File("../voter_list_master_ward_123.csv")
        ).firstOrNull { it.exists() } ?: return

        var rowCount = 0
        var mapping: SpreadsheetImportManager.ColumnMapping? = null
        var sampleVoter: com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity? = null

        val reader = java.io.BufferedReader(java.io.InputStreamReader(java.io.FileInputStream(file), java.nio.charset.StandardCharsets.UTF_8))
        SpreadsheetImportManager.parseCsv(reader) { idx, row ->
            if (idx == 0) {
                mapping = SpreadsheetImportManager.ColumnMapping.detect(row)
            } else if (idx <= 100) {
                rowCount++
                val voter = mapping!!.toVoterEntity(row, listId = 1L, fallbackSource = "ward_123.csv")
                assertTrue(voter.epicNumber.isNotBlank())
                assertTrue(voter.name.isNotBlank())
                if (idx == 1) {
                    sampleVoter = voter
                }
            }
        }

        assertTrue(rowCount >= 3)
        assertNotNull(sampleVoter)
        assertTrue(sampleVoter!!.epicNumber.isNotBlank())
        assertTrue(sampleVoter!!.name.isNotBlank())
    }

    @Test
    fun testRealXlsxFileParsing() {
        val file = java.io.File("../voter_list_master.xlsx")
        if (!file.exists()) return

        java.util.zip.ZipFile(file).use { zip ->
            val sheetEntry = zip.getEntry("xl/worksheets/sheet1.xml")
            assertNotNull(sheetEntry)

            val sharedStrings = mutableListOf<String>()
            val sstEntry = zip.getEntry("xl/sharedStrings.xml")
            if (sstEntry != null) {
                zip.getInputStream(sstEntry).use { input ->
                    SpreadsheetImportManager.parseSharedStrings(input, sharedStrings)
                }
            }

            var rowsRead = 0
            var sampleVoterName = ""
            var sampleEpic = ""

            zip.getInputStream(sheetEntry).use { input ->
                SpreadsheetImportManager.parseXlsxSheet(input, sharedStrings) { idx, row ->
                    if (idx == 1) {
                        sampleEpic = if (row.size > 9) row[9] else ""
                        sampleVoterName = if (row.size > 10) row[10] else ""
                    }
                    if (idx > 0 && idx <= 50) {
                        rowsRead++
                    }
                }
            }

            assertEquals(50, rowsRead)
            assertEquals("WUX1472125", sampleEpic)
            assertEquals("मोहित भार्गव", sampleVoterName)
        }
    }
}
