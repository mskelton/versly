package dev.mskelton.versly.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.core.database.sqlite.transaction
import dev.mskelton.versly.api.VerslyService
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.serialization.Serializable
import org.json.JSONArray

@Serializable
data class PassageId(
    val book: String,
    val chapter: String,
    val translation: String,
    val range: List<String>? = null,
)

data class Node(val id: String, val data: JSONArray)

data class Passage(
    val id: PassageId,
    val translation: String,
    val book: String,
    val bookTitle: String,
    val bookAbbreviation: String,
    val chapter: String,
    val nodes: List<Node>,
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
    val lastUpdated: String,
    val isDownloaded: Boolean,
)

class BibleDatabase(private val context: Context, private val service: VerslyService) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "bible.db"
        private const val DATABASE_VERSION = 4
        private const val TAG = "BibleDatabase"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "Creating Bible database from schema.sql")

        val inputStream = context.assets.open("schema.sql")
        val sql = inputStream.bufferedReader().use { it.readText() }

        db.transaction {
            for (statement in sql.split(";")) {
                val trimmed = statement.trim()
                if (trimmed.isNotEmpty()) {
                    db.execSQL(trimmed)
                }
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "Upgrading Bible database from version $oldVersion to $newVersion")

        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE `range` RENAME TO node_range")
        }

        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE translation DROP COLUMN last_updated")
            db.execSQL("ALTER TABLE translation ADD COLUMN last_updated TEXT")
            db.execSQL(
                "UPDATE translation SET last_updated = ?",
                arrayOf("2025-09-18T00:00:00.000Z"),
            )
        }
    }

    fun isInitialized(): Boolean {
        return readableDatabase.rawQuery("SELECT 1 FROM book LIMIT 1", null).use { it.count > 0 }
    }

    @SuppressLint("UseKtx")
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
                "INSERT OR REPLACE INTO translation(id, title, last_updated) VALUES(?, ?, ?)"
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
                "INSERT OR REPLACE INTO node_range(book_id, chapter_id, start_index, end_index, word_count, translation_id) VALUES(?, ?, ?, ?, ?, ?)"
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
                            bindString(6, translationId)
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

    fun getPassage(book: String, chapter: String, translation: String): Passage {
        val id = "$book.$chapter.$translation"
        Log.d(TAG, "Load passage $id")

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
                arrayOf(chapter, book, translation),
            )
            .use {
                it.moveToFirst()

                val prefix = "${book}.${chapter}"
                val nodes =
                    mutableListOf(
                        Node(
                            id = "${prefix}.0",
                            data = JSONArray(listOf("zc", it.getString(2), it.getString(0))),
                        )
                    )
                val data = JSONArray(it.getString(1))

                for (i in 0 until data.length()) {
                    nodes.add(Node(id = "$prefix.${i + 1}", data = data.getJSONArray(i)))
                }

                Passage(
                    id = PassageId(book = book, chapter = chapter, translation = translation),
                    translation = translation,
                    book = book,
                    bookTitle = it.getString(2),
                    bookAbbreviation = it.getString(3),
                    chapter = chapter,
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

    fun getNextBook(passage: Passage): BookMetadata? {
        Log.d(TAG, "Load next book after ${passage.book}")

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
                arrayOf(passage.translation, passage.book, passage.translation),
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

    fun getPreviousBook(passage: Passage): BookMetadata? {
        Log.d(TAG, "Load previous book before ${passage.book}")

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
                arrayOf(passage.translation, passage.book, passage.translation),
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
                SELECT id, title, last_updated, EXISTS(
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
                            lastUpdated = it.getString(2),
                            isDownloaded = it.getInt(3) == 1,
                        )
                    )
                }
            }

        return translations
    }

    suspend fun syncTranslations() {
        Log.d(TAG, "Starting translation sync")

        val response = service.getTranslations()
        if (!response.isSuccessful) {
            Log.e(TAG, "Failed to fetch translations from server: ${response.code()}")
            return
        }

        val serverTranslations = response.body() ?: return
        val localTranslations = getAvailableTranslations()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

        for (localTranslation in localTranslations) {
            val serverTranslation =
                serverTranslations.find { it.id == localTranslation.id } ?: continue

            val serverDate = dateFormat.parse(serverTranslation.lastUpdated)
            val localDate = dateFormat.parse(localTranslation.lastUpdated)
            val needsUpdate = serverDate != null && localDate == null && serverDate.after(localDate)

            if (needsUpdate) {
                Log.d(TAG, "Updating translation: ${localTranslation.id}")
                downloadTranslation(localTranslation.id)
                Log.d(TAG, "Successfully updated translation: ${localTranslation.id}")
            }
        }
    }
}

val LocalBibleDatabase = compositionLocalOf<BibleDatabase> { error("No BibleDatabase provided") }
