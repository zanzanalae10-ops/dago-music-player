package com.dagomusic.core.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagomusic.core.ui.components.glassmorphic
import com.dagomusic.core.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    var eqEnabled by remember { mutableStateOf(true) }
    var bassBoost by remember { mutableFloatStateOf(15f) }
    var virtualizer by remember { mutableFloatStateOf(20f) }
    var loudness by remember { mutableFloatStateOf(5f) }

    var band60Hz by remember { mutableFloatStateOf(0f) }
    var band230Hz by remember { mutableFloatStateOf(0f) }
    var band910Hz by remember { mutableFloatStateOf(0f) }
    var band4kHz by remember { mutableFloatStateOf(0f) }
    var band14kHz by remember { mutableFloatStateOf(0f) }

    val presetList = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Pop", "Rock", "Custom")
    var selectedPreset by remember { mutableStateOf("Normal") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Equalizer", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F1016))
            )
        },
        containerColor = Color(0xFF0F1016)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Master Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enable Equalizer & Effects",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Switch(
                    checked = eqEnabled,
                    onCheckedChange = { eqEnabled = it }
                )
            }

            // Presets row
            if (eqEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic()
                        .padding(16.dp)
                ) {
                    Text("Select Preset", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetList.take(4).forEach { preset ->
                            FilterChip(
                                selected = selectedPreset == preset,
                                onClick = {
                                    selectedPreset = preset
                                    if (preset == "Rock") {
                                        band60Hz = 4f
                                        band230Hz = 3f
                                        band910Hz = -1f
                                        band4kHz = 2f
                                        band14kHz = 5f
                                    } else {
                                        band60Hz = 0f
                                        band230Hz = 0f
                                        band910Hz = 0f
                                        band4kHz = 0f
                                        band14kHz = 0f
                                    }
                                },
                                label = { Text(preset) }
                            )
                        }
                    }
                }

                // 5 Frequency Bands
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Frequency Bands", color = Color.White, fontWeight = FontWeight.Bold)

                    // 60 Hz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("60 Hz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band60Hz,
                            onValueChange = { band60Hz = it; selectedPreset = "Custom" },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${band60Hz.toInt()} dB", color = Color.White, modifier = Modifier.width(48.dp))
                    }

                    // 230 Hz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("230 Hz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band230Hz,
                            onValueChange = { band230Hz = it; selectedPreset = "Custom" },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${band230Hz.toInt()} dB", color = Color.White, modifier = Modifier.width(48.dp))
                    }

                    // 910 Hz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("910 Hz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band910Hz,
                            onValueChange = { band910Hz = it; selectedPreset = "Custom" },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${band910Hz.toInt()} dB", color = Color.White, modifier = Modifier.width(48.dp))
                    }

                    // 4 kHz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("4 kHz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band4kHz,
                            onValueChange = { band4kHz = it; selectedPreset = "Custom" },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${band4kHz.toInt()} dB", color = Color.White, modifier = Modifier.width(48.dp))
                    }

                    // 14 kHz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("14 kHz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band14kHz,
                            onValueChange = { band14kHz = it; selectedPreset = "Custom" },
                            valueRange = -15f..15f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${band14kHz.toInt()} dB", color = Color.White, modifier = Modifier.width(48.dp))
                    }
                }

                // Advanced Effects (Bass Boost, Virtualizer, Loudness Enhancer)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Enhancements", color = Color.White, fontWeight = FontWeight.Bold)

                    // Bass Boost
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Bass Boost", color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = bassBoost,
                            onValueChange = { bassBoost = it },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${bassBoost.toInt()}%", color = Color.White, modifier = Modifier.width(36.dp))
                    }

                    // Virtualizer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("3D Sound", color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = virtualizer,
                            onValueChange = { virtualizer = it },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${virtualizer.toInt()}%", color = Color.White, modifier = Modifier.width(36.dp))
                    }

                    // Loudness Enhancer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Loudness", color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = loudness,
                            onValueChange = { loudness = it },
                            valueRange = 0f..20f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("${loudness.toInt()} dB", color = Color.White, modifier = Modifier.width(36.dp))
                    }
                }
            }
        }
    }
}
