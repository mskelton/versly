package dev.mskelton.versly.sync

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncManager {
    private const val TAG = "SyncManager"

    fun startPeriodicSync(context: Context) {
        Log.d(TAG, "Setting up periodic translation sync")

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val syncRequest =
            PeriodicWorkRequestBuilder<TranslationSyncWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                TranslationSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest,
            )

        Log.d(TAG, "Periodic translation sync scheduled")
    }

    @Suppress("unused")
    fun stopPeriodicSync(context: Context) {
        Log.d(TAG, "Stopping periodic translation sync")

        WorkManager.getInstance(context).cancelUniqueWork(TranslationSyncWorker.WORK_NAME)
    }

    @Suppress("unused")
    fun triggerImmediateSync(context: Context) {
        Log.d(TAG, "Triggering immediate translation sync")

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val syncRequest =
            androidx.work
                .OneTimeWorkRequestBuilder<TranslationSyncWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
