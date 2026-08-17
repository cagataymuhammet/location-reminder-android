package com.sap.codelab.di

import android.content.Context
import androidx.room.Room
import com.sap.codelab.data.db.Database
import com.sap.codelab.data.db.MemoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    private const val DATABASE_NAME: String = "codelab"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context,
            Database::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideMemoDao(database: Database): MemoDao {
        return database.getMemoDao()
    }
}