package dev.mskelton.versly.persistence

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.compositionLocalOf

data class Passage(
    val bookTitle: String,
    val data: String
)

class BibleDatabase(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bible.db"
        private const val DATABASE_VERSION = 1
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
                title TEXT NOT NULL
            )
            """.trimMargin()
        )

        db.execSQL(
            """
            CREATE TABLE book (
                ref TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                translation_ref TEXT NOT NULL,
                FOREIGN KEY (translation_ref) REFERENCES translation (ref)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE chapter (
                ref TEXT PRIMARY KEY,
                data JSON NOT NULL,
                book_ref TEXT NOT NULL,
                FOREIGN KEY (book_ref) REFERENCES book (ref)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE chapter (
                ref TEXT PRIMARY KEY,
                data JSON NOT NULL,
                book_ref TEXT NOT NULL,
                FOREIGN KEY (book_ref) REFERENCES book (ref)
            )
            """.trimIndent()
        )

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

//    private fun

    fun download(translation: String) {

    }

    fun getPassage(chapterRef: String): Passage {
        val cursor = readableDatabase.rawQuery(
            """
            SELECT book.title as bookTitle, chapter.data
            FROM chapter
            JOIN book ON book.ref = chapter.book_ref
            WHERE chapter.ref = ?
            """,
            arrayOf(chapterRef)
        )

        cursor.moveToFirst()
        val bookTitle = cursor.getString(0)
        val data = cursor.getString(1)
        val item = Passage(bookTitle, data)

        cursor.close()
        return item
    }
}


val LocalBibleDatabase = compositionLocalOf<BibleDatabase> {
    error("No BibleDatabase provided")
}
