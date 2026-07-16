package com.dagomusic.core.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.dagomusic.core.database.dao.MusicDao
import com.dagomusic.core.database.entities.SongEntity
import com.dagomusic.core.settings.DagoDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicDao: MusicDao,
    private val dataStore: DagoDataStore
) {
    suspend fun scan() {
        val minDurationMs = (dataStore.minDurationSeconds.first()) * 1000L
        val minSizeBytes = (dataStore.minSizeKb.first()) * 1024L
        val excluded = dataStore.excludedFolders.first()

        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val songsList = mutableListOf<SongEntity>()
        val seenPaths = mutableSetOf<String>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol) ?: continue
                val file = File(path)
                if (!file.exists() || !file.canRead() || file.length() <= 0) continue

                // Check excluded folders
                val parentPath = file.parent ?: ""
                if (excluded.any { parentPath.startsWith(it) }) continue

                // Ignore duplicates
                if (seenPaths.contains(path)) continue
                seenPaths.add(path)

                val duration = cursor.getLong(durationCol)
                if (duration < minDurationMs) continue

                val size = cursor.getLong(sizeCol)
                if (size < minSizeBytes) continue

                val id = cursor.getLong(idCol)
                val title = cursor.getString(titleCol) ?: "Unknown Song"
                val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                val album = cursor.getString(albumCol) ?: "Unknown Album"
                val mimeType = cursor.getString(mimeCol) ?: "audio/mpeg"
                val trackNumber = cursor.getInt(trackCol)
                val year = cursor.getString(yearCol) ?: ""
                val dateAdded = cursor.getLong(dateAddedCol)

                // Read advanced tags/metadata if possible via MediaMetadataRetriever
                var bitrate = 0
                var sampleRate = 44100
                var codec = ""
                var composer = ""
                var genre = ""

                try {
                    MediaMetadataRetriever().use { retriever ->
                        retriever.setDataSource(path)
                        composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER) ?: ""
                        genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
                        val bitRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                        if (bitRateStr != null) {
                            bitrate = bitRateStr.toInt() / 1000 // kbps
                        }
                        codec = mimeType.substringAfter("audio/").uppercase()
                    }
                } catch (e: Exception) {
                    // Ignore metadata reading errors for individual files to keep scan robust and fast
                }

                songsList.add(
                    SongEntity(
                        path = path,
                        title = title,
                        artist = if (artist == "<unknown>") "Unknown Artist" else artist,
                        album = if (album == "<unknown>") "Unknown Album" else album,
                        duration = duration,
                        size = size,
                        mimeType = mimeType,
                        trackNumber = trackNumber,
                        genre = genre,
                        year = year,
                        composer = composer,
                        bitrate = bitrate,
                        sampleRate = sampleRate,
                        codec = codec,
                        dateAdded = dateAdded
                    )
                )
            }
        }

        if (songsList.isNotEmpty()) {
            musicDao.insertSongs(songsList)
        }

        // Auto-remove deleted files from db
        musicDao.keepOnlySongs(seenPaths.toList())
    }
}
