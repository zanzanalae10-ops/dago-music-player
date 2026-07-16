package com.dagomusic.core.di

import android.content.Context
import androidx.room.Room
import com.dagomusic.core.database.DagoDatabase
import com.dagomusic.core.database.dao.MusicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): DagoDatabase {
        return Room.databaseBuilder(
            context,
            DagoDatabase::class.java,
            "dago_music.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideMusicDao(database: DagoDatabase): MusicDao {
        return database.musicDao()
    }
}
