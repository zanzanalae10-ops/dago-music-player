package com.dagomusic.core.backup

import android.content.Context
import com.dagomusic.core.database.dao.MusicDao
import com.dagomusic.core.database.entities.EqualizerPresetEntity
import com.dagomusic.core.database.entities.PlaylistEntity
import com.dagomusic.core.database.entities.PlaylistItemEntity
import com.dagomusic.core.settings.DagoDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    private val dataStore: DagoDataStore
) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun createBackup(): String {
        // Collect Playlists
        val playlistsFlow = musicDao.getAllPlaylists()
        val playlists = playlistsFlow.first().map { playlist ->
            val songs = musicDao.getSongsInPlaylist(playlist.id).first().map { it.path }
            PlaylistBackup(playlist.name, songs)
        }

        // Collect Favorites
        val favorites = musicDao.getFavoriteSongs().first().map { it.path }

        // Collect History
        val history = musicDao.getRecentlyPlayedSongs().first().map { song ->
            PlaybackHistoryBackup(song.path, song.lastPlayed, song.playCount)
        }

        // Collect EQ Presets
        val presets = musicDao.getAllPresets().first().map { preset ->
            EqPresetBackup(
                preset.name, preset.bassBoost, preset.virtualizer, preset.loudness,
                listOf(preset.band1, preset.band2, preset.band3, preset.band4, preset.band5)
            )
        }

        // Collect Settings
        val settings = mutableMapOf<String, String>()
        settings["theme_mode"] = dataStore.themeMode.first()
        settings["material_you"] = dataStore.materialYou.first().toString()
        settings["app_language"] = dataStore.appLanguage.first()
        settings["playback_speed"] = dataStore.playbackSpeed.first().toString()
        settings["playback_pitch"] = dataStore.playbackPitch.first().toString()
        settings["audio_balance"] = dataStore.audioBalance.first().toString()
        settings["mono_audio"] = dataStore.monoAudio.first().toString()
        settings["crossfade_seconds"] = dataStore.crossfadeSeconds.first().toString()
        settings["gapless_playback"] = dataStore.gaplessPlayback.first().toString()
        settings["fade_in"] = dataStore.fadeIn.first().toString()
        settings["fade_out"] = dataStore.fadeOut.first().toString()
        settings["min_duration_seconds"] = dataStore.minDurationSeconds.first().toString()
        settings["min_size_kb"] = dataStore.minSizeKb.first().toString()
        settings["enable_blur"] = dataStore.enableBlur.first().toString()

        val backupData = BackupData(playlists, favorites, history, presets, settings)
        return json.encodeToString(backupData)
    }

    suspend fun restoreBackup(jsonString: String): Boolean {
        return try {
            val backupData = json.decodeFromString<BackupData>(jsonString)

            // Restore Settings
            backupData.settings["theme_mode"]?.let { dataStore.setThemeMode(it) }
            backupData.settings["material_you"]?.let { dataStore.setMaterialYou(it.toBoolean()) }
            backupData.settings["app_language"]?.let { dataStore.setAppLanguage(it) }
            backupData.settings["playback_speed"]?.let { dataStore.setPlaybackSpeed(it.toFloat()) }
            backupData.settings["playback_pitch"]?.let { dataStore.setPlaybackPitch(it.toFloat()) }
            backupData.settings["audio_balance"]?.let { dataStore.setAudioBalance(it.toFloat()) }
            backupData.settings["mono_audio"]?.let { dataStore.setMonoAudio(it.toBoolean()) }
            backupData.settings["crossfade_seconds"]?.let { dataStore.setCrossfadeSeconds(it.toInt()) }
            backupData.settings["gapless_playback"]?.let { dataStore.setGaplessPlayback(it.toBoolean()) }
            backupData.settings["fade_in"]?.let { dataStore.setFadeIn(it.toBoolean()) }
            backupData.settings["fade_out"]?.let { dataStore.setFadeOut(it.toBoolean()) }
            backupData.settings["min_duration_seconds"]?.let { dataStore.setMinDurationSeconds(it.toInt()) }
            backupData.settings["min_size_kb"]?.let { dataStore.setMinSizeKb(it.toInt()) }
            backupData.settings["enable_blur"]?.let { dataStore.setEnableBlur(it.toBoolean()) }

            // Restore EQ Presets
            backupData.eqPresets.forEach { p ->
                if (p.bands.size >= 5) {
                    musicDao.insertPreset(
                        EqualizerPresetEntity(
                            p.name, p.bassBoost, p.virtualizer, p.loudness,
                            p.bands[0], p.bands[1], p.bands[2], p.bands[3], p.bands[4]
                        )
                    )
                }
            }

            // Restore Favorites
            backupData.favorites.forEach { path ->
                musicDao.setFavorite(path, true)
            }

            // Restore History
            backupData.history.forEach { hist ->
                val song = musicDao.getSongByPath(hist.path)
                if (song != null) {
                    musicDao.updateSong(song.copy(lastPlayed = hist.lastPlayed, playCount = hist.playCount))
                }
            }

            // Restore Playlists
            backupData.playlists.forEach { plBackup ->
                val playlistId = musicDao.insertPlaylist(PlaylistEntity(name = plBackup.name))
                val items = plBackup.songs.mapIndexed { index, path ->
                    PlaylistItemEntity(playlistId, path, index)
                }
                musicDao.insertPlaylistItems(items)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun saveBackupToFile(): File? {
        return try {
            val data = createBackup()
            val backupFile = File(context.getExternalFilesDir(null), "dago_music_backup.json")
            backupFile.writeText(data)
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreBackupFromFile(file: File): Boolean {
        return try {
            if (!file.exists()) return false
            val data = file.readText()
            restoreBackup(data)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
