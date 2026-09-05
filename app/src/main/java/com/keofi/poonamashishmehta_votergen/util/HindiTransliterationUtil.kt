package com.keofi.poonamashishmehta_votergen.util

import java.util.Locale

object HindiTransliterationUtil {

    private val COMMON_NAMES = mapOf(
        "akshat" to "अक्षत",
        "akshay" to "अक्षय",
        "mohit" to "मोहित",
        "puneet" to "पुनीत",
        "punit" to "पुनीत",
        "rohit" to "रोहित",
        "rahul" to "राहुल",
        "ram" to "राम",
        "sharma" to "शर्मा",
        "verma" to "वर्मा",
        "gupta" to "गुप्ता",
        "mehta" to "मेहता",
        "singh" to "सिंह",
        "kumar" to "कुमार",
        "kumari" to "कुमारी",
        "devi" to "देवी",
        "vijay" to "विजय",
        "ajay" to "अजय",
        "sangeeta" to "संगीता",
        "sangita" to "संगीता",
        "anurag" to "अनुराग",
        "sahu" to "साहू",
        "bhargav" to "भार्गव",
        "bhargava" to "भार्गव",
        "aman" to "अमन",
        "amit" to "अमित",
        "anil" to "अनिल",
        "sunil" to "सुनील",
        "suman" to "सुमन",
        "rekha" to "रेखा",
        "poonam" to "पूनम",
        "ashish" to "आशीष",
        "suresh" to "सुरेश",
        "dinesh" to "दिनेश",
        "mahesh" to "महेश",
        "mukesh" to "मुकेश",
        "rajesh" to "राजेश",
        "manoj" to "मनोज",
        "sanjay" to "संजय",
        "neha" to "नेहा",
        "priya" to "प्रिया",
        "pooja" to "पूजा",
        "puja" to "पूजा",
        "aarti" to "आरती",
        "arti" to "आरती",
        "deepak" to "दीपक",
        "dipak" to "दीपक",
        "vikas" to "विकास",
        "vikash" to "विकाश",
        "sachin" to "सचिन",
        "kavita" to "कविता",
        "kamla" to "कमला",
        "kamlesh" to "कमलेश",
        "radha" to "राधा",
        "krishna" to "कृष्णा",
        "santosh" to "संतोष",
        "rajendra" to "राजेंद्र",
        "sunita" to "सुनीता",
        "anita" to "अनीता",
        "geeta" to "गीता",
        "gita" to "गीता",
        "seema" to "सीमा",
        "sima" to "सीमा",
        "baby" to "बेबी",
        "samkit" to "समकित",
        "surana" to "सुराना",
        "surendra" to "सुरेन्द्र",
        "sonia" to "सोनिया",
        "soniya" to "सोनिया",
        "preeti" to "प्रीति",
        "priti" to "प्रीति",
        "poonihani" to "पूनिहाणी",
        "punihani" to "पूनिहाणी"
    )

    private val INITIAL_VOWELS = listOf(
        "aa" to "आ", "ee" to "ई", "ii" to "ई", "oo" to "ऊ", "uu" to "ऊ",
        "ai" to "ऐ", "au" to "औ", "ou" to "औ", "ae" to "ए",
        "a" to "अ", "i" to "इ", "u" to "उ", "e" to "ए", "o" to "ओ"
    )

    private val MATRAS = listOf(
        "aa" to "ा", "ee" to "ी", "ii" to "ी", "oo" to "ू", "uu" to "ू",
        "ai" to "ै", "au" to "ौ", "ou" to "ौ",
        "a" to "", "i" to "ि", "u" to "ु", "e" to "े", "o" to "ो"
    )

    private val CONSONANTS = listOf(
        "ksh" to "क्ष", "chh" to "छ", "shh" to "ष", "ch" to "च",
        "kh" to "ख", "gh" to "घ", "jh" to "झ", "th" to "थ",
        "dh" to "ध", "ph" to "फ", "bh" to "भ", "sh" to "श",
        "tr" to "त्र", "gy" to "ज्ञ", "jn" to "ज्ञ",
        "k" to "क", "g" to "ग", "j" to "ज", "t" to "त",
        "d" to "द", "n" to "न", "p" to "प", "b" to "ब",
        "m" to "म", "y" to "य", "r" to "र", "l" to "ल",
        "v" to "व", "w" to "व", "s" to "स", "h" to "ह",
        "f" to "फ", "z" to "ज़", "q" to "क", "x" to "क्स", "c" to "क"
    )

    private val DEVA_VOWELS = mapOf<Char, String>(
        'अ' to "a", 'आ' to "aa", 'इ' to "i", 'ई' to "ee", 'उ' to "u", 'ऊ' to "oo",
        'ऋ' to "ri", 'ए' to "e", 'ऐ' to "ai", 'ओ' to "o", 'औ' to "au"
    )

    private val DEVA_MATRAS = mapOf<Char, String>(
        'ा' to "a", 'ि' to "i", 'ी' to "ee", 'ु' to "u", 'ू' to "oo",
        'ृ' to "ri", 'े' to "e", 'ै' to "ai", 'ो' to "o", 'ौ' to "au",
        'ं' to "n", 'ँ' to "n", 'ः' to "h"
    )

