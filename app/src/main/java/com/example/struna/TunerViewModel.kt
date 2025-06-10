package com.example.struna

import android.media.AudioFormat
import android.media.AudioRecord
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2

class TunerViewModel : ViewModel() {

    companion object {
        private const val TAG = "TunerViewModel"

        private val STANDARD_TUNING = mapOf(
            "E2" to 82.41f,
            "A2" to 110.00f,
            "D3" to 146.83f,
            "G3" to 196.00f,
            "B3" to 246.94f,
            "E4" to 329.63f
        )
        private val DROP_D_TUNING = mapOf(
            "D2" to 73.42f,
            "A2" to 110.00f,
            "D3" to 146.83f,
            "G3" to 196.00f,
            "B3" to 246.94f,
            "E4" to 329.63f
        )
    }

    // --- Tuning selection ---
    private val _tuningMap = MutableStateFlow(STANDARD_TUNING)
    val tuningMap: StateFlow<Map<String, Float>> = _tuningMap

    fun setTuning(name: String) {
        _tuningMap.value = if (name == "Drop D") DROP_D_TUNING else STANDARD_TUNING
    }

    // --- Detected note + cents ---
    private val _note = MutableStateFlow("—")
    val note: StateFlow<String> = _note

    private val _deviation = MutableStateFlow(0f)
    val deviationCents: StateFlow<Float> = _deviation

    // --- Internal audio dispatcher ---
    private var dispatcher: AudioDispatcher? = null

    fun startTuning() {
        if (dispatcher != null) return

        try {
            val sampleRate = 44_100
            val bufferSize  = 2048

            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf < 0) {
                throw IllegalStateException("Invalid AudioRecord buffer size: $minBuf")
            }

            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
                sampleRate, bufferSize, /* overlap = */ 0
            )

            val handler = PitchDetectionHandler { result, _ ->
                val pitchHz = result.pitch
                if (pitchHz > 0f) {
                    // choose nearest string
                    val (name, targetHz) = _tuningMap.value
                        .minByOrNull { abs(pitchHz - it.value) }!!
                    val cents = (1200 * log2(pitchHz / targetHz))
                        .coerceIn(-50f, 50f)

                    viewModelScope.launch {
                        _note.value = name
                        _deviation.value = cents
                    }
                }
            }

            // ← Switch to YIN here instead of MPM to avoid the assertion
            val processor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                sampleRate.toFloat(),
                bufferSize,
                handler
            )
            dispatcher!!.addAudioProcessor(processor)

            // run in IO—and catch any AssertionErrors so they don’t bubble up
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    dispatcher!!.run()
                } catch (t: Throwable) {
                    Log.e(TAG, "Audio dispatcher crashed", t)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start tuning", e)
        }
    }

    fun stopTuning() {
        dispatcher?.stop()
        dispatcher = null
    }
}
