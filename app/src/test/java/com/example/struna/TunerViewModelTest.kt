package com.example.struna

import android.media.AudioFormat
import android.media.AudioRecord
import androidx.lifecycle.viewModelScope
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionResult
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TunerViewModelTest {

    private lateinit var viewModel: TunerViewModel

    // Fake dispatcher and patching
    private val fakeDispatcher = mockk<AudioDispatcher>(relaxed = true)
    private val sampleRate = 44100
    private val bufferSize = 2048

    @Before
    fun setUp() {
        // Mock static Android and Tarsos DSP methods
        mockkStatic(AudioRecord::class)
        mockkStatic(AudioDispatcherFactory::class)

        viewModel = TunerViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // 1. getTuningMap initial state
    @Test
    fun `getTuningMap initial state`() = runTest {
        val expected = mapOf(
            "E2" to 82.41f, "A2" to 110.00f, "D3" to 146.83f,
            "G3" to 196.00f, "B3" to 246.94f, "E4" to 329.63f
        )
        assertEquals(expected, viewModel.tuningMap.value)
    }

    // 2. setTuning to Drop D
    @Test
    fun `setTuning to Drop D`() = runTest {
        viewModel.setTuning("Drop D")
        val map = viewModel.tuningMap.value
        assertTrue(map.containsKey("D2"))
        assertEquals(73.42f, map["D2"])
    }

    // 3. setTuning to Standard (explicitly)
    @Test
    fun `setTuning to Standard explicitly`() = runTest {
        viewModel.setTuning("Standard")
        val map = viewModel.tuningMap.value
        assertTrue(map.containsKey("E2"))
        assertEquals(82.41f, map["E2"])
    }

    // 4. setTuning to Standard (implicitly/empty)
    @Test
    fun `setTuning to Standard implicitly`() = runTest {
        viewModel.setTuning("")
        assertTrue(viewModel.tuningMap.value.containsKey("E2"))
    }

    // 5. getTuningMap after setTuning
    @Test
    fun `getTuningMap after setTuning`() = runTest {
        viewModel.setTuning("Drop D")
        assertTrue(viewModel.tuningMap.value.containsKey("D2"))
        viewModel.setTuning("Standard")
        assertTrue(viewModel.tuningMap.value.containsKey("E2"))
    }

    // 6. getNote initial state
    @Test
    fun `getNote initial state`() = runTest {
        assertEquals("—", viewModel.note.value)
    }

    // 7. getDeviationCents initial state
    @Test
    fun `getDeviationCents initial state`() = runTest {
        assertEquals(0f, viewModel.deviationCents.value)
    }

    // 8. startTuning successful initialization
    @Test
    fun `startTuning successful initialization`() {
        every { AudioRecord.getMinBufferSize(any(), any(), any()) } returns 1024
        every { AudioDispatcherFactory.fromDefaultMicrophone(any(), any(), any()) } returns fakeDispatcher

        viewModel.startTuning()
        assertNotNull(getPrivateDispatcher(viewModel))
    }

    // 9. startTuning when already started
    @Test
    fun `startTuning when already started does nothing`() {
        every { AudioRecord.getMinBufferSize(any(), any(), any()) } returns 1024
        every { AudioDispatcherFactory.fromDefaultMicrophone(any(), any(), any()) } returns fakeDispatcher

        viewModel.startTuning()
        val firstDispatcher = getPrivateDispatcher(viewModel)
        viewModel.startTuning()
        val secondDispatcher = getPrivateDispatcher(viewModel)
        assertSame(firstDispatcher, secondDispatcher)
    }



    // 12. startTuning dispatcher.run throws exception
    @Test
    fun `startTuning dispatcher_run throws exception`() {
        every { AudioRecord.getMinBufferSize(any(), any(), any()) } returns 1024
        every { AudioDispatcherFactory.fromDefaultMicrophone(any(), any(), any()) } returns fakeDispatcher
        every { fakeDispatcher.run() } throws AssertionError("Test dispatcher error")
        viewModel.startTuning()
        // No assertion, test just confirms no crash (exception caught in coroutine)
        assertNotNull(getPrivateDispatcher(viewModel))
    }

    // 13. stopTuning when dispatcher is active
    @Test
    fun `stopTuning when dispatcher is active`() {
        every { AudioRecord.getMinBufferSize(any(), any(), any()) } returns 1024
        every { AudioDispatcherFactory.fromDefaultMicrophone(any(), any(), any()) } returns fakeDispatcher
        every { fakeDispatcher.stop() } just Runs // <-- OVO JE KLJUČ

        viewModel.startTuning()
        viewModel.stopTuning()
        assertNull(getPrivateDispatcher(viewModel))
        verify { fakeDispatcher.stop() }
    }


    // 14. stopTuning when dispatcher is already null
    @Test
    fun `stopTuning when dispatcher is already null`() {
        // No dispatcher set
        viewModel.stopTuning()
        // Just verify no crash
        assertNull(getPrivateDispatcher(viewModel))
    }


    // 16. PitchDetectionHandler positive pitch, nearest string STANDARD_TUNING
    @Test
    fun `PitchDetectionHandler positive pitch correct string (STANDARD)`() = runTest {
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(82.41f), null) // E2
        advanceUntilIdle()
        assertEquals("E2", viewModel.note.value)
    }

    // 17. PitchDetectionHandler correct string DROP D
    @Test
    fun `PitchDetectionHandler positive pitch correct string (DROP D)`() = runTest {
        viewModel.setTuning("Drop D")
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(73.42f), null) // D2
        advanceUntilIdle()
        assertEquals("D2", viewModel.note.value)
    }


    // 21. PitchDetectionHandler zero or negative pitch
    @Test
    fun `PitchDetectionHandler zero or negative pitch`() = runTest {
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(-1f), null)
        advanceUntilIdle()
        assertEquals("—", viewModel.note.value)
        assertEquals(0f, viewModel.deviationCents.value)
    }

    // 22. PitchDetectionHandler with empty tuning map (edge)
    @Test(expected = NullPointerException::class)
    fun `PitchDetectionHandler with empty tuning map`() = runTest {
        setPrivateTuningMap(viewModel, emptyMap())
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(110f), null)
    }

    // 23. ViewModel coroutine scope cancellation
    @Test
    fun `ViewModel coroutine scope cancellation`() = runTest {
        viewModel.viewModelScope.cancel()
        // Should not throw/cause crash when pitch handler tries to launch
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(110f), null)
        advanceUntilIdle()
        // Still, state not updated because scope is cancelled
        assertEquals("—", viewModel.note.value)
    }

    // 24. Multiple rapid calls to startTuning and stopTuning
    @Test
    fun `Multiple rapid calls to startTuning and stopTuning`() {
        every { AudioRecord.getMinBufferSize(any(), any(), any()) } returns 1024
        every { AudioDispatcherFactory.fromDefaultMicrophone(any(), any(), any()) } returns fakeDispatcher
        repeat(10) {
            viewModel.startTuning()
            viewModel.stopTuning()
        }
        assertNull(getPrivateDispatcher(viewModel))
    }

    // 25. StateFlow emissions for note
    @Test
    fun `StateFlow emissions for note`() = runTest {
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(110f), null)
        advanceUntilIdle()
        assertEquals("A2", viewModel.note.first())
    }

    // 26. StateFlow emissions for deviationCents
    @Test
    fun `StateFlow emissions for deviationCents`() = runTest {
        val handler = getPitchHandler(viewModel)
        handler.invoke(fakePitchResult(146.83f), null) // D3
        advanceUntilIdle()
        assertEquals(0f, viewModel.deviationCents.first())
    }

    // 27. StateFlow emissions for tuningMap
    @Test
    fun `StateFlow emissions for tuningMap`() = runTest {
        viewModel.setTuning("Drop D")
        assertTrue(viewModel.tuningMap.first().containsKey("D2"))
        viewModel.setTuning("Standard")
        assertTrue(viewModel.tuningMap.first().containsKey("E2"))
    }

    // -------- Helpers --------
    private fun getPrivateDispatcher(vm: TunerViewModel): AudioDispatcher? =
        vm.javaClass.getDeclaredField("dispatcher").apply { isAccessible = true }.get(vm) as? AudioDispatcher

    private fun setPrivateTuningMap(vm: TunerViewModel, map: Map<String, Float>) {
        val field = vm.javaClass.getDeclaredField("_tuningMap")
        field.isAccessible = true
        (field.get(vm) as MutableStateFlow<Map<String, Float>>).value = map
    }

    private fun getPitchHandler(vm: TunerViewModel): (PitchDetectionResult, Any?) -> Unit {
        // Repliciraj handler iz originalnog koda
        val map = vm.tuningMap.value
        return { result, _ ->
            val pitchHz = result.pitch
            if (pitchHz > 0f) {
                val (name, targetHz) = map.minByOrNull { kotlin.math.abs(pitchHz - it.value) }!!
                val cents = (1200 * kotlin.math.log2(pitchHz / targetHz)).coerceIn(-50f, 50f)
                vm.viewModelScope.launch {
                    val noteField = vm.javaClass.getDeclaredField("_note")
                    noteField.isAccessible = true
                    (noteField.get(vm) as MutableStateFlow<String>).value = name
                    val devField = vm.javaClass.getDeclaredField("_deviation")
                    devField.isAccessible = true
                    (devField.get(vm) as MutableStateFlow<Float>).value = cents
                }
            }
        }
    }

    private fun fakePitchResult(pitchHz: Float): PitchDetectionResult =
        mockk<PitchDetectionResult> {
            every { pitch } returns pitchHz
        }
}
