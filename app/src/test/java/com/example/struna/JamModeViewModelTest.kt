package com.example.struna

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class JamModeViewModelTest {

    private lateinit var viewModel: JamModeViewModel

    @Before
    fun setUp() {
        viewModel = JamModeViewModel()
    }

    @Test
    fun testFreqToNoteName_A4() {
        val note = viewModel.freqToNoteName(440f)
        assertEquals("A", note)
    }

    @Test
    fun testFreqToNoteName_C4() {
        val note = viewModel.freqToNoteName(261.63f)
        assertEquals("C", note)
    }

    @Test
    fun testFreqToNoteName_E4() {
        val note = viewModel.freqToNoteName(329.63f)
        assertEquals("E", note)
    }

    @Test
    fun testFreqToNoteName_NegativeFrequency() {
        val note = viewModel.freqToNoteName(-10f)
        assertEquals("—", note)
    }
}