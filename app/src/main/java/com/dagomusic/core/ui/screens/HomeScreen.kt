package com.dagomusic.core.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dagomusic.R
import com.dagomusic.core.database.entities.SongEntity
import com.dagomusic.core.ui.components.glassmorphic
import com.dagomusic.core.ui.theme.LocalAppStrings
import com.dagomusic.core.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    onSongSelected: (SongEntity) -> Unit
) {
    val strings = LocalAppStrings.current
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val recentlyAdded by viewModel.recentlyAdded.collectAsState()
    val mostPlayed by viewModel.mostPlayed.collectAsState()
    val allSongs by viewModel.allSongs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // App header / greetings
        Text(
            text = strings.appName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Premium Offline Listening",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Section: Recently Played
        if (recentlyPlayed.isNotEmpty()) {
            HomeSection(
                title = strings.homeRecentlyPlayed,
                songs = recentlyPlayed,
                onSongSelected = onSongSelected
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Most Played
        if (mostPlayed.isNotEmpty()) {
            HomeSection(
                title = strings.homeMostPlayed,
                songs = mostPlayed,
                onSongSelected = onSongSelected
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Section: Newly Added
        HomeSection(
            title = strings.homeNewlyAdded,
            songs = if (recentlyAdded.isNotEmpty()) recentlyAdded else allSongs,
            onSongSelected = onSongSelected
        )

        Spacer(modifier = Modifier.height(100.dp)) // extra spacing for bottom bars
    }
}

@Composable
fun HomeSection(
    title: String,
    songs: List<SongEntity>,
    onSongSelected: (SongEntity) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                Column(
                    modifier = Modifier
                        .width(120.dp)
                        .glassmorphic(cornerRadius = 12.dp)
                        .clickable { onSongSelected(song) }
                        .padding(8.dp)
                ) {
                    coil.compose.AsyncImage(
                        model = song.path,
                        contentDescription = "Cover",
                        placeholder = painterResource(id = R.drawable.ic_default_artwork),
                        error = painterResource(id = R.drawable.ic_default_artwork),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(104.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = Color.Gray,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
