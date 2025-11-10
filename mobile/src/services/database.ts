import SQLite from 'react-native-sqlite-storage'
import RNFS from 'react-native-fs'
import {
  Passage,
  PassageId,
  BookMetadata,
  Translation,
  Node,
} from '../models/types'

SQLite.enablePromise(true)

const DATABASE_NAME = 'bible.db'
const DATABASE_VERSION = 4

class BibleDatabase {
  private db: SQLite.SQLiteDatabase | null = null

  async init(): Promise<void> {
    this.db = await SQLite.openDatabase({
      name: DATABASE_NAME,
      location: 'default',
    })

    const isInit = await this.isInitialized()
    if (!isInit) {
      const schema = await RNFS.readFileAssets('schema.sql', 'utf8')

      const statements = schema.split(';').filter((s) => s.trim())
      for (const statement of statements) {
        if (statement.trim()) {
          await this.db.executeSql(statement)
        }
      }
    }
  }

  async isInitialized(): Promise<boolean> {
    if (!this.db) return false

    try {
      const [result] = await this.db.executeSql('SELECT 1 FROM book LIMIT 1')
      return result.rows.length > 0
    } catch {
      return false
    }
  }

  async downloadTranslation(
    translationId: string,
    data: string[],
  ): Promise<void> {
    if (!this.db) throw new Error('Database not initialized')

    await this.db.transaction(async (tx) => {
      for (const line of data) {
        const parsed = JSON.parse(line)
        const type = parsed[0]

        switch (type) {
          case 't':
            await tx.executeSql(
              'INSERT OR REPLACE INTO translation(id, title, last_updated) VALUES(?, ?, ?)',
              [parsed[1], parsed[2], parsed[3]],
            )
            break
          case 'b':
            await tx.executeSql(
              'INSERT OR REPLACE INTO book(id, title, abbreviation, sort_order, translation_id) VALUES(?, ?, ?, ?, ?)',
              [parsed[1], parsed[2], parsed[3], parsed[4], translationId],
            )
            break
          case 'c':
            await tx.executeSql(
              'INSERT OR REPLACE INTO chapter(id, book_id, data, translation_id) VALUES(?, ?, ?, ?)',
              [parsed[2], parsed[1], parsed[3], translationId],
            )
            break
          case 'r':
            await tx.executeSql(
              'INSERT OR REPLACE INTO node_range(book_id, chapter_id, start_index, end_index, word_count, translation_id) VALUES(?, ?, ?, ?, ?, ?)',
              [
                parsed[1],
                parsed[2],
                parsed[3],
                parsed[4],
                parsed[5],
                translationId,
              ],
            )
            break
        }
      }
    })
  }

  async getBookList(translationId: string): Promise<BookMetadata[]> {
    if (!this.db) throw new Error('Database not initialized')

    const [result] = await this.db.executeSql(
      `SELECT book.id, book.title, book.abbreviation, COUNT(chapter.id) as chapterCount
       FROM book
       JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
       WHERE book.translation_id = ?
       GROUP BY book.id, book.title
       ORDER BY book.sort_order`,
      [translationId],
    )

    const books: BookMetadata[] = []
    for (let i = 0; i < result.rows.length; i++) {
      const row = result.rows.item(i)
      books.push({
        id: row.id,
        title: row.title,
        abbreviation: row.abbreviation,
        chapterCount: row.chapterCount,
      })
    }

    return books
  }

  async getPassage(
    book: string,
    chapter: string,
    translation: string,
  ): Promise<Passage> {
    if (!this.db) throw new Error('Database not initialized')

    const [result] = await this.db.executeSql(
      `SELECT chapter.id, chapter.data, book.title as bookTitle, book.abbreviation as bookAbbreviation
       FROM chapter
       LEFT JOIN book ON book.id = chapter.book_id AND book.translation_id = chapter.translation_id
       WHERE chapter.id = ? AND chapter.book_id = ? AND chapter.translation_id = ?`,
      [chapter, book, translation],
    )

    if (result.rows.length === 0) {
      throw new Error(`Passage not found: ${book}.${chapter}.${translation}`)
    }

    const row = result.rows.item(0)
    const prefix = `${book}.${chapter}`
    const nodes: Node[] = [
      {
        id: `${prefix}.0`,
        data: ['zc', row.bookTitle, row.id],
      },
    ]

    const chapterData = JSON.parse(row.data)
    for (let i = 0; i < chapterData.length; i++) {
      nodes.push({
        id: `${prefix}.${i + 1}`,
        data: chapterData[i],
      })
    }

    return {
      translation,
      book,
      bookTitle: row.bookTitle,
      bookAbbreviation: row.bookAbbreviation,
      chapter,
      nodes,
    }
  }

  async getNextChapter(
    book: string,
    chapter: string,
    translation: string,
  ): Promise<PassageId | null> {
    if (!this.db) throw new Error('Database not initialized')

    const nextChapterNum = parseInt(chapter) + 1
    const [result] = await this.db.executeSql(
      `SELECT 1 FROM chapter WHERE book_id = ? AND id = ? AND translation_id = ?`,
      [book, nextChapterNum.toString(), translation],
    )

    if (result.rows.length > 0) {
      return { book, chapter: nextChapterNum.toString(), translation }
    }

    const [nextBook] = await this.db.executeSql(
      `SELECT id FROM book
       WHERE translation_id = ? AND sort_order > (
         SELECT sort_order FROM book WHERE id = ? AND translation_id = ?
       )
       ORDER BY sort_order LIMIT 1`,
      [translation, book, translation],
    )

    if (nextBook.rows.length === 0) return null

    return { book: nextBook.rows.item(0).id, chapter: '1', translation }
  }

  async getPreviousChapter(
    book: string,
    chapter: string,
    translation: string,
  ): Promise<PassageId | null> {
    if (!this.db) throw new Error('Database not initialized')

    const prevChapterNum = parseInt(chapter) - 1
    if (prevChapterNum > 0) {
      const [result] = await this.db.executeSql(
        `SELECT 1 FROM chapter WHERE book_id = ? AND id = ? AND translation_id = ?`,
        [book, prevChapterNum.toString(), translation],
      )

      if (result.rows.length > 0) {
        return { book, chapter: prevChapterNum.toString(), translation }
      }
    }

    const [prevBook] = await this.db.executeSql(
      `SELECT book.id, MAX(chapter.id) as lastChapter
       FROM book
       JOIN chapter ON chapter.book_id = book.id AND chapter.translation_id = book.translation_id
       WHERE book.translation_id = ? AND book.sort_order < (
         SELECT sort_order FROM book WHERE id = ? AND translation_id = ?
       )
       GROUP BY book.id
       ORDER BY book.sort_order DESC LIMIT 1`,
      [translation, book, translation],
    )

    if (prevBook.rows.length === 0) return null

    const row = prevBook.rows.item(0)
    return { book: row.id, chapter: row.lastChapter, translation }
  }

  async getTranslations(): Promise<Translation[]> {
    if (!this.db) throw new Error('Database not initialized')

    const [result] = await this.db.executeSql(
      'SELECT id, title, last_updated FROM translation ORDER BY title',
    )

    const translations: Translation[] = []
    for (let i = 0; i < result.rows.length; i++) {
      const row = result.rows.item(i)
      translations.push({
        id: row.id,
        title: row.title,
        lastUpdated: row.last_updated,
        isDownloaded: true,
      })
    }

    return translations
  }
}

export const database = new BibleDatabase()
