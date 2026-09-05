package com.keofi.poonamashishmehta_votergen

import com.keofi.poonamashishmehta_votergen.data.printer.EscPosAlign
import com.keofi.poonamashishmehta_votergen.data.printer.EscPosEncoder
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class EscPosEncoderTest {

    @Test
    fun testInitialize() {
        val bytes = EscPosEncoder().initialize().toByteArray()
        assertArrayEquals(byteArrayOf(0x1B, 0x40), bytes)
    }

    @Test
    fun testAlignment() {
        val centerBytes = EscPosEncoder().setAlignment(EscPosAlign.CENTER).toByteArray()
        assertArrayEquals(byteArrayOf(0x1B, 0x61, 0x01), centerBytes)
    }

    @Test
    fun testBold() {
        val boldOn = EscPosEncoder().setBold(true).toByteArray()
        assertArrayEquals(byteArrayOf(0x1B, 0x45, 0x01), boldOn)

        val boldOff = EscPosEncoder().setBold(false).toByteArray()
        assertArrayEquals(byteArrayOf(0x1B, 0x45, 0x00), boldOff)
    }

    @Test
    fun testFeedLines() {
        val feed = EscPosEncoder().feedLines(3).toByteArray()
        assertArrayEquals(byteArrayOf(0x1B, 0x64, 0x03), feed)
    }

    @Test
    fun testCut() {
        val cut = EscPosEncoder().cut(fullCut = false).toByteArray()
        assertArrayEquals(byteArrayOf(0x1D, 0x56, 0x01), cut)
    }
}
