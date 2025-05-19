package com.example.struna

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun AkordiScreen() {
    val akordi = listOf(
        "C", "Cm", "D", "Dm", "E", "Em", "F", "Fm",
        "G", "Gm", "A", "Am", "H", "Hm"
    )

    var selectedAkord by remember { mutableStateOf("Em") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Dropdown s ikonom
        Box {
            Button(onClick = { expanded = true }) {
                Text(selectedAkord)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown strelica",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                akordi.forEach { akord ->
                    DropdownMenuItem(
                        text = { Text(akord) },
                        onClick = {
                            selectedAkord = akord
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Prikaz slike akorda
        val imageName = selectedAkord.lowercase()
        val imageResId = getDrawableIdByName(imageName)

        if (imageResId != null) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = "Akord $selectedAkord",
                modifier = Modifier
                    .size(200.dp)
            )
        } else {
            Text(
                text = "Slika za akord \"$selectedAkord\" nije pronađena.",
                fontSize = 16.sp,
                color = Color.Red
            )
        }
    }
}

@Composable
fun getDrawableIdByName(name: String): Int? {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
    return if (resId != 0) resId else null
}
