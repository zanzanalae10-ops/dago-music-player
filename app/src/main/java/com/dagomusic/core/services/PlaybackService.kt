package com.dagomusic.core.services

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.dagomusic.MainActivity
import com.dagomusic.core.database.dao.MusicDao
import com.dagomusic.core.equalizer.AudioEffectManager
import com.dagomusic.core.settings.DagoDataStore
import com.google.common.collect.ImmutableList
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var audioEffectManager: AudioEffectManager

    @Inject
    lateinit var dataStore: DagoDataStore

    @Inject
    lateinit var musicDao: MusicDao

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(pendingIntent)
            .setCallback(CustomSessionCallback())
            .build()

        // Sync Audio Effects with Session
        exoPlayer.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                audioEffectManager.initEffects(audioSessionId)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                val currentPath = mediaItem?.mediaId ?: return
                serviceScope.launch {
                    dataStore.setLastPlayingPath(currentPath)
                    musicDao.incrementPlayCount(currentPath)
                }
            }
        })

        // Restore State (Last playing track, speed/pitch, repeat/shuffle)
        serviceScope.launch {
            val speed = dataStore.playbackSpeed.first()
            val pitch = dataStore.playbackPitch.first()
            exoPlayer.setPlaybackSpeed(speed)

            val repeat = dataStore.repeatMode.first()
            exoPlayer.repeatMode = when (repeat) {
                1 -> Player.REPEAT_MODE_ONE
                2 -> Player.REPEAT_MODE_ALL
                else -> Player.REPEAT_MODE_OFF
            }

            val shuffle = dataStore.shuffleMode.first()
            exoPlayer.shuffleModeEnabled = shuffle

            // Restore Queue
            val queueItems = musicDao.getQueueItems()
            if (queueItems.isNotEmpty()) {
                val mediaItems = queueItems.mapNotNull { item ->
                    val song = musicDao.getSongByPath(item.songPath) ?: return@mapNotNull null
                    MediaItem.Builder()
                        .setMediaId(song.path)
                        .setUri(song.path)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .build()
                        )
                        .build()
                }
                exoPlayer.setMediaItems(mediaItems)

                // Seek to last saved song and position
                val lastPath = dataStore.lastPlayingPath.first()
                if (lastPath != null) {
                    val index = mediaItems.indexOfFirst { it.mediaId == lastPath }
                    if (index >= 0) {
                        val lastPos = dataStore.lastPlayingPosition.first()
                        exoPlayer.seekTo(index, lastPos)
                    }
                }
                exoPlayer.prepare()
            }
        }

    }

    private inner class CustomSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
            val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands.build())
                .setAvailablePlayerCommands(playerCommands.build())
                .build()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        // Save state before destruction
        val currentMediaItem = exoPlayer.currentMediaItem
        if (currentMediaItem != null) {
            val path = currentMediaItem.mediaId
            val pos = exoPlayer.currentPosition
            // Run synchronous or quick launch save
            serviceScope.launch {
                dataStore.setLastPlayingPath(path)
                dataStore.setLastPlayingPosition(pos)
                // Save Queue paths to Database
                val paths = (0 until exoPlayer.mediaItemCount).map { i ->
                    exoPlayer.getMediaItemAt(i).mediaId
                }
                musicDao.saveQueue(paths)
            }
        }
        audioEffectManager.releaseEffects()
        exoPlayer.release()
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }
}
