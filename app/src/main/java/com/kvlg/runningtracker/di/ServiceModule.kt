package com.kvlg.runningtracker.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.kvlg.runningtracker.R
import com.kvlg.runningtracker.ui.main.MainActivity
import com.kvlg.runningtracker.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

/**
 * DI module for [TrackingService]
 *
 * [ServiceScoped] means that for a lifetime of service there is only one instance is going to be of [FusedLocationProviderClient] and etc
 *
 * @author Konstantin Koval
 * @since 05.08.2020
 */
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) = FusedLocationProviderClient(context)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(@ApplicationContext context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            Constants.MAIN_ACTIVITY_PENDING_INTENT,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(@ApplicationContext context: Context, mainActivityPendingIntent: PendingIntent): NotificationCompat.Builder =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_run_24)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string._00_00_00))
            .setContentIntent(mainActivityPendingIntent)
}