package com.ifpe.tanajura.monitor

import android.app.NotificationManager
import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ifpe.tanajura.model.City
import java.util.concurrent.TimeUnit

class ForecastMonitor(context: Context) {

    private val applicationContext = context.applicationContext
    private val workManager = WorkManager.getInstance(applicationContext)
    private val notificationManager = applicationContext.getSystemService(
        Context.NOTIFICATION_SERVICE
    ) as NotificationManager

    fun updateCity(city: City) {
        cancelCity(city)

        if (!city.isMonitored) return

        val inputData = Data.Builder()
            .putString("city", city.name)
            .build()

        val request = PeriodicWorkRequestBuilder<ForecastWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setInitialDelay(
                duration = 10,
                timeUnit = TimeUnit.SECONDS
            )
            .setInputData(inputData)
            .build()

        workManager.enqueueUniquePeriodicWork(
            city.name,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    fun cancelCity(city: City) {
        workManager.cancelUniqueWork(city.name)
        notificationManager.cancel(city.name.hashCode())
    }

    fun cancelAll() {
        workManager.cancelAllWork()
        notificationManager.cancelAll()
    }
}