    private val DEVA_CONSONANTS = mapOf<Char, String>(
        'क' to "k", 'ख' to "kh", 'ग' to "g", 'घ' to "gh", 'ङ' to "ng",
        'च' to "ch", 'छ' to "chh", 'ज' to "j", 'झ' to "jh", 'ञ' to "ny",
        'ट' to "t", 'ठ' to "th", 'ड' to "d", 'ढ' to "dh", 'ण' to "n",
        'त' to "t", 'थ' to "th", 'द' to "d", 'ध' to "dh", 'न' to "n",
        'प' to "p", 'फ' to "ph", 'ब' to "b", 'भ' to "bh", 'म' to "m",
        'य' to "y", 'र' to "r", 'ल' to "l", 'व' to "v",
        'श' to "sh", 'ष' to "sh", 'स' to "s", 'ह' to "h"
    )

    /**
     * Translates English / Latin phonetic text into Hindi Devanagari candidates.
     * e.g., "akshat" -> ["अक्षत"], "puneet" -> ["पुनीत"], "mohit" -> ["मोहित"]
     */
    fun latinToDevanagari(latinText: String): List<String> {
        val trimmed = latinText.trim().lowercase(Locale.ROOT)
        if (trimmed.isEmpty()) return emptyList()

        val words = trimmed.split(Regex("\\s+"))
        val candidateLists = words.map { word ->
            transliterateSingleWord(word)
        }

        // Combine words
        val primary = candidateLists.map { it.firstOrNull() ?: "" }.joinToString(" ")
        val results = mutableListOf<String>()
        if (primary.isNotBlank()) results.add(primary)

        // If any word had a secondary candidate, add it
        if (candidateLists.any { it.size > 1 }) {
            val secondary = candidateLists.map { if (it.size > 1) it[1] else it.firstOrNull() ?: "" }.joinToString(" ")
            if (secondary.isNotBlank() && secondary != primary) {
                results.add(secondary)
            }
        }

        return results
    }

    private fun transliterateSingleWord(word: String): List<String> {
        if (word.isEmpty()) return emptyList()

        COMMON_NAMES[word]?.let { return listOf(it) }

        val res = StringBuilder()
        var i = 0
        val n = word.length

        while (i < n) {
            // Check initial vowel
            if (i == 0 || (res.isNotEmpty() && res.last() == ' ')) {
                var matchedVowel = false
                for ((vStr, vChar) in INITIAL_VOWELS) {
                    if (word.startsWith(vStr, i)) {
                        res.append(vChar)
                        i += vStr.length
                        matchedVowel = true
                        break
                    }
                }
                if (matchedVowel) continue
            }

            // Check consonant
            var matchedConsonant = false
            for ((cStr, cChar) in CONSONANTS) {
                if (word.startsWith(cStr, i)) {
                    res.append(cChar)
                    i += cStr.length
                    matchedConsonant = true

                    // Check following matra
                    var matchedMatra = false
                    for ((mStr, mChar) in MATRAS) {
                        if (word.startsWith(mStr, i)) {
                            res.append(mChar)
                            i += mStr.length
                            matchedMatra = true
                            break
                        }
                    }

                    // If no vowel followed and another letter follows, add halant
                    if (!matchedMatra && i < n && word[i] !in "aeiou" && word[i].isLetter()) {
                        res.append('्')
                    }
                    break
                }
            }

            if (!matchedConsonant) {
                // Standalone vowel
                var matchedVowel = false
                for ((vStr, vChar) in INITIAL_VOWELS) {
                    if (word.startsWith(vStr, i)) {
                        res.append(vChar)
                        i += vStr.length
                        matchedVowel = true
                        break
                    }
                }
                if (!matchedVowel) {
                    res.append(word[i])
                    i++
                }
            }
        }

        val result = res.toString()
        return if (result.isNotBlank()) listOf(result) else emptyList()
    }

    /**
     * Translates Devanagari Hindi text to Latin phonetic English.
     * e.g., "अक्षत" -> "akshat", "मोहित भार्गव" -> "mohit bhargav"
     */
    fun devanagariToLatin(devanagariText: String): String {
        val prepared = devanagariText
            .replace("क़", "k")
            .replace("ख़", "kh")
            .replace("ग़", "gh")
            .replace("ज़", "z")
            .replace("ड़", "d")
            .replace("ढ़", "dh")
            .replace("फ़", "f")
            .replace("क्ष", "ksha")
            .replace("त्र", "tra")
            .replace("ज्ञ", "gya")

        val out = StringBuilder()
        var i = 0
        val n = prepared.length

        while (i < n) {
            val ch = prepared[i]
            when {
                ch in DEVA_VOWELS -> {
                    out.append(DEVA_VOWELS[ch])
                    i++
                }
                ch in DEVA_CONSONANTS -> {
                    val base = DEVA_CONSONANTS[ch] ?: ""
                    if (i + 1 < n) {
                        val nextCh = prepared[i + 1]
                        when {
                            nextCh == '्' -> { // Halant suppresses inherent 'a'
                                out.append(base)
                                i += 2
                            }
                            nextCh in DEVA_MATRAS -> {
                                out.append(base).append(DEVA_MATRAS[nextCh])
                                i += 2
                            }
                            nextCh == ' ' || nextCh in ",-/" -> { // Word end: schwa deletion
                                out.append(base)
                                i++
                            }
                            else -> {
                                out.append(base).append('a')
                                i++
                            }
                        }
                    } else {
                        // End of string: schwa deletion
                        out.append(base)
                        i++
                    }
                }
                ch in DEVA_MATRAS -> {
                    out.append(DEVA_MATRAS[ch])
                    i++
                }
                else -> {
                    out.append(ch)
                    i++
                }
            }
        }

        return out.toString().trim()
            .replace(Regex("\\s+"), " ")
    }
}
