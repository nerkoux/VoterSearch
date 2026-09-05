package com.keofi.poonamashishmehta_votergen.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.keofi.poonamashishmehta_votergen.R

val MangalFontFamily = FontFamily(
    Font(R.font.mangal_regular, FontWeight.Normal),
    Font(R.font.mangal_regular, FontWeight.Medium),
    Font(R.font.mangal_bold, FontWeight.Bold)
)

val TiroDevanagari = FontFamily(
    Font(R.font.tiro_devanagari_hindi_regular, FontWeight.Normal),
    Font(R.font.tiro_devanagari_hindi_regular, FontWeight.Medium),
    Font(R.font.tiro_devanagari_hindi_regular, FontWeight.Bold),
    Font(R.font.tiro_devanagari_hindi_italic, FontWeight.Normal, FontStyle.Italic)
)

val Typography = Typography(
    // Screen title: 24sp / medium
    titleLarge = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp,
        color = PrimaryText
    ),
    // Section title: 18sp / medium
    titleMedium = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = PrimaryText
    ),
    titleSmall = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = PrimaryText
    ),
    // Body: 16sp
    bodyLarge = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
        color = PrimaryText
    ),
    // Body: 15sp
    bodyMedium = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
        color = PrimaryText
    ),
    // Secondary information: 13sp
    bodySmall = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp,
        color = SecondaryText
    ),
    // Buttons: 15-16sp / medium
    labelLarge = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = PrimaryText
    ),
    labelMedium = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        color = PrimaryText
    ),
    labelSmall = TextStyle(
        fontFamily = TiroDevanagari,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = SecondaryText
    )
)
