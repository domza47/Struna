package com.example.struna

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
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

@Composable
fun MetronomScreen() {
    val context = LocalContext.current
    var bpm by remember { mutableStateOf(60f) }
    var isPlaying by remember { mutableStateOf(false) } // kontrola pokretanja

    LaunchedEffect(bpm, isPlaying) {
        while (isPlaying) {
            val player = MediaPlayer.create(context, R.raw.click)
            player.setOnCompletionListener {
                it.release()
            }
            player.start()

            val delayMillis = (60000 / bpm).toLong()
            delay(delayMillis)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(if (isPlaying) Color(0xFF66FF66) else Color(0xFFFF6666), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bpm.toInt().toString(),
                fontSize = 48.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = bpm,
            onValueChange = { bpm = it },
            valueRange = 40f..240f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { isPlaying = !isPlaying }) {
            Text(if (isPlaying) "Stop" else "Start")
        }
    }
}




