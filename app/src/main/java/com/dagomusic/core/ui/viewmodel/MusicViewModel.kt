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
import com.dagomusic.core.settings.DagoDataStore
import com.dagomusic.core.workers.LibraryScanWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    val dataStore: DagoDataStore,
    private val workManager: WorkManager
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

    // Active track & playing states
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPlayingSong = MutableStateFlow<SongEntity?>(null)
    val currentPlayingSong: StateFlow<SongEntity?> = _currentPlayingSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    init {
        // Automatically scan library on startup
        scanLibrary()

        // Sync last active song
        viewModelScope.launch {
            dataStore.lastPlayingPath.collectLatest { path ->
                if (path != null) {
                    val song = musicDao.getSongByPath(path)
                    _currentPlayingSong.value = song
                }
            }
        }
    }

    fun scanLibrary() {
        val scanRequest = OneTimeWorkRequestBuilder<LibraryScanWorker>().build()
        workManager.enqueue(scanRequest)
    }

    fun playSong(song: SongEntity) {
        _currentPlayingSong.value = song
        _isPlaying.value = true
        viewModelScope.launch {
            dataStore.setLastPlayingPath(song.path)
            musicDao.incrementPlayCount(song.path)
        }
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun playNext() {
        // Simple mock rotation for demo purposes when no media session bound directly
        val songs = allSongs.value
        if (songs.isNotEmpty()) {
            val currentIndex = songs.indexOfFirst { it.path == _currentPlayingSong.value?.path }
            val nextIndex = (currentIndex + 1) % songs.size
            playSong(songs[nextIndex])
        }
    }

    fun playPrevious() {
        val songs = allSongs.value
        if (songs.isNotEmpty()) {
            val currentIndex = songs.indexOfFirst { it.path == _currentPlayingSong.value?.path }
            val prevIndex = if (currentIndex <= 0) songs.size - 1 else currentIndex - 1
            playSong(songs[prevIndex])
        }
    }

    fun toggleFavorite(song: SongEntity) {
        viewModelScope.launch {
            musicDao.setFavorite(song.path, !song.isFavorite)
            // Trigger local reload of active song status
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
                _isPlaying.value = false
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
}
