package com.dagomusic.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equalizer_presets")
data class EqualizerPresetEntity(
    @PrimaryKey val name: String,
    val bassBoost: Int,
    val virtualizer: Int,
    val loudness: Int,
    val band1: Float,
    val band2: Float,
    val band3: Float,
    val band4: Float,
    val band5: Float,
    val isCustom: Boolean = true
)
