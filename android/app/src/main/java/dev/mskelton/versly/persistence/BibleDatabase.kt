package dev.mskelton.versly.persistence

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.compose.runtime.compositionLocalOf
import androidx.core.database.sqlite.transaction
import dev.mskelton.versly.api.VerslyService
import kotlinx.serialization.Serializable
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
data class PassageId(
    val book: String,
    val chapter: String,
    val translation: String,
    val range: List<String>? = null,
)

data class Node(
    val id: String,
    val data: JSONArray,
)

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

data class HydratedPassageId(
    val id: PassageId,
    val bookTitle: String,
    val text: String,
)

open class BibleDatabase(
    private val context: Context,
    private val service: VerslyService,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int,
    ) {
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

        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS node_range")
        }
    }

    fun isInitialized(): Boolean = readableDatabase.rawQuery("SELECT 1 FROM book LIMIT 1", null).use { it.count > 0 }

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
                "INSERT OR REPLACE INTO translation(id, title, last_updated) VALUES(?, ?, ?)",
            )
        val insertBook =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO book(id, title, abbreviation, sort_order, translation_id) VALUES(?, ?, ?, ?, ?)",
            )
        val insertChapter =
            writableDatabase.compileStatement(
                "INSERT OR REPLACE INTO chapter(id, book_id, data, translation_id) VALUES(?, ?, ?, ?)",
            )

        writableDatabase.beginTransaction()
        body.use { source ->
            var batchCount = 0
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                val data = JSONArray(line)

                when (data.getString(0)) {
                    "t" -> {
                        insertTranslation.apply {
                            bindString(1, data.getString(1))
                            bindString(2, data.getString(2))
                            bindString(3, data.getString(3))
                            executeInsert()
                        }
                    }

                    "b" -> {
                        insertBook.apply {
                            bindString(1, data.getString(1))
                            bindString(2, data.getString(2))
                            bindString(3, data.getString(3))
                            bindLong(4, data.getLong(4))
                            bindString(5, translationId)
                            executeInsert()
                        }
                    }

                    "c" -> {
                        insertChapter.apply {
                            bindString(1, data.getString(2))
                            bindString(2, data.getString(1))
                            bindString(3, data.getString(3))
                            bindString(4, translationId)
                            executeInsert()
                        }
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

    open fun getBookList(translationId: String): List<BookMetadata> {
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
            ).use {
                while (it.moveToNext()) {
                    books.add(
                        BookMetadata(
                            id = it.getString(0),
                            title = it.getString(1),
                            abbreviation = it.getString(2),
                            chapterCount = it.getInt(3),
                        ),
                    )
                }
            }

        return books
    }

    /**
     * Gets a passage. If range is null, returns the full chapter. If range is provided, it's always
     * a subset (partial chapter), never the full chapter.
     */
    open fun getPassage(
        book: String,
        chapter: String,
        translation: String,
        range: List<String>? = null,
    ): Passage =
        readableDatabase
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
            ).use {
                it.moveToFirst()

                val prefix = "$book.$chapter"
                val chapterNode =
                    Node(
                        id = "$prefix.0",
                        data = JSONArray(listOf("zc", it.getString(2), it.getString(0))),
                    )
                val data = JSONArray(it.getString(1))

                val nodes =
                    if (range != null) {
                        mutableListOf<Node>().apply {
                            add(chapterNode)
                            addAll(filterNodesByRange(data, range, prefix))
                        }
                    } else {
                        mutableListOf<Node>().apply {
                            add(chapterNode)
                            for (i in 0 until data.length()) {
                                add(Node(id = "$prefix.${i + 1}", data = data.getJSONArray(i)))
                            }
                        }
                    }

                Passage(
                    id =
                        PassageId(
                            book = book,
                            chapter = chapter,
                            translation = translation,
                            range = range,
                        ),
                    translation = translation,
                    book = book,
                    bookTitle = it.getString(2),
                    bookAbbreviation = it.getString(3),
                    chapter = chapter,
                    nodes = nodes,
                )
            }

    open fun getBookMetadata(
        bookId: String,
        translationId: String,
    ): BookMetadata? {
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
            ).use {
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

    fun bulkGetPassages(passages: List<PassageId>): List<HydratedPassageId> {
        if (passages.isEmpty()) return emptyList()

        Log.d(TAG, "Bulk loading ${passages.size} passages")

        // Get unique books and chapters
        val uniqueBooks = passages.map { it.book }.distinct()
        val uniqueChapters = passages.map { Pair(it.book, it.chapter) }.distinct()
        val translationId = passages.first().translation

        // Bulk fetch book titles
        val bookPlaceholders = uniqueBooks.joinToString(",") { "?" }
        val bookTitles = mutableMapOf<String, String>()
        readableDatabase
            .rawQuery(
                """
                SELECT id, title FROM book
                WHERE id IN ($bookPlaceholders)
                AND translation_id = ?
                """,
                uniqueBooks.toTypedArray() + translationId,
            ).use {
                while (it.moveToNext()) {
                    bookTitles[it.getString(0)] = it.getString(1)
                }
            }

        val chapterData = mutableMapOf<Pair<String, String>, JSONArray>()
        for ((book, chapter) in uniqueChapters) {
            readableDatabase
                .rawQuery(
                    """
                    SELECT data FROM chapter
                    WHERE book_id = ?
                    AND id = ?
                    AND translation_id = ?
                    """,
                    arrayOf(book, chapter, translationId),
                ).use {
                    if (it.moveToFirst()) {
                        chapterData[Pair(book, chapter)] = JSONArray(it.getString(0))
                    }
                }
        }

        return passages.map { passage ->
            val bookTitle = bookTitles[passage.book] ?: passage.book
            val data = chapterData[Pair(passage.book, passage.chapter)]
            val text =
                if (data != null && passage.range != null) extractText(data, passage.range) else ""

            HydratedPassageId(id = passage, bookTitle = bookTitle, text = text)
        }
    }

    private fun extractText(
        chapterData: JSONArray,
        range: List<String>,
    ): String {
        val startVerse = range.getOrNull(0)?.toIntOrNull() ?: 1
        val endVerse = range.getOrNull(1)?.toIntOrNull() ?: startVerse

        val textParts = mutableListOf<String>()
        for (i in 0 until chapterData.length()) {
            val node = chapterData.getJSONArray(i)
            val type = node.getString(0)

            // Check if this is a verse node and within range
            if (type == "zv") {
                val verseNum = node.getString(1).toIntOrNull() ?: continue
                if (verseNum in startVerse..endVerse) {
                    // Extract text from subsequent nodes until next verse
                    for (j in i + 1 until chapterData.length()) {
                        val textNode = chapterData.getJSONArray(j)
                        val textType = textNode.getString(0)
                        if (textType == "zv") break
                        if (textType == "zt") {
                            textParts.add(textNode.getString(1))
                        }
                    }
                }
            }
        }

        return textParts.joinToString(" ").trim()
    }

    open fun getNextBook(passageId: PassageId): BookMetadata? {
        Log.d(TAG, "Load next book after ${passageId.book}")

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
                arrayOf(passageId.translation, passageId.book, passageId.translation),
            ).use {
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

    open fun getPreviousBook(passageId: PassageId): BookMetadata? {
        Log.d(TAG, "Load previous book before ${passageId.book}")

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
                arrayOf(passageId.translation, passageId.book, passageId.translation),
            ).use {
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
                """.trimIndent(),
                null,
            ).use {
                while (it.moveToNext()) {
                    translations.add(
                        Translation(
                            id = it.getString(0),
                            title = it.getString(1),
                            lastUpdated = it.getString(2),
                            isDownloaded = it.getInt(3) == 1,
                        ),
                    )
                }
            }

        return translations
    }

    fun deleteTranslation(translationId: String) {
        Log.d(TAG, "Deleting translation $translationId")

        writableDatabase.transaction {
            execSQL("DELETE FROM chapter WHERE translation_id = ?", arrayOf(translationId))
            execSQL("DELETE FROM book WHERE translation_id = ?", arrayOf(translationId))
        }
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
