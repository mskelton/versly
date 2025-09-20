package dev.mskelton.versly.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.mskelton.versly.api.BASE_URL
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.BibleDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TranslationSyncWorker(private val context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TranslationSyncWorker"
        const val WORK_NAME = "translation_sync_work"
    }

    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting translation sync")

                val retrofit =
                    Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                val service = retrofit.create(VerslyService::class.java)
                val bibleDatabase = BibleDatabase(context, service)

                bibleDatabase.syncTranslations()
                Log.d(TAG, "Translation sync completed successfully")
                Result.success()
            } catch (e: Exception) {
                Log.e(TAG, "Translation sync failed", e)
                Result.retry()
            }
        }
}
