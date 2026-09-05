package com.keofi.poonamashishmehta_votergen.util

import java.util.regex.Pattern

/**
 * Universal Rajasthan SEC Devanagari Glyph Map for Municipal / Nagar Nigam rolls (e.g. Jaipur Ward 123).
 *
 * In SEC PDFs, font '/a' (AAAAAB+ArialUnicodeMS) embeds 148 custom glyphs mapped to character codes 0x20..0xB3.
 * The internal ToUnicode table contains erroneous mappings (e.g. 0x53 'ट' mapped to 'क', 0x29 'ा' to 'प',
 * 0x2E 'ु' to 'प', 0x87 'न्न' to 'न', 0x67 'ू' to 'प').
 *
 * This object directly maps the true glyph codes to their exact Devanagari characters and provides
 * standard typewriter-to-Unicode ligature normalization (chhoti 'i' matra and reph placement).
 */
object RajasthanSecGlyphMap {

    val GLYPH_MAP: Map<Int, String> = mapOf(
        0x20 to "न",
        0x21 to "ग",
        0x22 to "र",
        0x23 to "ि", // short i matra
        0x24 to "म",
        0x25 to "प",
        0x26 to "ि", // short i matra
        0x27 to "ष",
        0x28 to "द",
        0x29 to "ा", // aa matra
        0x2A to "ल",
        0x2B to "क",
        0x2C to "ज",
        0x2D to "य",
        0x2E to "ु", // u matra
        0x2F to "ी", // ee matra
        0x30 to "क्ष",
        0x31 to "ण",
        0x32 to "व",
        0x33 to "र्", // reph
        0x34 to "प्र",
        0x35 to "अ",
        0x36 to "ह",
        0x37 to "त",
        0x38 to "ि", // short i matra
        0x39 to "ं", // anusvara
        0x3A to "श",
        0x3B to "ी", // ee matra
        0x3C to "ड",
        0x3D to "स",
        0x3E to "े", // e matra
        0x3F to "द्र",
        0x40 to "ख",
        0x41 to "ए",
        0x42 to "ओं",
        0x43 to "आ",
        0x44 to "म्", // half ma
        0x45 to "भ",
        0x46 to "क्र",
        0x47 to "रू",
        0x48 to "स्त्र",
        0x49 to "ो", // o matra
        0x4A to "द्य",
        0x4B to "न्", // half na
        0x4C to "ध",
        0x4D to "ब",
        0x4E to "उ",
        0x4F to "च्च",
        0x50 to "ध्", // half dha
        0x51 to "ु", // u matra
        0x52 to "क्", // half ka
        0x53 to "ट",
        0x54 to "ृ", // ri matra
        0x55 to "िं", // i matra + bindi
        0x56 to "छ",
        0x57 to "झ",
        0x58 to "ठ",
        0x59 to "े", // e matra
        0x5A to "त्र",
        0x5B to "च",
        0x5C to "स्", // half sa
        0x5D to "ल्", // half la
        0x5E to "ई",
        0x5F to "त्", // half ta
        0x60 to "ु", // u matra
        0x61 to "ॉ", // chandra a matra
        0x62 to "ै", // ai matra
        0x63 to "श्व",
        0x64 to "ओ",
        0x65 to "हु",
        0x66 to "थ",
        0x67 to "ू", // oo matra
        0x68 to "ीं", // ee matra + bindi
        0x69 to "त्त",
        0x6A to "द्व",
        0x6B to "।",
        0x6C to "ज्", // half ja
        0x6D to "ष्ठ",
        0x6E to "श्र",
        0x6F to "ग्र",
        0x70 to "फ",
        0x71 to "व्य",
        0x72 to "श्", // half sha
        0x73 to "घ",
        0x74 to "ाँ", // chandra bindu
        0x75 to "हू",
        0x76 to "र्", // reph
        0x77 to "ख्", // half kha
        0x78 to "्र", // ra-kar
        0x79 to "ष्ट",
        0x7A to "श्व",
        0x7B to "औ",
        0x7C to "म",
        0x7D to "ड़",
        0x7E to "्",
        0x7F to "्",
        0x80 to "क्त",
        0x81 to "ऋ",
        0x82 to "ख़",
        0x83 to "ऐ",
        0x84 to "म्र",
        0x85 to "इ",
        0x86 to "ऑ",
        0x87 to "न्न",
        0x88 to "रु",
        0x89 to "ढ",
        0x8A to "द्ध",
        0x8B to "ठ",
        0x8C to "७",
        0x8D to "१",
        0x8E to "३",
        0x8F to "त्र",
        0x90 to "़", // nukta
        0x91 to "ज्ञ",
        0x92 to "स्त्र",
        0x93 to "ब्र",
        0x94 to "ट",
        0x95 to "क्ष",
        0x96 to "२",
        0x97 to "ह",
        0x98 to "फ",
        0x99 to "ु", // u matra
        0x9A to "र",
        0x9B to "ा",
        0x9C to "ह",
        0x9D to "ु",
        0x9E to "ल",
        0x9F to "ब",
        0xA0 to "ड",
        0xA1 to "ि",
        0xA2 to "य",
        0xA3 to "श",
        0xA4 to "ो",
        0xA5 to "भ",
        0xA6 to "ट",
        0xA7 to "ग",
        0xA8 to "०",
        0xA9 to "भ",
        0xAA to "ध",
        0xAB to "क़",
        0xAC to "स्त्र",
        0xAD to "ब्", // half ba
        0xAE to "ऊ",
        0xAF to "ण",
        0xB0 to "ह",
        0xB1 to "व",
        0xB2 to "ड्ड",
        0xB3 to "ड्ढ"
    )

    fun mapCode(code: Int): String? {
        return GLYPH_MAP[code]
    }

    /**
     * Reorders typewriter matra conventions into standard Devanagari Unicode:
     * 1. 'ि' (chhoti i matra) placed before a consonant/conjunct is moved after it.
     * 2. 'र्' (reph) placed after a consonant is moved to proper reph position.
     */
    fun normalizeDevanagari(text: String): String {
        if (text.isEmpty()) return text

        // 1. Chhoti i matra: 'ि' followed by consonant or consonant+halant+consonant
        // e.g. ि + क -> कि, ि + क + ् + ष -> क्षि
        var res = text.replace(Regex("ि([क-ह]्[क-ह]|[क-ह])"), "$1ि")

        // 2. Anusvara with i matra: 'िं' followed by consonant
        res = res.replace(Regex("िं([क-ह]्[क-ह]|[क-ह])"), "$1िं")

        // 3. Reph: in typewriter, 'र्' placed after a consonant -> move before: consonant + 'र्' -> 'र्' + consonant
        // (e.g. 'श' + 'र्' -> 'र्श' which in Unicode is र + ् + श)
        res = res.replace(Regex("([क-ह](?:[ािीुूेैोौृ]|िं|ीं)?)र्"), "र्$1")

        return res
    }
}
