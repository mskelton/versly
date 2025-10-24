package dev.mskelton.versly.persistence

import androidx.compose.runtime.saveable.Saver

fun passageSaver(bibleDatabase: BibleDatabase) =
    Saver<List<Passage>, List<String>>(
        save = { passages -> passages.map { "${it.book}.${it.chapter}.${it.translation}" } },
        restore = { saved ->
            saved.map { encoded ->
                val (book, chapter, translation) = encoded.split('.', limit = 3)
                bibleDatabase.getPassage(book = book, chapter = chapter, translation = translation)
            }
        },
    )
