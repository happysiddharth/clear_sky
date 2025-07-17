package com.weather.clearsky.feature.alerts.data.work

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    fun schedulePeriodicAlertCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .build()
        
        val alertCheckRequest = PeriodicWorkRequestBuilder<AlertCheckWorker>(
            repeatInterval = CHECK_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = FLEX_INTERVAL_MINUTES,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(ALERT_CHECK_TAG)
            .build()
        
        // Use ExistingPeriodicWorkPolicy.REPLACE to update the work if it already exists
        workManager.enqueueUniquePeriodicWork(
            AlertCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Keep existing work if it's already scheduled
            alertCheckRequest
        )
    }
    
    fun scheduleImmediateAlertCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val immediateCheckRequest = OneTimeWorkRequestBuilder<AlertCheckWorker>()
            .setConstraints(constraints)
            .addTag(ALERT_CHECK_TAG)
            .build()
        
        workManager.enqueue(immediateCheckRequest)
    }
    
    fun cancelAlertCheck() {
        workManager.cancelUniqueWork(AlertCheckWorker.WORK_NAME)
    }
    
    fun cancelAllAlertWork() {
        workManager.cancelAllWorkByTag(ALERT_CHECK_TAG)
    }
    
    fun getAlertWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData(AlertCheckWorker.WORK_NAME)
    
    companion object {
        private const val CHECK_INTERVAL_MINUTES = 15L // Check every 15 minutes
        private const val FLEX_INTERVAL_MINUTES = 5L   // Allow 5 minutes flexibility
        private const val ALERT_CHECK_TAG = "alert_check"
    }
} 