package com.dagomusic.core.receivers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.dagomusic.MainActivity
import com.dagomusic.R

class MusicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        // Handle widget actions like com.dagomusic.action.PLAY_PAUSE
        val action = intent.action
        if (action == "com.dagomusic.action.PLAY_PAUSE" ||
            action == "com.dagomusic.action.NEXT" ||
            action == "com.dagomusic.action.PREV") {
            // Forward action to the service if needed or update layout
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MusicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId, isPlayingPlaceholder = action == "com.dagomusic.action.PLAY_PAUSE")
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        isPlayingPlaceholder: Boolean = false
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_large)

        // Set text
        views.setTextViewText(R.id.widget_title, "Dago Music")
        views.setTextViewText(R.id.widget_artist, "Offline Player")

        // Play/Pause icon state
        val playIcon = if (isPlayingPlaceholder) {
            android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_media_pause)
        } else {
            android.graphics.drawable.Icon.createWithResource(context, android.R.drawable.ic_media_play)
        }
        views.setImageViewIcon(R.id.widget_play, playIcon)

        // Intent for main app click
        val appIntent = Intent(context, MainActivity::class.java)
        val appPendingIntent = PendingIntent.getActivity(
            context, 0, appIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_artwork, appPendingIntent)

        // Action Intents
        val playIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = "com.dagomusic.action.PLAY_PAUSE"
        }
        val playPendingIntent = PendingIntent.getBroadcast(
            context, 1, playIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_play, playPendingIntent)

        val nextIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = "com.dagomusic.action.NEXT"
        }
        val nextPendingIntent = PendingIntent.getBroadcast(
            context, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_next, nextPendingIntent)

        val prevIntent = Intent(context, MusicWidgetProvider::class.java).apply {
            action = "com.dagomusic.action.PREV"
        }
        val prevPendingIntent = PendingIntent.getBroadcast(
            context, 3, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_prev, prevPendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
