package com.dagomusic.core.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dagomusic.core.scanner.MediaStoreScanner
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class LibraryScanWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ScannerEntryPoint {
        fun getScanner(): MediaStoreScanner
    }

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPoints.get(
                applicationContext,
                ScannerEntryPoint::class.java
            )
            val scanner = entryPoint.getScanner()
            scanner.scan()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
