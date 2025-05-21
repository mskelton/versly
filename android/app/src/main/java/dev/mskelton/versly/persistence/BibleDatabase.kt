package dev.mskelton.versly.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import dev.mskelton.versly.DEFAULT_TRANSLATION
import dev.mskelton.versly.api.VerslyService
import org.json.JSONArray

data class Passage(
    val id: String,
    val bookTitle: String,
    val bookAbbreviation: String,
    val data: String,
)

class BibleDatabase(
    context: Context,
    private val service: VerslyService,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bible.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "BibleDatabase"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating Bible database...")

        db.execSQL(
            """
            CREATE TABLE translation (
                id TEXT PRIMARY KEY, 
                version INTEGER NOT NULL,
                title TEXT NOT NULL
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE book (
                id TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                translation_id TEXT NOT NULL,
                FOREIGN KEY (translation_id) REFERENCES translation (id)
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE chapter (
                id TEXT PRIMARY KEY,
                data JSON NOT NULL,
                book_id TEXT NOT NULL,
                FOREIGN KEY (book_id) REFERENCES book (id)
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE range (
                start_index INTEGER NOT NULL,
                end_index INTEGER NOT NULL,
                word_count INTEGER NOT NULL,
                chapter_id TEXT NOT NULL,
                PRIMARY KEY (start_index, end_index),
                FOREIGN KEY (chapter_id) REFERENCES chapter (id)
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading Bible database from version $oldVersion to $newVersion...")
    }

    fun isInitialized(): Boolean {
        return readableDatabase.rawQuery(
            "SELECT 1 FROM book WHERE translation_id = ? LIMIT 1",
            arrayOf(DEFAULT_TRANSLATION),
        ).use {
            it.count > 0
        }
    }

    suspend fun downloadTranslation(translation: String) {
        Log.d(TAG, "Downloading translation $translation...")

        val response = service.downloadTranslation(translation)
        if (!response.isSuccessful) {
            Log.e(TAG, "Failed to download translation with status code ${response.code()}")
            return
        }

        val body = response.body()?.source() ?: return
        val insertTranslation = writableDatabase.compileStatement(
            "INSERT OR REPLACE INTO translation(id, version, title) VALUES(?, ?, ?)",
        )
        val insertBook = writableDatabase.compileStatement(
            "INSERT OR REPLACE INTO book(id, title, translation_id) VALUES(?, ?, ?)",
        )
        val insertChapter = writableDatabase.compileStatement(
            "INSERT OR REPLACE INTO chapter(id, data, book_id) VALUES(?, ?, ?)",
        )
        val insertRange = writableDatabase.compileStatement(
            "INSERT OR REPLACE INTO range(start_index, end_index, word_count, chapter_id) VALUES(?, ?, ?, ?)",
        )

        writableDatabase.beginTransaction()
        body.use { source ->
            var batchCount = 0
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                val data = JSONArray(line)

                when (data.getString(0)) {
                    "translation" -> insertTranslation.apply {
                        bindString(1, data.getString(1))
                        bindString(2, data.getString(2))
                        bindString(3, data.getString(3))
                        executeInsert()
                    }

                    "book" -> insertBook.apply {
                        bindString(1, data.getString(1))
                        bindString(2, data.getString(2))
                        bindString(3, translation)
                        executeInsert()
                    }

                    "chapter" -> insertChapter.apply {
                        bindString(1, data.getString(1))
                        bindString(2, data.getString(2))
                        bindString(3, parseBookId(data.getString(1)))
                        executeInsert()
                    }

                    "range" -> insertRange.apply {
                        bindLong(1, data.getLong(2))
                        bindLong(2, data.getLong(3))
                        bindLong(3, data.getLong(4))
                        bindString(4, data.getString(4))
                        executeInsert()
                    }
                }

                if (++batchCount >= 100) {
                    writableDatabase.yieldIfContendedSafely()
                    batchCount = 0
                }
            }
        }
        writableDatabase.setTransactionSuccessful()
        writableDatabase.endTransaction()
    }

    fun getPassage(chapterId: String): Passage {
        Log.d(TAG, "Load passage $chapterId...")

        return readableDatabase.rawQuery(
            """
            SELECT chapter.id, chapter.data, book.title as bookTitle, book.title as bookAbbreviation
            FROM chapter
            JOIN book ON book.id = chapter.book_id
            WHERE chapter.id = ?
            """,
            arrayOf(chapterId),
        ).use {
            it.moveToFirst()

            Passage(
                id = it.getString(0),
                data = it.getString(1),
                bookTitle = it.getString(2),
                bookAbbreviation = it.getString(3),
            )
        }
    }

    private fun parseBookId(chapterId: String): String {
        val (chapter, _, translation) = chapterId.split('.')
        return "${chapter}.${translation}"
    }
}

val LocalBibleDatabase = compositionLocalOf<BibleDatabase> {
    error("No BibleDatabase provided")
}

