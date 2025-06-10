package com.example.struna

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

data class BackingTrack(
    val name: String,
    val filename: String,        // npr. "a_blues" (bez ekstenzije)
    val allowedNotes: List<String>
)

class JamModeViewModel : ViewModel() {

    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score

    private val _lastNote = MutableStateFlow("—")
    val lastNote: StateFlow<String> = _lastNote

    private var currentTrack: BackingTrack? = null
    private var allowedNotes: List<String> = emptyList()
    private var jamming = false

    private var dispatcher: AudioDispatcher? = null
    private var mediaPlayer: MediaPlayer? = null

    private var lastScoredNote: String = ""

    fun startJam(track: BackingTrack, context: Context) {
        currentTrack = track
        allowedNotes = track.allowedNotes
        _score.value = 0
        jamming = true
        lastScoredNote = ""
        playBackingTrack(track, context)
        startTuner()
    }

    fun stopJam() {
        jamming = false
        stopTuner()
        stopBackingTrack()
    }

    private fun playBackingTrack(track: BackingTrack, context: Context) {
        stopBackingTrack()
        val resId = context.resources.getIdentifier(
            track.filename.substringBeforeLast('.'),
            "raw",
            context.packageName
        )
        if (resId == 0) return // fail-safe

        mediaPlayer = MediaPlayer.create(context, resId)
        mediaPlayer?.isLooping = false
        mediaPlayer?.setOnCompletionListener {
            stopJam()
        }
        mediaPlayer?.start()
    }

    private fun stopBackingTrack() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startTuner() {
        if (dispatcher != null) return

        try {
            val sampleRate = 44100
            val bufferSize = 2048

            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf < 0) return

            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0)

            val handler = PitchDetectionHandler { result, _ ->
                val pitchHz = result.pitch
                if (pitchHz > 0) {
                    val note = freqToNoteName(pitchHz)
                    _lastNote.value = note

                    // Boduj allowed note, ali ne spammati isti ton za redom
                    if (jamming && allowedNotes.contains(note) && note != lastScoredNote) {
                        lastScoredNote = note
                        viewModelScope.launch {
                            _score.value += 1
                        }
                    }
                }
            }
            val processor = PitchProcessor(
                PitchEstimationAlgorithm.YIN,
                sampleRate.toFloat(), bufferSize, handler
            )
            dispatcher!!.addAudioProcessor(processor)

            viewModelScope.launch(Dispatchers.IO) {
                try {
                    dispatcher!!.run()
                } catch (e: Exception) {
                    Log.e("JamModeVM", "Tuner error", e)
                }
            }
        } catch (e: Exception) {
            Log.e("JamModeVM", "startTuner fail", e)
        }
    }

    private fun stopTuner() {
        dispatcher?.stop()
        dispatcher = null
    }

    // Pretvori frekvenciju u naziv note (C, C#, D, ...)
    fun freqToNoteName(freq: Float): String {
        if (freq <= 0f) return "—"
        val notes = listOf(
            "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
        )
        val midi = (69 + 12 * log2(freq / 440f)).roundToInt()
        val noteIndex = (midi % 12 + 12) % 12  // osigurava pozitivan indeks
        return notes[noteIndex]
    }

    fun saveScoreToFirebase(onResult: (isHighScore: Boolean) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val db = Firebase.firestore
        val uid = user.uid
        val trackName = currentTrack?.name ?: "Unknown"
        val scoreValue = score.value

        // Spremaj svaki jam u podkolekciju "jams" kod korisnika (ne mijenjamo)
        val jamData = hashMapOf(
            "score" to scoreValue,
            "track" to trackName,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("users").document(uid)
            .collection("jams")
            .add(jamData)

        // Dohvati high score za ovu skalu (track) iz kolekcije scores
        val scoreDocId = "${uid}_$trackName" // Jedan rekord po useru po skali!
        db.collection("scores").document(scoreDocId).get()
            .addOnSuccessListener { document ->
                val currentHighScore = document.getLong("score") ?: 0

                if (scoreValue > currentHighScore) {
                    // Novi rekord za ovu skalu → update u /scores
                    val scoreData = hashMapOf(
                        "userId" to uid,
                        "username" to (user.displayName ?: user.email ?: "Unknown"),
                        "score" to scoreValue,
                        "track" to trackName,
                        "timestamp" to System.currentTimeMillis()
                    )
                    db.collection("scores").document(scoreDocId).set(scoreData)
                    onResult(true)
                } else {
                    onResult(false)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }


}
