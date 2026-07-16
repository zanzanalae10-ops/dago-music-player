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
import com.dagomusic.core.ui.theme.LocalAppStrings
import com.dagomusic.core.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    val eqEnabled by viewModel.equalizerEnabled.collectAsState()
    val bassBoost by viewModel.equalizerBassBoost.collectAsState()
    val virtualizer by viewModel.equalizerVirtualizer.collectAsState()
    val loudness by viewModel.equalizerLoudness.collectAsState()
    val enableBlur by viewModel.dataStore.enableBlur.collectAsState(initial = true)

    val band60Hz by viewModel.equalizerBand1.collectAsState()
    val band230Hz by viewModel.equalizerBand2.collectAsState()
    val band910Hz by viewModel.equalizerBand3.collectAsState()
    val band4kHz by viewModel.equalizerBand4.collectAsState()
    val band14kHz by viewModel.equalizerBand5.collectAsState()

    val presetList = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Pop", "Rock", "Custom")
    var selectedPreset by remember { mutableStateOf("Normal") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.eqTitle, color = Color.White) },
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
                    .glassmorphic(enableBlur = enableBlur)
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
                    onCheckedChange = { viewModel.setEqualizerEnabled(it) }
                )
            }

            // Presets row
            if (eqEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(enableBlur = enableBlur)
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
                                        viewModel.setBandLevel(0, 4f)
                                        viewModel.setBandLevel(1, 3f)
                                        viewModel.setBandLevel(2, -1f)
                                        viewModel.setBandLevel(3, 2f)
                                        viewModel.setBandLevel(4, 5f)
                                    } else {
                                        viewModel.setBandLevel(0, 0f)
                                        viewModel.setBandLevel(1, 0f)
                                        viewModel.setBandLevel(2, 0f)
                                        viewModel.setBandLevel(3, 0f)
                                        viewModel.setBandLevel(4, 0f)
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
                        .glassmorphic(enableBlur = enableBlur)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Frequency Bands", color = Color.White, fontWeight = FontWeight.Bold)

                    // 60 Hz
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("60 Hz", color = Color.LightGray, modifier = Modifier.width(60.dp))
                        Slider(
                            value = band60Hz,
                            onValueChange = { viewModel.setBandLevel(0, it); selectedPreset = "Custom" },
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
                            onValueChange = { viewModel.setBandLevel(1, it); selectedPreset = "Custom" },
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
                            onValueChange = { viewModel.setBandLevel(2, it); selectedPreset = "Custom" },
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
                            onValueChange = { viewModel.setBandLevel(3, it); selectedPreset = "Custom" },
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
                            onValueChange = { viewModel.setBandLevel(4, it); selectedPreset = "Custom" },
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
                        .glassmorphic(enableBlur = enableBlur)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Enhancements", color = Color.White, fontWeight = FontWeight.Bold)

                    // Bass Boost
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.eqBassBoost, color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = bassBoost.toFloat(),
                            onValueChange = { viewModel.setBassBoost(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$bassBoost%", color = Color.White, modifier = Modifier.width(36.dp))
                    }

                    // Virtualizer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(strings.eqVirtualizer, color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = virtualizer.toFloat(),
                            onValueChange = { viewModel.setVirtualizer(it.toInt()) },
                            valueRange = 0f..100f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$virtualizer%", color = Color.White, modifier = Modifier.width(36.dp))
                    }

                    // Loudness Enhancer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Loudness", color = Color.LightGray, modifier = Modifier.width(80.dp))
                        Slider(
                            value = loudness.toFloat(),
                            onValueChange = { viewModel.setLoudness(it.toInt()) },
                            valueRange = 0f..20f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$loudness dB", color = Color.White, modifier = Modifier.width(36.dp))
                    }
                }
            }
        }
    }
}
