package com.sap.codelab.di
import com.sap.codelab.data.repository.IMemoRepository
import com.sap.codelab.data.repository.MemoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by M.Çağatay
 * Created on 17.08.2026
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMemoRepository(repository: MemoRepository): IMemoRepository
}