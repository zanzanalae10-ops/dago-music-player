package com.dagomusic.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dagomusic.core.database.dao.MusicDao
import com.dagomusic.core.database.entities.*

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        QueueItemEntity::class,
        EqualizerPresetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DagoDatabase : RoomDatabase() {
    abstract fun musicDao(): MusicDao
}
