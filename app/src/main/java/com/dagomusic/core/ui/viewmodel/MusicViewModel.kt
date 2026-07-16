package com.dagomusic.core.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dagomusic.core.database.dao.MusicDao
import com.dagomusic.core.database.entities.PlaylistEntity
import com.dagomusic.core.database.entities.PlaylistItemEntity
import com.dagomusic.core.database.entities.SongEntity
import com.dagomusic.core.equalizer.AudioEffectManager
import com.dagomusic.core.player.PlaybackConnection
import com.dagomusic.core.scanner.MediaStoreScanner
import com.dagomusic.core.settings.DagoDataStore
import com.dagomusic.core.workers.LibraryScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    val dataStore: DagoDataStore,
    private val workManager: WorkManager,
    private val playbackConnection: PlaybackConnection,
    private val audioEffectManager: AudioEffectManager,
    private val mediaStoreScanner: MediaStoreScanner
) : ViewModel() {

    // Songs
    val allSongs: StateFlow<List<SongEntity>> = musicDao.getAllSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteSongs: StateFlow<List<SongEntity>> = musicDao.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded: StateFlow<List<SongEntity>> = musicDao.getRecentlyAddedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed: StateFlow<List<SongEntity>> = musicDao.getRecentlyPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<SongEntity>> = musicDao.getMostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Playlists
    val playlists: StateFlow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active track & playing states synced directly to Media3
    val isPlaying: StateFlow<Boolean> = playbackConnection.isPlaying
    val currentPosition: StateFlow<Long> = playbackConnection.currentPosition

    private val _currentPlayingSong = MutableStateFlow<SongEntity?>(null)
    val currentPlayingSong: StateFlow<SongEntity?> = _currentPlayingSong.asStateFlow()

    // Equalizer States
    val equalizerEnabled: StateFlow<Boolean> = dataStore.equalizerEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val equalizerBassBoost: StateFlow<Int> = dataStore.equalizerBassBoost
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equalizerVirtualizer: StateFlow<Int> = dataStore.equalizerVirtualizer
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equalizerLoudness: StateFlow<Int> = dataStore.equalizerLoudness
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val equalizerBand1: StateFlow<Float> = dataStore.equalizerBand1
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val equalizerBand2: StateFlow<Float> = dataStore.equalizerBand2
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val equalizerBand3: StateFlow<Float> = dataStore.equalizerBand3
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val equalizerBand4: StateFlow<Float> = dataStore.equalizerBand4
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val equalizerBand5: StateFlow<Float> = dataStore.equalizerBand5
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    init {
        // Automatically scan library instantly on startup
        scanLibraryInstant()

        // Sync active MediaItem back to SongEntity from local database
        viewModelScope.launch {
            playbackConnection.currentMediaItem.collect { mediaItem ->
                if (mediaItem != null) {
                    val song = musicDao.getSongByPath(mediaItem.mediaId)
                    if (song != null) {
                        _currentPlayingSong.value = song
                    }
                } else {
                    _currentPlayingSong.value = null
                }
            }
        }
    }

    fun scanLibraryInstant() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                mediaStoreScanner.scan()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun scanLibrary() {
        val scanRequest = OneTimeWorkRequestBuilder<LibraryScanWorker>().build()
        workManager.enqueue(scanRequest)
    }

    fun playSong(song: SongEntity) {
        _currentPlayingSong.value = song
        playbackConnection.play(song.path, song.title, song.artist, song.album)
        viewModelScope.launch {
            dataStore.setLastPlayingPath(song.path)
            musicDao.incrementPlayCount(song.path)
        }
    }

    fun togglePlayPause() {
        playbackConnection.togglePlayPause()
    }

    fun playNext() {
        playbackConnection.playNext()
    }

    fun playPrevious() {
        playbackConnection.playPrevious()
    }

    fun seekTo(positionMs: Long) {
        playbackConnection.seekTo(positionMs)
    }

    fun setShuffle(enabled: Boolean) {
        playbackConnection.setShuffle(enabled)
    }

    fun setRepeatMode(mode: Int) {
        playbackConnection.setRepeatMode(mode)
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            musicDao.setFavorite(song.path, !song.isFavorite)
            if (_currentPlayingSong.value?.path == song.path) {
                _currentPlayingSong.value = song.copy(isFavorite = !song.isFavorite)
            }
        }
    }

    fun deleteSong(song: SongEntity) {
        viewModelScope.launch {
            musicDao.deleteSongByPath(song.path)
            if (_currentPlayingSong.value?.path == song.path) {
                _currentPlayingSong.value = null
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicDao.insertPlaylist(PlaylistEntity(name = name))
        }
    }

    fun addSongToPlaylist(songPath: String, playlistId: Long) {
        viewModelScope.launch {
            musicDao.insertPlaylistItems(listOf(PlaylistItemEntity(playlistId, songPath, 0)))
        }
    }

    fun setSpeed(speed: Float) {
        viewModelScope.launch {
            dataStore.setPlaybackSpeed(speed)
        }
    }

    fun setPitch(pitch: Float) {
        viewModelScope.launch {
            dataStore.setPlaybackPitch(pitch)
        }
    }

    // Equalizer Functions
    fun setEqualizerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setEqualizerEnabled(enabled)
            audioEffectManager.setEffectsEnabled(enabled)
        }
    }

    fun setBassBoost(value: Int) {
        viewModelScope.launch {
            dataStore.setEqualizerBassBoost(value)
            audioEffectManager.setBassBoostStrength(value)
        }
    }

    fun setVirtualizer(value: Int) {
        viewModelScope.launch {
            dataStore.setEqualizerVirtualizer(value)
            audioEffectManager.setVirtualizerStrength(value)
        }
    }

    fun setLoudness(value: Int) {
        viewModelScope.launch {
            dataStore.setEqualizerLoudness(value)
            audioEffectManager.setLoudnessGain(value * 100) // gain is in milliBels
        }
    }

    fun setBandLevel(band: Int, valueDb: Float) {
        viewModelScope.launch {
            when (band) {
                0 -> dataStore.setEqualizerBand1(valueDb)
                1 -> dataStore.setEqualizerBand2(valueDb)
                2 -> dataStore.setEqualizerBand3(valueDb)
                3 -> dataStore.setEqualizerBand4(valueDb)
                4 -> dataStore.setEqualizerBand5(valueDb)
            }
            audioEffectManager.setBandLevel(band, valueDb)
        }
    }
}
