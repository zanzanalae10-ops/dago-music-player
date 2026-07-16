package com.dagomusic.core.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val playlists: List<PlaylistBackup> = emptyList(),
    val favorites: List<String> = emptyList(), // list of song paths
    val history: List<PlaybackHistoryBackup> = emptyList(),
    val eqPresets: List<EqPresetBackup> = emptyList(),
    val settings: Map<String, String> = emptyMap()
)

@Serializable
data class PlaylistBackup(
    val name: String,
    val songs: List<String> // song paths
)

@Serializable
data class PlaybackHistoryBackup(
    val path: String,
    val lastPlayed: Long,
    val playCount: Int
)

@Serializable
data class EqPresetBackup(
    val name: String,
    val bassBoost: Int,
    val virtualizer: Int,
    val loudness: Int,
    val bands: List<Float>
)
