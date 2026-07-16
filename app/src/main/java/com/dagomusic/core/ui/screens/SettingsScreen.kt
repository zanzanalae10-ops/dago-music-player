package com.dagomusic.core.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagomusic.core.ui.components.glassmorphic
import com.dagomusic.core.ui.theme.LocalAppStrings
import com.dagomusic.core.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel
) {
    val strings = LocalAppStrings.current
    val themeMode by viewModel.dataStore.themeMode.collectAsState(initial = "SYSTEM")
    val materialYou by viewModel.dataStore.materialYou.collectAsState(initial = true)
    val enableBlur by viewModel.dataStore.enableBlur.collectAsState(initial = true)
    val appLanguage by viewModel.dataStore.appLanguage.collectAsState(initial = "en")

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = strings.navSettings,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Group 1: Appearance
        Text(strings.settingsAppearance, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", color = Color.White)
                Switch(
                    checked = themeMode == "DARK",
                    onCheckedChange = {
                        scope.launch {
                            viewModel.dataStore.setThemeMode(if (it) "DARK" else "LIGHT")
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings.settingsMaterialYou, color = Color.White)
                Switch(
                    checked = materialYou,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.dataStore.setMaterialYou(it)
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.settingsEnableBlur, color = Color.White)
                    Text("Turn off for better performance on weaker devices", color = Color.Gray, fontSize = 12.sp)
                }
                Switch(
                    checked = enableBlur,
                    onCheckedChange = {
                        scope.launch {
                            viewModel.dataStore.setEnableBlur(it)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group 2: Library & Actions
        Text("Library & Actions", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.scanLibrary() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Rescan Music Library", color = Color.White)
                    Text("Trigger immediate background scan", color = Color.Gray, fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch {
                            val newLang = if (appLanguage == "en") "ar" else "en"
                            viewModel.dataStore.setAppLanguage(newLang)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(strings.settingsLanguage, color = Color.White)
                    Text(if (appLanguage == "en") "English" else "العربية", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Group 3: About
        Text("About", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(bottom = 8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(strings.appName, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(strings.settingsVersion, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Text("Created by an elite team of engineers to deliver the ultimate local playback experience.", color = Color.LightGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
