package dev.mskelton.versly.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.core.database.sqlite.transaction
import dev.mskelton.versly.api.VerslyService
import org.json.JSONArray

data class ChapterId(val book: String, val chapter: String, val translation: String) {
    override fun toString(): String {
        return "$book.$chapter.$translation"
    }
}

data class Passage(
    val id: ChapterId,
    val bookTitle: String,
    val bookAbbreviation: String,
    val nodes: JSONArray,
)

data class BookMetadata(
    val id: String,
    val title: String,
    val abbreviation: String,
    val chapterCount: Int,
)

data class Translation(
    val id: String,
    val title: String,
    val version: Int,
    val isDownloaded: Boolean,
)

class BibleDatabase(private val context: Context, private val service: VerslyService) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "bible.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "BibleDatabase"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating Bible database from schema.sql")
        db.transaction { initializeDatabase(db) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading Bible database from version $oldVersion to $newVersion")

        db.transaction {
            db.execSQL("DROP TABLE IF EXISTS range")
            db.execSQL("DROP TABLE IF EXISTS chapter")
            db.execSQL("DROP TABLE IF EXISTS book")
            db.execSQL("DROP TABLE IF EXISTS translation")

            initializeDatabase(db)
        }
    }

    fun isInitialized(): Boolean {
        return readableDatabase.rawQuery("SELECT 1 FROM book LIMIT 1", null).use { it.count > 0 }
    }

    private fun initializeDatabase(db: SQLiteDatabase) {
        val inputStream = context.assets.open("schema.sql")
        val sql = inputStream.bufferedReader().use { it.readText() }

        for (statement in sql.split(";")) {
            val trimmed = statement.trim()
            if (trimmed.isNotEmpty()) {
                db.execSQL(trimmed)
            }
        }
    }

    suspend fun downloadTranslation(translationId: String) {
        Log.d(TAG, "Downloading translation $translationId")

        val response = service.downloadTranslation(translationId)
        if (!response.isSuccessful) {
            Log.e(TAG, "Failed to download translation with status code ${response.code()}")
            return
        }

        val body = response.body()?.source() ?: return
        val insertTranslation =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO translation(id, version, title) VALUES(?, ?, ?)"
            )
        val insertBook =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO book(id, title, abbreviation, sort_order, translation_id) VALUES(?, ?, ?, ?, ?)"
            )
        val insertChapter =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO chapter(id, book_id, data, translation_id) VALUES(?, ?, ?, ?)"
            )
        val insertRange =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO range(book_id, chapter_id, start_index, end_index, word_count, translation_id) VALUES(?, ?, ?, ?, ?, ?)"
            )

        writableDatabase.beginTransaction()
        body.use { source ->
            var batchCount = 0
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                val data = JSONArray(line)

                when (data.getString(0)) {
                    "t" ->
                        insertTranslation.apply {
                            bindString(1, data.getString(1))
                            bindString(2, data.getString(2))
                            bindString(3, data.getString(3))
                            executeInsert()
                        }

                    "b" ->
                        insertBook.apply {
                            bindString(1, data.getString(1))
                            bindString(2, data.getString(2))
                            bindString(3, data.getString(3))
                            bindLong(4, data.getLong(4))
                            bindString(5, translationId)
                            executeInsert()
                        }

                    "c" ->
                        insertChapter.apply {
                            bindString(1, data.getString(2))
                            bindString(2, data.getString(1))
                            bindString(3, data.getString(3))
                            bindString(4, translationId)
                            executeInsert()
                        }

                    "r" ->
                        insertRange.apply {
                            bindString(1, data.getString(1))
                            bindString(2, data.getString(2))
                            bindLong(3, data.getLong(3))
                            bindLong(4, data.getLong(4))
                            bindLong(5, data.getLong(5))
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

    fun getBookList(translationId: String): List<BookMetadata> {
        Log.d(TAG, "Load books for $translationId")

        val books = mutableListOf<BookMetadata>()
        readableDatabase
            .rawQuery(
                """
            SELECT book.id, book.title, book.abbreviation as abbreviation, COUNT(chapter.id)
            FROM book
            JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
            WHERE book.translation_id = ?
            GROUP BY book.id, book.title
            ORDER BY book.sort_order
            """,
                arrayOf(translationId),
            )
            .use {
                while (it.moveToNext()) {
                    books.add(
                        BookMetadata(
                            id = it.getString(0),
                            title = it.getString(1),
                            abbreviation = it.getString(2),
                            chapterCount = it.getInt(3),
                        )
                    )
                }
            }

        return books
    }

    fun getPassage(chapterId: ChapterId): Passage {
        Log.d(TAG, "Load passage $chapterId")

        return readableDatabase
            .rawQuery(
                """
            SELECT
                chapter.id, 
                chapter.data,
                book.title as bookTitle, 
                book.abbreviation as bookAbbreviation
            FROM chapter
            LEFT JOIN book ON book.id = chapter.book_id AND book.translation_id = chapter.translation_id
            WHERE chapter.id = ?
            AND chapter.book_id = ?
            AND chapter.translation_id = ?
            """,
                arrayOf(chapterId.chapter, chapterId.book, chapterId.translation),
            )
            .use {
                it.moveToFirst()

                val nodes = JSONArray()
                nodes.put(JSONArray(listOf("zc", it.getString(2), it.getInt(0))))

                val original = JSONArray(it.getString(1))
                for (i in 0 until original.length()) {
                    nodes.put(original.get(i))
                }

                Passage(
                    id = chapterId,
                    bookTitle = it.getString(2),
                    bookAbbreviation = it.getString(3),
                    nodes = nodes,
                )
            }
    }

    fun getBookMetadata(bookId: String, translationId: String): BookMetadata? {
        Log.d(TAG, "Load book metadata for $bookId")

        return readableDatabase
            .rawQuery(
                """
            SELECT book.id, book.title, book.abbreviation, COUNT(chapter.id)
            FROM book
            LEFT JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
            WHERE book.id = ? AND book.translation_id = ?
            GROUP BY book.id, book.title
            """,
                arrayOf(bookId, translationId),
            )
            .use {
                if (it.moveToFirst()) {
                    BookMetadata(
                        id = it.getString(0),
                        title = it.getString(1),
                        abbreviation = it.getString(2),
                        chapterCount = it.getInt(3),
                    )
                } else {
                    null
                }
            }
    }

    fun getNextBook(currentBookId: String, translationId: String): BookMetadata? {
        Log.d(TAG, "Load next book after $currentBookId")

        return readableDatabase
            .rawQuery(
                """
            SELECT book.id, book.title, book.abbreviation, COUNT(chapter.id)
            FROM book
            LEFT JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
            WHERE book.translation_id = ? 
            AND book.sort_order > (
                SELECT sort_order FROM book WHERE id = ? AND translation_id = ?
            )
            GROUP BY book.id, book.title, book.sort_order
            ORDER BY book.sort_order
            LIMIT 1
            """,
                arrayOf(translationId, currentBookId, translationId),
            )
            .use {
                if (it.moveToFirst()) {
                    BookMetadata(
                        id = it.getString(0),
                        title = it.getString(1),
                        abbreviation = it.getString(2),
                        chapterCount = it.getInt(3),
                    )
                } else {
                    null
                }
            }
    }

    fun getPreviousBook(currentBookId: String, translationId: String): BookMetadata? {
        Log.d(TAG, "Load previous book before $currentBookId")

        return readableDatabase
            .rawQuery(
                """
            SELECT book.id, book.title, book.abbreviation, COUNT(chapter.id)
            FROM book
            LEFT JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
            WHERE book.translation_id = ? 
            AND book.sort_order < (
                SELECT sort_order FROM book WHERE id = ? AND translation_id = ?
            )
            GROUP BY book.id, book.title, book.sort_order
            ORDER BY book.sort_order DESC
            LIMIT 1
            """,
                arrayOf(translationId, currentBookId, translationId),
            )
            .use {
                if (it.moveToFirst()) {
                    BookMetadata(
                        id = it.getString(0),
                        title = it.getString(1),
                        abbreviation = it.getString(2),
                        chapterCount = it.getInt(3),
                    )
                } else {
                    null
                }
            }
    }

    fun getAvailableTranslations(): List<Translation> {
        Log.d(TAG, "Getting available translations")

        val translations = mutableListOf<Translation>()
        readableDatabase
            .rawQuery(
                """
                SELECT id, title, version, EXISTS(
                    SELECT 1
                    FROM book
                    WHERE book.translation_id = translation.id
                )
                FROM translation
                ORDER BY id
                """
                    .trimIndent(),
                null,
            )
            .use {
                while (it.moveToNext()) {
                    translations.add(
                        Translation(
                            id = it.getString(0),
                            title = it.getString(1),
                            version = it.getInt(2),
                            isDownloaded = it.getInt(3) == 1,
                        )
                    )
                }
            }

        return translations
    }
}

val LocalBibleDatabase = compositionLocalOf<BibleDatabase> { error("No BibleDatabase provided") }
