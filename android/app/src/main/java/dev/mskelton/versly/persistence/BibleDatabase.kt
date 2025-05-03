package dev.mskelton.versly.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.compositionLocalOf
import dev.mskelton.versly.api.VerslyService
import org.json.JSONArray

data class Passage(
    val bookTitle: String, val data: String,
)

class BibleDatabase(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "bible.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        db?.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE translation (
                ref TEXT PRIMARY KEY, 
                version INTEGER NOT NULL,
                title TEXT NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE book (
                ref TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                translation_ref TEXT NOT NULL,
                FOREIGN KEY (translation_ref) REFERENCES translation (ref)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE chapter (
                ref TEXT PRIMARY KEY,
                data JSON NOT NULL,
                book_ref TEXT NOT NULL,
                FOREIGN KEY (book_ref) REFERENCES book (ref)
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE range (
                start_index INTEGER NOT NULL,
                end_index INTEGER NOT NULL,
                word_count INTEGER NOT NULL,
                chapter_ref TEXT NOT NULL,
                PRIMARY KEY (start_index, end_index),
                FOREIGN KEY (chapter_ref) REFERENCES chapter (ref)
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        println("Upgrading database from version $oldVersion to $newVersion")
        db.execSQL("DROP TABLE IF EXISTS translation")
        db.execSQL("DROP TABLE IF EXISTS book")
        db.execSQL("DROP TABLE IF EXISTS chapter")
        db.execSQL("DROP TABLE IF EXISTS range")
        onCreate(db)
    }

    fun isInitialized(): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM translation",
            null,
        )

        val isInitialized = cursor.count > 0
        cursor.close()
        println("isInitialized: $isInitialized")

        return isInitialized
    }

    suspend fun download(
        service: VerslyService,
        translation: String,
    ) {
        val response = service.downloadTranslation(translation)
        if (!response.isSuccessful) {
            println("Failed to download translation with status code ${response.code()}")
            return
        }

        val body = response.body()?.source() ?: return
        val insertTranslation =
            writableDatabase.compileStatement("INSERT OR REPLACE INTO translation(ref, title) VALUES(?, ?)")
        val insertBook =
            writableDatabase.compileStatement("INSERT OR REPLACE INTO book(ref, title, translation_ref) VALUES(?, ?, ?)")
        val insertChapter =
            writableDatabase.compileStatement("INSERT OR REPLACE INTO chapter(ref, data, book_ref) VALUES(?, ?, ?)")
        val insertRange =
            writableDatabase.compileStatement("INSERT OR REPLACE INTO range(start_index, end_index, word_count, chapter_ref) VALUES(?, ?, ?, ?)")

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
                        bindString(3, parseBookRef(data.getString(1)))
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

    fun getPassage(chapterRef: String): Passage {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT book.title as bookTitle, chapter.data
            FROM chapter
            JOIN book ON book.ref = chapter.book_ref
            WHERE chapter.ref = ?
            """, arrayOf(chapterRef)
        )

        cursor.moveToFirst()
        val bookTitle = cursor.getString(0)
        val data = cursor.getString(1)
        val item = Passage(bookTitle, data)

        cursor.close()
        return item
    }

    private fun parseBookRef(chapterRef: String): String {
        val (chapter, _, translation) = chapterRef.split('.')
        return "${chapter}.${translation}"
    }
}

val LocalBibleDatabase = compositionLocalOf<BibleDatabase> {
    error("No BibleDatabase provided")
}

