package com.keofi.poonamashishmehta_votergen.data.parser

import com.keofi.poonamashishmehta_votergen.data.db.entity.VoterEntity
import com.keofi.poonamashishmehta_votergen.util.RajasthanSecDecoder
import com.keofi.poonamashishmehta_votergen.util.TextNormalizer
import java.util.Locale

data class ParsedVoterResult(
    val voter: VoterEntity,
    val isHighConfidence: Boolean
)

object VoterParser {

    // Common Indian EPIC patterns: 3 letters + 7 digits (e.g. ABC1234567, WDX1452637) or state codes (e.g. RJ/06/042/336003)
    val EPIC_REGEX = Regex("\\b([A-Z]{3}[0-9]{7}|[A-Z]{2}/[0-9]{2}/[0-9]{2,3}/[0-9]{6,7}|[A-Z]{2,4}[0-9]{6,8})\\b", RegexOption.IGNORE_CASE)

    // Standard Name patterns: English and Hindi
    private val NAME_REGEX = Regex("(?:^|[\\r\\n])\\s*(?:Elector(?:'s)? Name|Voter Name|मतदाता का नाम|नाम|Name)\\s*[:\\-]?\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE)

    // Standard Relative patterns: Father's, Husband's, Mother's, Other's
    private val FATHER_REGEX = Regex("(?:^|[\\r\\n])\\s*(?:Father(?:'s)? Name|पिता का नाम)\\s*[:\\-]?\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE)
    private val HUSBAND_REGEX = Regex("(?:^|[\\r\\n])\\s*(?:Husband(?:'s)? Name|पति का नाम)\\s*[:\\-]?\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE)
    private val MOTHER_REGEX = Regex("(?:^|[\\r\\n])\\s*(?:Mother(?:'s)? Name|माता का नाम)\\s*[:\\-]?\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE)
    private val RELATIVE_GENERIC_REGEX = Regex("(?:^|[\\r\\n])\\s*(?:Relative(?:'s)? Name|संबंधी का नाम)\\s*[:\\-]?\\s*([^\\r\\n]+)", RegexOption.IGNORE_CASE)

    // Standard House number patterns
    private val HOUSE_REGEX = Regex("(?:House (?:No|Number)|मकान (?:संख्या|नं))\\s*[:\\-]?\\s*([A-Za-z0-9\\-/]+)", RegexOption.IGNORE_CASE)

    // Standard Age patterns
    private val AGE_REGEX = Regex("(?:Age|आयु|उम्र)\\s*[:\\-]?\\s*([0-9]{1,3})", RegexOption.IGNORE_CASE)

    // Standard Gender patterns
    private val GENDER_MALE_REGEX = Regex("(?:Gender|Sex|लिंग)\\s*[:\\-]?\\s*(?:M|Male|पुरुष|पु)", RegexOption.IGNORE_CASE)
    private val GENDER_FEMALE_REGEX = Regex("(?:Gender|Sex|लिंग)\\s*[:\\-]?\\s*(?:F|Female|महिला|स्त्री|म)", RegexOption.IGNORE_CASE)

    // Part number / Polling station header patterns
    private val PART_REGEX = Regex("(?:Part (?:No|Number)|भाग संख्या|मपग सनखयप)\\s*[:\\-]?\\s*([0-9A-Za-z\\-/]+)", RegexOption.IGNORE_CASE)
    private val POLLING_STATION_REGEX = Regex("(?:Polling Station|मतदान केंद्र)\\s*[:\\-]?\\s*([^\\n\\r]+)", RegexOption.IGNORE_CASE)

    /**
     * Parses a page's extracted or OCR text into structured VoterEntity records.
     * Automatically differentiates between Rajasthan SEC municipal format and standard ECI formats.
     */
    fun parsePage(
        pageText: String,
        sourcePdf: String,
        pageNumber: Int,
        voterListId: Long,
        defaultPartNumber: String? = null,
        defaultPollingStation: String? = null
    ): List<ParsedVoterResult> {
        if (pageText.isBlank()) return emptyList()

        // 1. Detect if this is a Rajasthan State Election Commission (SEC) page
        if (RajasthanSecDecoder.isRajasthanSecText(pageText)) {
            val secResults = parseRajasthanSecPage(
                pageText = pageText,
                sourcePdf = sourcePdf,
                pageNumber = pageNumber,
                voterListId = voterListId,
                defaultPartNumber = defaultPartNumber,
                defaultPollingStation = defaultPollingStation
            )
            if (secResults.isNotEmpty()) {
                return secResults
            }
        }

        // 2. Fallback to Standard ECI Parser for ECI assembly / parliamentary rolls
        return parseStandardEciPage(
            pageText = pageText,
            sourcePdf = sourcePdf,
            pageNumber = pageNumber,
            voterListId = voterListId,
            defaultPartNumber = defaultPartNumber,
            defaultPollingStation = defaultPollingStation
        )
    }

    /**
     * Specialized high-accuracy parser for Rajasthan State Election Commission (SEC) rolls
     * (such as Jaipur Nagar Nigam Ward 123).
     */
    private fun parseRajasthanSecPage(
        pageText: String,
        sourcePdf: String,
        pageNumber: Int,
        voterListId: Long,
        defaultPartNumber: String?,
        defaultPollingStation: String?
    ): List<ParsedVoterResult> {
        // Detect part number from header (e.g., "मपग सनखयप : 13" or "भाग संख्या : 13")
        val detectedPartNumber = PART_REGEX.find(pageText)?.groupValues?.getOrNull(1)?.trim() ?: defaultPartNumber
        val detectedPollingStation = POLLING_STATION_REGEX.find(pageText)?.groupValues?.getOrNull(1)?.trim() ?: defaultPollingStation

        // Detect locality / section from header
        var detectedLocality: String? = null
        val headerLines = pageText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        for (i in 0 until minOf(15, headerLines.size)) {
            val l = headerLines[i]
            if (l.contains("नगरपपनलकप चपनपव") || l.contains("निर्वाचक नामावली")) {
                if (i + 1 < headerLines.size) {
                    val candidate = headerLines[i + 1]
                    if (!candidate.contains("नपम:") && !candidate.contains("नाम:") && candidate.length > 3) {
                        detectedLocality = RajasthanSecDecoder.decodeHindi(candidate)
                    }
                }
                break
            }
        }

        // Split text by voter card boundary: "Photo is"
        val chunks = pageText.split(Regex("Photo is\\s*\\n(?:Available|Not Available|Deleted)?", RegexOption.IGNORE_CASE))
        if (chunks.size < 2) return emptyList()

        val results = mutableListOf<ParsedVoterResult>()

        for (idx in 0 until chunks.size - 1) {
            val chunk = chunks[idx]
            val nextChunk = chunks[idx + 1]

            // Serial number is found right at the beginning of the next chunk (e.g. " 1 S" or " 24 S")
            val snoMatch = Regex("^\\s*([0-9]{1,5})\\s*([A-Z])?").find(nextChunk)
            val sno = snoMatch?.groupValues?.getOrNull(1)?.toIntOrNull()

            // EPIC number
            val epicMatch = EPIC_REGEX.find(chunk)
            val rawEpic = epicMatch?.groupValues?.getOrNull(1) ?: ""
            val normalizedEpic = TextNormalizer.normalizeEpic(rawEpic)

            // Age & Gender (handles both SEC font variants: आयप/आजप and पपरष/सर/सल)
            val ageMatch = Regex("(?:आयप|आजप|आयु|उम्र)\\s*[:\\-]?\\s*([0-9]{1,3})\\s*(पपरष|सर|सल|पुरुष|महिला|स्त्री)", RegexOption.IGNORE_CASE).find(chunk)
            val age = ageMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
            val gender = when {
                ageMatch != null && (ageMatch.groupValues[2].contains("पपरष") || ageMatch.groupValues[2].contains("पुरुष")) -> "Male"
                ageMatch != null -> "Female"
                else -> null
            }

            // Relationship (handles both variants: पनत/पति, मपतप/माता, अनय/अन्य, नपतर/नपतप/पिता)
            val relationship = when {
                chunk.contains("पनत") || chunk.contains("पति") -> "Husband"
                chunk.contains("मपतप") || chunk.contains("माता") -> "Mother"
                chunk.contains("अनय") || chunk.contains("अन्य") -> "Other"
                else -> "Father"
            }

            // House, Name, Relative Name lines between age line and EPIC
            var rawHouse = ""
            var rawName = ""
            var rawRelative = ""

            if (ageMatch != null) {
                val endPos = epicMatch?.range?.first ?: chunk.length
                val sub = chunk.substring(ageMatch.range.last + 1, endPos).trim()
                val subLines = sub.lines().map { it.trim() }.filter { it.isNotEmpty() }
                if (subLines.isNotEmpty()) rawHouse = subLines[0]
                if (subLines.size > 1) rawName = subLines[1]
                if (subLines.size > 2) rawRelative = subLines[2]
            }

            // Decode legacy SEC font representations to standard Hindi Unicode
            val decodedName = RajasthanSecDecoder.decodeHindi(rawName)
            val decodedRelative = RajasthanSecDecoder.decodeHindi(rawRelative)
            val decodedHouse = RajasthanSecDecoder.decodeHouseNumber(rawHouse)

            // Status (Check for DELETED watermark or stamp)
            val isDeleted = chunk.contains("DELETED", ignoreCase = true) ||
                    chunk.contains("ववरोपपत") ||
                    chunk.contains("ननरसत")
            val status = if (isDeleted) "Deleted" else "Active"

            // A valid voter card should have at least an EPIC OR (age and serial number)
            if (normalizedEpic.isNotEmpty() || (age != null && sno != null)) {
                val displayName = if (decodedName.isNotBlank()) decodedName else (rawName.ifBlank { "Voter #${sno ?: pageNumber}" })
                val confidence = if (normalizedEpic.isNotEmpty() && age != null) 0.95f else 0.70f

                // Formulate search key covering decoded Hindi, raw string, and normalized terms
                val transliteratedName = RajasthanSecDecoder.getTransliterations(displayName)
                val transliteratedRel = if (decodedRelative.isNotBlank()) RajasthanSecDecoder.getTransliterations(decodedRelative) else ""

                val searchKey = buildString {
                    append(displayName.lowercase(Locale.ROOT))
                    append(" ")
                    if (transliteratedName.isNotBlank()) append(transliteratedName).append(" ")
                    if (rawName.isNotBlank()) append(rawName.lowercase(Locale.ROOT)).append(" ")
                    if (decodedRelative.isNotBlank()) append(decodedRelative.lowercase(Locale.ROOT)).append(" ")
                    if (transliteratedRel.isNotBlank()) append(transliteratedRel).append(" ")
                }.trim()

                val voter = VoterEntity(
                    epicNumber = rawEpic,
                    normalizedEpic = normalizedEpic,
                    name = displayName,
                    nameHindi = decodedName.ifBlank { null },
                    normalizedName = TextNormalizer.normalizeName(searchKey),
                    relativeName = decodedRelative.ifBlank { rawRelative.ifBlank { null } },
                    relationship = relationship,
                    age = age,
                    gender = gender,
                    houseNumber = decodedHouse.ifBlank { rawHouse.ifBlank { null } },
                    partNumber = detectedPartNumber,
                    serialNumber = sno,
                    address = detectedLocality,
                    pollingStation = detectedPollingStation,
                    status = status,
                    voterListId = voterListId,
                    sourcePdf = sourcePdf,
                    sourcePage = pageNumber,
                    rawOcrText = chunk.trim(),
                    confidence = confidence
                )

                results.add(ParsedVoterResult(voter = voter, isHighConfidence = confidence >= 0.75f))
            }
        }

        return results
    }

    /**
     * Standard ECI 3-column / line parser for standard Election Commission of India rolls.
     */
    private fun parseStandardEciPage(
        pageText: String,
        sourcePdf: String,
        pageNumber: Int,
        voterListId: Long,
        defaultPartNumber: String?,
        defaultPollingStation: String?
    ): List<ParsedVoterResult> {
        val detectedPartNumber = PART_REGEX.find(pageText)?.groupValues?.getOrNull(1)?.trim() ?: defaultPartNumber
        val detectedPollingStation = POLLING_STATION_REGEX.find(pageText)?.groupValues?.getOrNull(1)?.trim() ?: defaultPollingStation

        val results = mutableListOf<ParsedVoterResult>()
        val blocks = splitIntoVoterBlocks(pageText)

        for (block in blocks) {
            val parsed = parseSingleStandardBlock(
                blockText = block,
                sourcePdf = sourcePdf,
                pageNumber = pageNumber,
                voterListId = voterListId,
                partNumber = detectedPartNumber,
                pollingStation = detectedPollingStation
            )
            if (parsed != null) {
                results.add(parsed)
            }
        }

        return results
    }

    private fun splitIntoVoterBlocks(pageText: String): List<String> {
        val lines = pageText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val blocks = mutableListOf<String>()
        var currentBlock = StringBuilder()

        // Check if page has EPIC matches to anchor blocks
        val epicMatches = EPIC_REGEX.findAll(pageText).toList()
        if (epicMatches.size >= 2) {
            var lastIdx = 0
            for (match in epicMatches) {
                val start = match.range.first
                if (start > lastIdx) {
                    val prevText = pageText.substring(lastIdx, start).trim()
                    if (prevText.isNotBlank()) {
                        blocks.add(prevText)
                    }
                }
                lastIdx = start
            }
            if (lastIdx < pageText.length) {
                val tail = pageText.substring(lastIdx).trim()
                if (tail.isNotBlank()) blocks.add(tail)
            }
            return blocks.filter { it.length > 20 }
        }

        // Fallback: chunk by lines
        for (line in lines) {
            if (line.matches(Regex("^[0-9]{1,4}\\b.*")) && currentBlock.length > 50) {
                blocks.add(currentBlock.toString())
                currentBlock = StringBuilder()
            }
            currentBlock.append(line).append("\n")
        }
        if (currentBlock.isNotBlank()) {
            blocks.add(currentBlock.toString())
        }

        return if (blocks.isEmpty()) listOf(pageText) else blocks
    }

    private fun parseSingleStandardBlock(
        blockText: String,
        sourcePdf: String,
        pageNumber: Int,
        voterListId: Long,
        partNumber: String?,
        pollingStation: String?
    ): ParsedVoterResult? {
        val epicMatch = EPIC_REGEX.find(blockText)
        val rawEpic = epicMatch?.groupValues?.getOrNull(1) ?: ""
        val normalizedEpic = TextNormalizer.normalizeEpic(rawEpic)

        // Name
        var name = ""
        var nameHindi: String? = null
        val nameMatch = NAME_REGEX.find(blockText)
        if (nameMatch != null) {
            val candidate = cleanNameString(nameMatch.groupValues[1].trim())
            if (isHindiText(candidate)) {
                nameHindi = candidate
                name = candidate
            } else {
                name = candidate
            }
        }

        // Relative & Relationship
        var relativeName: String? = null
        var relationship: String? = null

        val fatherMatch = FATHER_REGEX.find(blockText)
        val husbandMatch = HUSBAND_REGEX.find(blockText)
        val motherMatch = MOTHER_REGEX.find(blockText)
        val genericRelativeMatch = RELATIVE_GENERIC_REGEX.find(blockText)

        when {
            fatherMatch != null -> {
                relationship = "Father"
                relativeName = cleanNameString(fatherMatch.groupValues[1].trim())
            }
            husbandMatch != null -> {
                relationship = "Husband"
                relativeName = cleanNameString(husbandMatch.groupValues[1].trim())
            }
            motherMatch != null -> {
                relationship = "Mother"
                relativeName = cleanNameString(motherMatch.groupValues[1].trim())
            }
            genericRelativeMatch != null -> {
                relationship = "Relative"
                relativeName = cleanNameString(genericRelativeMatch.groupValues[1].trim())
            }
        }

        // Age
        val ageMatch = AGE_REGEX.find(blockText)
        val age = ageMatch?.groupValues?.getOrNull(1)?.toIntOrNull()

        // Gender
        val gender = when {
            GENDER_MALE_REGEX.containsMatchIn(blockText) -> "Male"
            GENDER_FEMALE_REGEX.containsMatchIn(blockText) -> "Female"
            else -> null
        }

        // House number
        val houseMatch = HOUSE_REGEX.find(blockText)
        val houseNumber = houseMatch?.groupValues?.getOrNull(1)?.trim()

        // Serial number
        val serialNumber = extractSerialNumber(blockText)

        // If we found neither an EPIC nor a Name, ignore noise
        if (normalizedEpic.isEmpty() && name.isEmpty() && nameHindi.isNullOrEmpty()) {
            return null
        }

        // Compute confidence score
        var confidence = 0.0f
        if (normalizedEpic.isNotEmpty()) confidence += 0.40f
        if (name.isNotEmpty() || !nameHindi.isNullOrEmpty()) confidence += 0.30f
        if (age != null && age in 18..120) confidence += 0.15f
        if (gender != null) confidence += 0.10f
        if (serialNumber != null) confidence += 0.05f

        val isHighConfidence = confidence >= 0.75f && normalizedEpic.isNotEmpty()

        val displayName = when {
            name.isNotEmpty() -> name
            !nameHindi.isNullOrEmpty() -> nameHindi
            else -> "Voter #${serialNumber ?: pageNumber}"
        }

        val voter = VoterEntity(
            epicNumber = rawEpic,
            normalizedEpic = normalizedEpic,
            name = displayName,
            nameHindi = nameHindi,
            normalizedName = TextNormalizer.normalizeName(displayName),
            relativeName = relativeName,
            relationship = relationship,
            age = age,
            gender = gender,
            houseNumber = houseNumber,
            partNumber = partNumber,
            serialNumber = serialNumber,
            address = null,
            pollingStation = pollingStation,
            status = "Active",
            voterListId = voterListId,
            sourcePdf = sourcePdf,
            sourcePage = pageNumber,
            rawOcrText = blockText.trim(),
            confidence = confidence
        )

        return ParsedVoterResult(voter = voter, isHighConfidence = isHighConfidence)
    }

    private fun extractSerialNumber(blockText: String): Int? {
        val lines = blockText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        for (line in lines.take(3)) {
            val numMatch = Regex("^([0-9]{1,5})$").find(line)
            if (numMatch != null) {
                return numMatch.groupValues[1].toIntOrNull()
            }
        }
        return null
    }

    private fun cleanNameString(raw: String): String {
        return raw.replace(Regex("[:\\-/,|]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isHindiText(text: String): Boolean {
        return text.any { it in '\u0900'..'\u097F' }
    }
}
