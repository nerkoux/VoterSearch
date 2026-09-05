package com.keofi.poonamashishmehta_votergen.util

import java.util.Locale

object TextNormalizer {

    /**
     * Normalizes an EPIC / Voter ID:
     * Removes spaces, dashes, slashes, and turns into uppercase.
     * e.g., "ABC 1234567" -> "ABC1234567", "ABC-1234567" -> "ABC1234567"
     */
    fun normalizeEpic(epic: String?): String {
        if (epic.isNullOrBlank()) return ""
        return epic.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.ROOT)
    }

    /**
     * Normalizes a name for search index:
     * Lowercases, replaces multiple spaces with single space, strips punctuation.
     * Also keeps Hindi / Devanagari characters intact.
     */
    fun normalizeName(name: String?): String {
        if (name.isNullOrBlank()) return ""
        val cleaned = name.trim()
            .lowercase(Locale.ROOT)
            .replace(Regex("[\\p{Punct}&&[^'\\-]]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        val romanized = HindiTransliterationUtil.devanagariToLatin(cleaned)
        return if (romanized.isNotBlank() && romanized != cleaned) {
            "$cleaned $romanized"
        } else {
            cleaned
        }
    }

    /**
     * General normalization for search query:
     * Handles both EPIC-like query and name query.
     */
    fun normalize(input: String): String {
        val trimmed = input.trim()
        val epicNormalized = normalizeEpic(trimmed)
        // An EPIC contains digits and is typically at least 5 alphanumeric characters
        val hasDigits = epicNormalized.any { it.isDigit() }
        if (epicNormalized.isNotEmpty() && epicNormalized.length >= 5 && hasDigits && epicNormalized.matches(Regex("^[A-Z0-9]+$"))) {
            return epicNormalized
        }
        return normalizeName(trimmed)
    }
}
