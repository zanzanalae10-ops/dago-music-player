package com.dagomusic.core.settings

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "dago_settings")

@Singleton
class DagoDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MATERIAL_YOU = booleanPreferencesKey("material_you")
        val EXTRACT_COLOR_ARTWORK = booleanPreferencesKey("extract_color_artwork")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val PLAYBACK_PITCH = floatPreferencesKey("playback_pitch")
        val AUDIO_BALANCE = floatPreferencesKey("audio_balance")
        val MONO_AUDIO = booleanPreferencesKey("mono_audio")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val GAPLESS_PLAYBACK = booleanPreferencesKey("gapless_playback")
        val FADE_IN = booleanPreferencesKey("fade_in")
        val FADE_OUT = booleanPreferencesKey("fade_out")
        val MIN_DURATION_SECONDS = intPreferencesKey("min_duration_seconds")
        val MIN_SIZE_KB = intPreferencesKey("min_size_kb")
        val ENABLE_BLUR = booleanPreferencesKey("enable_blur")
        val LAST_PLAYING_PATH = stringPreferencesKey("last_playing_path")
        val LAST_PLAYING_POSITION = longPreferencesKey("last_playing_position")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val PINNED_FOLDERS = stringSetPreferencesKey("pinned_folders")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val EQUALIZER_ENABLED = booleanPreferencesKey("equalizer_enabled")
        val EQUALIZER_BASS_BOOST = intPreferencesKey("equalizer_bass_boost")
        val EQUALIZER_VIRTUALIZER = intPreferencesKey("equalizer_virtualizer")
        val EQUALIZER_LOUDNESS = intPreferencesKey("equalizer_loudness")
        val EQUALIZER_BAND1 = floatPreferencesKey("equalizer_band1")
        val EQUALIZER_BAND2 = floatPreferencesKey("equalizer_band2")
        val EQUALIZER_BAND3 = floatPreferencesKey("equalizer_band3")
        val EQUALIZER_BAND4 = floatPreferencesKey("equalizer_band4")
        val EQUALIZER_BAND5 = floatPreferencesKey("equalizer_band5")
    }

    // Theme Mode: SYSTEM, LIGHT, DARK
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "SYSTEM" }
    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    // Material You
    val materialYou: Flow<Boolean> = context.dataStore.data.map { it[MATERIAL_YOU] ?: true }
    suspend fun setMaterialYou(enabled: Boolean) {
        context.dataStore.edit { it[MATERIAL_YOU] = enabled }
    }

    // Extract color from artwork
    val extractColorArtwork: Flow<Boolean> = context.dataStore.data.map { it[EXTRACT_COLOR_ARTWORK] ?: true }
    suspend fun setExtractColorArtwork(enabled: Boolean) {
        context.dataStore.edit { it[EXTRACT_COLOR_ARTWORK] = enabled }
    }

    // Language: en, ar
    val appLanguage: Flow<String> = context.dataStore.data.map { it[APP_LANGUAGE] ?: "en" }
    suspend fun setAppLanguage(lang: String) {
        context.dataStore.edit { it[APP_LANGUAGE] = lang }
    }

    // Playback Speed
    val playbackSpeed: Flow<Float> = context.dataStore.data.map { it[PLAYBACK_SPEED] ?: 1.0f }
    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[PLAYBACK_SPEED] = speed }
    }

    // Playback Pitch
    val playbackPitch: Flow<Float> = context.dataStore.data.map { it[PLAYBACK_PITCH] ?: 1.0f }
    suspend fun setPlaybackPitch(pitch: Float) {
        context.dataStore.edit { it[PLAYBACK_PITCH] = pitch }
    }

    // Audio Balance (-1.0f to 1.0f, 0f is center)
    val audioBalance: Flow<Float> = context.dataStore.data.map { it[AUDIO_BALANCE] ?: 0.0f }
    suspend fun setAudioBalance(balance: Float) {
        context.dataStore.edit { it[AUDIO_BALANCE] = balance }
    }

    // Mono Audio
    val monoAudio: Flow<Boolean> = context.dataStore.data.map { it[MONO_AUDIO] ?: false }
    suspend fun setMonoAudio(enabled: Boolean) {
        context.dataStore.edit { it[MONO_AUDIO] = enabled }
    }

    // Crossfade Seconds
    val crossfadeSeconds: Flow<Int> = context.dataStore.data.map { it[CROSSFADE_SECONDS] ?: 0 }
    suspend fun setCrossfadeSeconds(seconds: Int) {
        context.dataStore.edit { it[CROSSFADE_SECONDS] = seconds }
    }

    // Gapless Playback
    val gaplessPlayback: Flow<Boolean> = context.dataStore.data.map { it[GAPLESS_PLAYBACK] ?: true }
    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.dataStore.edit { it[GAPLESS_PLAYBACK] = enabled }
    }

    // Fade In
    val fadeIn: Flow<Boolean> = context.dataStore.data.map { it[FADE_IN] ?: false }
    suspend fun setFadeIn(enabled: Boolean) {
        context.dataStore.edit { it[FADE_IN] = enabled }
    }

    // Fade Out
    val fadeOut: Flow<Boolean> = context.dataStore.data.map { it[FADE_OUT] ?: false }
    suspend fun setFadeOut(enabled: Boolean) {
        context.dataStore.edit { it[FADE_OUT] = enabled }
    }

    // Filter Minimum Duration (seconds)
    val minDurationSeconds: Flow<Int> = context.dataStore.data.map { it[MIN_DURATION_SECONDS] ?: 30 }
    suspend fun setMinDurationSeconds(seconds: Int) {
        context.dataStore.edit { it[MIN_DURATION_SECONDS] = seconds }
    }

    // Filter Minimum Size (KB)
    val minSizeKb: Flow<Int> = context.dataStore.data.map { it[MIN_SIZE_KB] ?: 100 }
    suspend fun setMinSizeKb(kb: Int) {
        context.dataStore.edit { it[MIN_SIZE_KB] = kb }
    }

    // Enable Blur/Glassmorphism (Performance control for older/weaker devices)
    val enableBlur: Flow<Boolean> = context.dataStore.data.map { it[ENABLE_BLUR] ?: true }
    suspend fun setEnableBlur(enabled: Boolean) {
        context.dataStore.edit { it[ENABLE_BLUR] = enabled }
    }

    // Last playing song path
    val lastPlayingPath: Flow<String?> = context.dataStore.data.map { it[LAST_PLAYING_PATH] }
    suspend fun setLastPlayingPath(path: String?) {
        context.dataStore.edit {
            if (path == null) it.remove(LAST_PLAYING_PATH)
            else it[LAST_PLAYING_PATH] = path
        }
    }

    // Last playing position (ms)
    val lastPlayingPosition: Flow<Long> = context.dataStore.data.map { it[LAST_PLAYING_POSITION] ?: 0L }
    suspend fun setLastPlayingPosition(position: Long) {
        context.dataStore.edit { it[LAST_PLAYING_POSITION] = position }
    }

    // Excluded Folders
    val excludedFolders: Flow<Set<String>> = context.dataStore.data.map { it[EXCLUDED_FOLDERS] ?: emptySet() }
    suspend fun excludeFolder(folder: String) {
        context.dataStore.edit {
            val current = it[EXCLUDED_FOLDERS] ?: emptySet()
            it[EXCLUDED_FOLDERS] = current + folder
        }
    }
    suspend fun unexcludeFolder(folder: String) {
        context.dataStore.edit {
            val current = it[EXCLUDED_FOLDERS] ?: emptySet()
            it[EXCLUDED_FOLDERS] = current - folder
        }
    }

    // Pinned Folders
    val pinnedFolders: Flow<Set<String>> = context.dataStore.data.map { it[PINNED_FOLDERS] ?: emptySet() }
    suspend fun pinFolder(folder: String) {
        context.dataStore.edit {
            val current = it[PINNED_FOLDERS] ?: emptySet()
            it[PINNED_FOLDERS] = current + folder
        }
    }
    suspend fun unpinFolder(folder: String) {
        context.dataStore.edit {
            val current = it[PINNED_FOLDERS] ?: emptySet()
            it[PINNED_FOLDERS] = current - folder
        }
    }

    // Repeat Mode (0 = REPEAT_OFF, 1 = REPEAT_ONE, 2 = REPEAT_ALL)
    val repeatMode: Flow<Int> = context.dataStore.data.map { it[REPEAT_MODE] ?: 0 }
    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { it[REPEAT_MODE] = mode }
    }

    // Shuffle Mode
    val shuffleMode: Flow<Boolean> = context.dataStore.data.map { it[SHUFFLE_MODE] ?: false }
    suspend fun setShuffleMode(enabled: Boolean) {
        context.dataStore.edit { it[SHUFFLE_MODE] = enabled }
    }

    // Equalizer State
    val equalizerEnabled: Flow<Boolean> = context.dataStore.data.map { it[EQUALIZER_ENABLED] ?: false }
    suspend fun setEqualizerEnabled(enabled: Boolean) {
        context.dataStore.edit { it[EQUALIZER_ENABLED] = enabled }
    }

    val equalizerBassBoost: Flow<Int> = context.dataStore.data.map { it[EQUALIZER_BASS_BOOST] ?: 0 }
    suspend fun setEqualizerBassBoost(value: Int) {
        context.dataStore.edit { it[EQUALIZER_BASS_BOOST] = value }
    }

    val equalizerVirtualizer: Flow<Int> = context.dataStore.data.map { it[EQUALIZER_VIRTUALIZER] ?: 0 }
    suspend fun setEqualizerVirtualizer(value: Int) {
        context.dataStore.edit { it[EQUALIZER_VIRTUALIZER] = value }
    }

    val equalizerLoudness: Flow<Int> = context.dataStore.data.map { it[EQUALIZER_LOUDNESS] ?: 0 }
    suspend fun setEqualizerLoudness(value: Int) {
        context.dataStore.edit { it[EQUALIZER_LOUDNESS] = value }
    }

    val equalizerBand1: Flow<Float> = context.dataStore.data.map { it[EQUALIZER_BAND1] ?: 0.0f }
    suspend fun setEqualizerBand1(value: Float) {
        context.dataStore.edit { it[EQUALIZER_BAND1] = value }
    }

    val equalizerBand2: Flow<Float> = context.dataStore.data.map { it[EQUALIZER_BAND2] ?: 0.0f }
    suspend fun setEqualizerBand2(value: Float) {
        context.dataStore.edit { it[EQUALIZER_BAND2] = value }
    }

    val equalizerBand3: Flow<Float> = context.dataStore.data.map { it[EQUALIZER_BAND3] ?: 0.0f }
    suspend fun setEqualizerBand3(value: Float) {
        context.dataStore.edit { it[EQUALIZER_BAND3] = value }
    }

    val equalizerBand4: Flow<Float> = context.dataStore.data.map { it[EQUALIZER_BAND4] ?: 0.0f }
    suspend fun setEqualizerBand4(value: Float) {
        context.dataStore.edit { it[EQUALIZER_BAND4] = value }
    }

    val equalizerBand5: Flow<Float> = context.dataStore.data.map { it[EQUALIZER_BAND5] ?: 0.0f }
    suspend fun setEqualizerBand5(value: Float) {
        context.dataStore.edit { it[EQUALIZER_BAND5] = value }
    }
}
