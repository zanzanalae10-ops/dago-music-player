package com.dagomusic.core.database.dao

import androidx.room.*
import com.dagomusic.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {

    // Songs
    @Query("SELECT * FROM songs ORDER BY title ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC")
    fun getRecentlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayed > 0 ORDER BY lastPlayed DESC")
    fun getRecentlyPlayedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC")
    fun getMostPlayedSongs(): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE path = :path")
    suspend fun deleteSongByPath(path: String)

    @Query("DELETE FROM songs WHERE path NOT IN (:paths)")
    suspend fun keepOnlySongs(paths: List<String>)

    @Query("SELECT * FROM songs WHERE path = :path LIMIT 1")
    suspend fun getSongByPath(path: String): SongEntity?

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE path = :path")
    suspend fun setFavorite(path: String, isFavorite: Boolean)

    @Query("UPDATE songs SET lastPlayed = :lastPlayed, playCount = playCount + 1 WHERE path = :path")
    suspend fun incrementPlayCount(path: String, lastPlayed: Long = System.currentTimeMillis())

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    // Playlist Items
    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_items pi ON s.path = pi.songPath
        WHERE pi.playlistId = :playlistId
        ORDER BY pi.playOrder ASC
    """)
    fun getSongsInPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId AND songPath = :songPath")
    suspend fun removePlaylistItem(playlistId: Long, songPath: String)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearPlaylistItems(playlistId: Long)

    // Playback Queue
    @Query("SELECT * FROM queue_items ORDER BY playOrder ASC")
    suspend fun getQueueItems(): List<QueueItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItemEntity>)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()

    @Transaction
    suspend fun saveQueue(paths: List<String>) {
        clearQueue()
        val items = paths.mapIndexed { index, path -> QueueItemEntity(path, index) }
        insertQueueItems(items)
    }

    // Equalizer Presets
    @Query("SELECT * FROM equalizer_presets ORDER BY name ASC")
    fun getAllPresets(): Flow<List<EqualizerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: EqualizerPresetEntity)

    @Query("DELETE FROM equalizer_presets WHERE name = :name")
    suspend fun deletePreset(name: String)
}
