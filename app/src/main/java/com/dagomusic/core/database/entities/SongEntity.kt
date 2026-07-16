package com.dagomusic.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val path: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    val mimeType: String,
    val trackNumber: Int = 0,
    val genre: String = "",
    val year: String = "",
    val composer: String = "",
    val lastPlayed: Long = 0,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val codec: String = "",
    val dateAdded: Long = 0
)
