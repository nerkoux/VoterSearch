package com.keofi.poonamashishmehta_votergen

import android.app.Application
import com.keofi.poonamashishmehta_votergen.data.db.AppDatabase
import com.keofi.poonamashishmehta_votergen.data.importer.PdfImportManager
import com.keofi.poonamashishmehta_votergen.data.preferences.AppPreferences
import com.keofi.poonamashishmehta_votergen.data.printer.PrinterManager
import com.keofi.poonamashishmehta_votergen.data.repository.VoterListRepository
import com.keofi.poonamashishmehta_votergen.data.repository.VoterRepository
import com.keofi.poonamashishmehta_votergen.data.slip.DigitalSlipRenderer
import com.keofi.poonamashishmehta_votergen.data.slip.ThermalSlipRenderer
import com.keofi.poonamashishmehta_votergen.share.SlipShareManager

class VoterApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var voterRepository: VoterRepository
        private set
    lateinit var voterListRepository: VoterListRepository
        private set
    lateinit var appPreferences: AppPreferences
        private set
    lateinit var pdfImportManager: PdfImportManager
        private set
    lateinit var spreadsheetImportManager: com.keofi.poonamashishmehta_votergen.data.importer.SpreadsheetImportManager
        private set
    lateinit var thermalSlipRenderer: ThermalSlipRenderer
        private set
    lateinit var digitalSlipRenderer: DigitalSlipRenderer
        private set
    lateinit var slipShareManager: SlipShareManager
        private set
    lateinit var printerManager: PrinterManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        voterRepository = VoterRepository(database.voterDao())
        voterListRepository = VoterListRepository(
            voterListDao = database.voterListDao(),
            voterDao = database.voterDao(),
            importJobDao = database.importJobDao()
        )
        appPreferences = AppPreferences(this)
        pdfImportManager = PdfImportManager(this, voterRepository, voterListRepository)
        spreadsheetImportManager = com.keofi.poonamashishmehta_votergen.data.importer.SpreadsheetImportManager(this, voterRepository, voterListRepository)

        thermalSlipRenderer = ThermalSlipRenderer(this)
        digitalSlipRenderer = DigitalSlipRenderer(this)
        slipShareManager = SlipShareManager(this, digitalSlipRenderer)
        printerManager = PrinterManager(this, appPreferences, thermalSlipRenderer)
    }

    companion object {
        lateinit var instance: VoterApp
            private set
    }
}
