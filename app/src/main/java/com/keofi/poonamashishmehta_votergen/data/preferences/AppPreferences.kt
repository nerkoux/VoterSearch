package com.keofi.poonamashishmehta_votergen.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voter_app_preferences")

enum class SlipType {
    CUSTOM_IMAGE,
    TEXT_ONLY
}

data class SlipSettings(
    val slipType: SlipType = SlipType.CUSTOM_IMAGE,
    val headerImageUri: String? = null,
    val candidateName: String = "Poonam Ashish Mehta",
    val tagline: String = "Voter Information Slip"
)

data class PrinterSettings(
    val selectedPrinterName: String? = "F2C CX588",
    val selectedPrinterAddress: String? = null,
    val paperWidthMm: Int = 58
)

class AppPreferences(private val context: Context) {

    private object Keys {
        val SLIP_TYPE = stringPreferencesKey("slip_type")
        val HEADER_IMAGE_URI = stringPreferencesKey("header_image_uri")
        val CANDIDATE_NAME = stringPreferencesKey("candidate_name")
        val TAGLINE = stringPreferencesKey("tagline")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PRINTER_NAME = stringPreferencesKey("printer_name")
        val PRINTER_ADDRESS = stringPreferencesKey("printer_address")
        val PAPER_WIDTH_MM = intPreferencesKey("paper_width_mm")
    }

    val slipSettingsFlow: Flow<SlipSettings> = context.dataStore.data.map { prefs ->
        val typeStr = prefs[Keys.SLIP_TYPE] ?: SlipType.CUSTOM_IMAGE.name
        val slipType = try {
            SlipType.valueOf(typeStr)
        } catch (_: Exception) {
            SlipType.CUSTOM_IMAGE
        }
        SlipSettings(
            slipType = slipType,
            headerImageUri = prefs[Keys.HEADER_IMAGE_URI],
            candidateName = prefs[Keys.CANDIDATE_NAME] ?: "Poonam Ashish Mehta",
            tagline = prefs[Keys.TAGLINE] ?: "Voter Information Slip"
        )
    }

    val printerSettingsFlow: Flow<PrinterSettings> = context.dataStore.data.map { prefs ->
        PrinterSettings(
            selectedPrinterName = prefs[Keys.PRINTER_NAME] ?: "F2C CX588",
            selectedPrinterAddress = prefs[Keys.PRINTER_ADDRESS],
            paperWidthMm = prefs[Keys.PAPER_WIDTH_MM] ?: 58
        )
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "SYSTEM"
    }

    suspend fun setSlipType(slipType: SlipType) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SLIP_TYPE] = slipType.name
        }
    }

    suspend fun setHeaderImageUri(uriString: String?) {
        context.dataStore.edit { prefs ->
            if (uriString != null) {
                prefs[Keys.HEADER_IMAGE_URI] = uriString
            } else {
                prefs.remove(Keys.HEADER_IMAGE_URI)
            }
        }
    }

    suspend fun setCandidateName(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CANDIDATE_NAME] = name
        }
    }

    suspend fun setTagline(tagline: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TAGLINE] = tagline
        }
    }

    suspend fun setPrinter(name: String?, address: String?) {
        context.dataStore.edit { prefs ->
            if (name != null) prefs[Keys.PRINTER_NAME] = name else prefs.remove(Keys.PRINTER_NAME)
            if (address != null) prefs[Keys.PRINTER_ADDRESS] = address else prefs.remove(Keys.PRINTER_ADDRESS)
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }
}
