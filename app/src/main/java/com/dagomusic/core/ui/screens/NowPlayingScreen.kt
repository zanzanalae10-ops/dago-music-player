package com.dagomusic.core.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagomusic.R
import com.dagomusic.core.ui.components.glassmorphic
import com.dagomusic.core.ui.theme.LocalAppStrings
import com.dagomusic.core.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    onNavigateToEqualizer: () -> Unit
) {
    val strings = LocalAppStrings.current
    val song by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val enableBlur by viewModel.dataStore.enableBlur.collectAsState(initial = true)

    // Rotating artwork animation
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    // Lyrics configuration
    var showLyrics by remember { mutableStateOf(false) }
    var lyricsSize by remember { mutableFloatStateOf(16f) }

    // Simulated playback progress
    var progress by remember { mutableFloatStateOf(0.3f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (true) {
                delay(1000)
                progress = (progress + 0.01f).coerceAtMost(1f)
            }
        }
    }

    if (song == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F1016)),
            contentAlignment = Alignment.Center
        ) {
            Text("No track selected", color = Color.White)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E0E29), Color(0xFF0F1016))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White)
                }
                Text(
                    text = "NOW PLAYING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    letterSpacing = 2.sp
                )
                IconButton(onClick = onNavigateToEqualizer) {
                    Icon(Icons.Default.Equalizer, contentDescription = "Equalizer", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lyrics or Rotating Disc view
            if (showLyrics) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .glassmorphic(enableBlur = enableBlur)
                        .clickable { showLyrics = false }
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = { lyricsSize = (lyricsSize + 2).coerceAtMost(24f) }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                        }
                        IconButton(onClick = { lyricsSize = (lyricsSize - 2).coerceAtLeast(12f) }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                        }
                    }
                    Text(
                        text = "I heard there was a secret chord\nThat David played, and it pleased the Lord\nBut you don't really care for music, do ya?\nIt goes like this, the fourth, the fifth\nThe minor fall, the major lift\nThe baffled king composing Hallelujah...",
                        color = Color.White,
                        fontSize = lyricsSize.sp,
                        lineHeight = (lyricsSize * 1.5).sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .clickable { showLyrics = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_default_artwork),
                        contentDescription = "Song cover",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize(0.85f)
                            .clip(CircleShape)
                            .rotate(if (isPlaying) angle else 0f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Metadata Detail
            Text(
                text = song!!.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song!!.artist,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Slider
            Slider(
                value = progress,
                onValueChange = { progress = it },
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = Color.DarkGray
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0:45", color = Color.Gray, fontSize = 12.sp)
                Text("3:15", color = Color.Gray, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Toggle Shuffle */ }) {
                    Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = Color.LightGray)
                }
                IconButton(onClick = { viewModel.playPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { viewModel.togglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "PlayPause",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { viewModel.playNext() }) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { viewModel.toggleFavorite(song!!) }) {
                    Icon(
                        imageVector = if (song!!.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (song!!.isFavorite) Color.Red else Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
