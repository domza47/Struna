package com.example.struna

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun MetronomScreen() {
    val context = LocalContext.current
    var bpm by remember { mutableStateOf(60f) }
    var isPlaying by remember { mutableStateOf(false) }

    // SoundPool za bolju preciznost zvuka
    val soundPool = remember {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attrs)
            .build()
    }

    var soundId by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        soundId = soundPool.load(context, R.raw.click, 1)
        onDispose {
            soundPool.release()
        }
    }

    // Metronom petlja
    LaunchedEffect(bpm, isPlaying) {
        while (isPlaying && soundId != 0 && isActive) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            val delayMillis = (60000 / bpm).toLong()
            delay(delayMillis)
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {


        // Krug s BPM brojem
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color(0xFFFF8A80), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = bpm.toInt().toString(),
                    fontSize = 48.sp,
                    color = Color.Black
                )
                Text(
                    text = "bpm",
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        }

        // Slider
        Slider(
            value = bpm,
            onValueChange = { bpm = it },
            valueRange = 15f..240f,
            steps = 225,
            modifier = Modifier
                .padding(horizontal = 24.dp)
        )

        // Gumb Start/Stop
        Button(
            onClick = { isPlaying = !isPlaying },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isPlaying) Color.Red else Color.Green
            ),
            modifier = Modifier
                .size(100.dp)
        ) {
            Text(
                text = if (isPlaying) "STOP" else "START",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
