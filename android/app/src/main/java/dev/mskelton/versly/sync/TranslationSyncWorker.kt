package dev.mskelton.versly.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.mskelton.versly.persistence.BibleDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class TranslationSyncWorker
    @AssistedInject
    constructor(
        @Assisted private val context: Context,
        @Assisted params: WorkerParameters,
        private val bibleDatabase: BibleDatabase,
    ) : CoroutineWorker(context, params) {
        companion object {
            private const val TAG = "TranslationSyncWorker"
            const val WORK_NAME = "translation_sync_work"
        }

        override suspend fun doWork(): Result =
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Starting translation sync")
                    bibleDatabase.syncTranslations()
                    Log.d(TAG, "Translation sync completed successfully")
                    Result.success()
                } catch (e: Exception) {
                    Log.e(TAG, "Translation sync failed", e)
                    Result.retry()
                }
            }
    }
