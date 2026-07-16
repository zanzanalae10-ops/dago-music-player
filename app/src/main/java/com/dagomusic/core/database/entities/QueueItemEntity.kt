package com.dagomusic.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey val songPath: String,
    val playOrder: Int
)
