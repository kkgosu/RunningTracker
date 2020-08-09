package com.kvlg.runningtracker.di

import com.kvlg.runningtracker.adapters.RunAdapter
import com.kvlg.runningtracker.adapters.RunDiffCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

/**
 * @author Konstantin Koval
 * @since 09.08.2020
 */
@Module
@InstallIn(FragmentComponent::class)
object RunModule {

    @Provides
    @FragmentScoped
    fun provideRunDiffCallback() = RunDiffCallback()

    @Provides
    @FragmentScoped
    fun provideRunAdapter(runDiffCallback: RunDiffCallback) = RunAdapter(runDiffCallback)
}