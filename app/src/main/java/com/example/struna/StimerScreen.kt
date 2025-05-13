package com.example.struna

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt

@Composable
fun TuningDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = { /* readOnly */ },
            label = { Text("Štim") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Toggle tuning menu",
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun StimerScreen(viewModel: TunerViewModel = viewModel()) {
    val ctx = LocalContext.current

    // 1) Mic permission
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    // 2) Auto start/stop on permission
    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.startTuning()
        else launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopTuning() }
    }

    // 3) Dropdown state
    val options = listOf("Standard", "Drop D")
    var selected by remember { mutableStateOf(options[0]) }

    LaunchedEffect(selected) {
        viewModel.setTuning(selected)
    }

    // 4) Observe VM flows
    val note by viewModel.note.collectAsState()
    val cents by viewModel.deviationCents.collectAsState()
    val animatedCents by animateFloatAsState(targetValue = cents)

    // 5) UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!hasPermission) {
            Text("Čekanje na mikrofon…", style = typography.bodyLarge)
        } else {
            TuningDropdown(
                options = options,
                selectedOption = selected,
                onOptionSelected = { selected = it },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            Text(note, style = typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Gauge(animatedCents)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (abs(animatedCents) < 10f)"${animatedCents.roundToInt()} super!" else "${animatedCents.roundToInt()} nije u štimu!",
                style = typography.bodyLarge,
                color = if (abs(animatedCents) < 10f) Color.Green else Color.Red
            )
        }
    }
}

@Composable
fun Gauge(deviation: Float, size: Dp = 200.dp) {
    Canvas(Modifier.size(size)) {
        val strokePx = 12.dp.toPx()
        val startAngle = 135f
        val sweep = 270f
        val area = Rect(0f, 0f, size.toPx(), size.toPx())

        // Background arc
        drawArc(
            color = Color.LightGray,
            startAngle = startAngle,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = Offset.Zero,
            size = area.size,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // In-tune zone ±5¢
        val fine = sweep * 5f / 100f
        drawArc(
            color = Color.Green,
            startAngle = startAngle + (sweep - fine) / 2,
            sweepAngle = fine,
            useCenter = false,
            topLeft = Offset.Zero,
            size = area.size,
            style = Stroke(width = strokePx, cap = StrokeCap.Round)
        )

        // Needle
        val angle = startAngle +
                sweep / 2 +
                (deviation.coerceIn(-50f, 50f) / 50f) * (sweep / 2)
        val cx = size.toPx() / 2
        val cy = size.toPx() / 2
        val r = size.toPx() / 2 - strokePx
        val rad = Math.toRadians(angle.toDouble())
        val end = Offset(
            x = cx + r * cos(rad).toFloat(),
            y = cy + r * sin(rad).toFloat()
        )
        drawLine(
            color = Color.Red,
            start = Offset(cx, cy),
            end = end,
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
