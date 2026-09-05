package com.keofi.poonamashishmehta_votergen.util

import android.content.Context
import android.graphics.Typeface

/**
 * Unified Font Manager providing robust Devanagari typographic rendering across both
 * Microsoft Mangal (official electoral standard) and Tiro Devanagari Hindi font families.
 *
 * Implements a prioritized fallback chain:
 * 1. Mangal 2016 / Mangal Bold (classic Indian government voter roll font)
 * 2. Mangal historical releases (2009, 2007, 2005)
 * 3. Tiro Devanagari Hindi (Google / Tiro Typeworks high-legibility display font)
 * 4. Android System Default
 */
object DevanagariFontManager {

    private const val FONT_MANGAL_2016 = "fonts/mangal16jul2016.ttf"
    private const val FONT_MANGAL_BOLD = "fonts/mangalb.ttf"
    private const val FONT_MANGAL_2009 = "fonts/mangal11jun2009.ttf"
    private const val FONT_MANGAL_2007 = "fonts/mangal14april2007.ttf"
    private const val FONT_MANGAL_2005 = "fonts/mangal30nov2005.ttf"
    private const val FONT_TIRO_REGULAR = "fonts/TiroDevanagariHindi-Regular.ttf"
    private const val FONT_TIRO_ITALIC  = "fonts/TiroDevanagariHindi-Italic.ttf"

    @Volatile
    private var cachedRegular: Typeface? = null

    @Volatile
    private var cachedBold: Typeface? = null

    @Volatile
    private var cachedTiro: Typeface? = null

    /**
     * Returns the primary Devanagari regular typeface (Mangal or Tiro with fallback).
     */
    fun getRegularTypeface(context: Context): Typeface {
        cachedRegular?.let { return it }
        synchronized(this) {
            cachedRegular?.let { return it }
            val tf = tryLoadTypeface(context, FONT_MANGAL_2016)
                ?: tryLoadTypeface(context, FONT_TIRO_REGULAR)
                ?: tryLoadTypeface(context, FONT_MANGAL_2009)
                ?: tryLoadTypeface(context, FONT_MANGAL_2007)
                ?: tryLoadTypeface(context, FONT_MANGAL_2005)
                ?: Typeface.DEFAULT
            cachedRegular = tf
            return tf
        }
    }

    /**
     * Returns the primary Devanagari bold typeface.
     */
    fun getBoldTypeface(context: Context): Typeface {
        cachedBold?.let { return it }
        synchronized(this) {
            cachedBold?.let { return it }
            val tf = tryLoadTypeface(context, FONT_MANGAL_BOLD)
                ?: Typeface.create(getRegularTypeface(context), Typeface.BOLD)
            cachedBold = tf
            return tf
        }
    }

    /**
     * Returns Tiro Devanagari Hindi typeface specifically.
     */
    fun getTiroTypeface(context: Context): Typeface {
        cachedTiro?.let { return it }
        synchronized(this) {
            cachedTiro?.let { return it }
            val tf = tryLoadTypeface(context, FONT_TIRO_REGULAR)
                ?: getRegularTypeface(context)
            cachedTiro = tf
            return tf
        }
    }

    private fun tryLoadTypeface(context: Context, assetPath: String): Typeface? {
        return try {
            Typeface.createFromAsset(context.assets, assetPath)
        } catch (_: Exception) {
            null
        }
    }
}
