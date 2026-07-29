package com.ifpe.tanajura.monitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ifpe.tanajura.MainActivity
import com.ifpe.tanajura.R

class ForecastWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    companion object {
        private const val CHANNEL_ID = "WEATHER_APP"
    }

    override fun doWork(): Result {
        val cityName = inputData.getString("city") ?: return Result.failure()
        showNotification(cityName)
        return Result.success()
    }

    private fun showNotification(cityName: String) {
        val newIntent = Intent(applicationContext, MainActivity::class.java)
        newIntent.addFlags(
            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )
        newIntent.putExtra("city", cityName)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            cityName.hashCode(),
            newIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        createNotificationChannel()

        val notification = NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(cityName)
            .setContentText("Clique para ver previsão do tempo atualizada.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.notify(cityName.hashCode(), notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "WeatherApp",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "WeatherApp Notifications"
        }

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
