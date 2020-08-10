package com.kvlg.runningtracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.kvlg.runningtracker.db.RunDatabase
import com.kvlg.runningtracker.utils.Constants
import com.kvlg.runningtracker.utils.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

/**
 * Application dagger module.
 * [InstallIn] means that objects will be created at the start of application and destroyed at the end of application.
 * [Singleton] means that only one instance of an object will be created
 *
 * @author Konstantin Koval
 * @since 19.07.2020
 */
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext appContext: Context
    ) = Room.databaseBuilder(
        appContext,
        RunDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Provides
    @Singleton
    fun provideRunDAO(db: RunDatabase) = db.getRunDao()

    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context) =
        context.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideName(sharedPreferences: SharedPreferences) = sharedPreferences.getString(Constants.KEY_PREF_NAME, "") ?: ""

    @Provides
    @Singleton
    fun provideWeight(sharedPreferences: SharedPreferences) = sharedPreferences.getFloat(Constants.KEY_PREF_WEIGHT, 70f)


    @Provides
    @Singleton
    fun providesFirstTimeToggle(sharedPreferences: SharedPreferences) = sharedPreferences.getBoolean(Constants.KEY_PREF_FIRST_TIME_TOGGLE, true)
}
