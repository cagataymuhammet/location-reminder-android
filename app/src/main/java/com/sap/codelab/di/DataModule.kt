package com.sap.codelab.di

import android.content.Context
import androidx.room.Room
import com.sap.codelab.data.db.Database
import com.sap.codelab.data.db.DatabaseMigratorFrom1To2
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
 * DataModule provides data-layer dependencies such as the Room database
 * and DAO instances to the Hilt dependency graph.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DataModule {

    /*
     * The name of the database.
     */
    private const val DATABASE_NAME: String = "codelab"

    /*
     * Provides a singleton instance of the Room database.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): Database {
        return Room.databaseBuilder(
            context,
            Database::class.java,
            DATABASE_NAME
        ).addMigrations(DatabaseMigratorFrom1To2).build()
    }

    /*
     * Provides a singleton instance of the MemoDao.
     */
    @Provides
    fun provideMemoDao(database: Database): MemoDao {
        return database.getMemoDao()
    }
}