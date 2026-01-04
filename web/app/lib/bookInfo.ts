import { buildChapterId, PassageId } from './passageId'

const bookInfo: [bookId: string, title: string, abbreviation: string, chapters: number][] = [
  ['GEN', 'Genesis', 'Gen', 50],
  ['EXO', 'Exodus', 'Exod', 40],
  ['LEV', 'Leviticus', 'Lev', 27],
  ['NUM', 'Numbers', 'Num', 36],
  ['DEU', 'Deuteronomy', 'Deut', 34],
  ['JOS', 'Joshua', 'Josh', 24],
  ['JDG', 'Judges', 'Judg', 21],
  ['RUT', 'Ruth', 'Ruth', 4],
  ['1SA', '1 Samuel', '1 Sam', 31],
  ['2SA', '2 Samuel', '2 Sam', 24],
  ['1KI', '1 Kings', '1 Kng', 22],
  ['2KI', '2 Kings', '2 Kng', 25],
  ['1CH', '1 Chronicles', '1 Chr', 29],
  ['2CH', '2 Chronicles', '2 Chr', 36],
  ['EZR', 'Ezra', 'Ezra', 10],
  ['NEH', 'Nehemiah', 'Neh', 13],
  ['EST', 'Esther', 'Esth', 10],
  ['JOB', 'Job', 'Job', 42],
  ['PSA', 'Psalms', 'Psa', 150],
  ['PRO', 'Proverbs', 'Prov', 31],
  ['ECC', 'Ecclesiastes', 'Eccl', 12],
  ['SNG', 'Song of Songs', 'Song', 8],
  ['ISA', 'Isaiah', 'Isa', 66],
  ['JER', 'Jeremiah', 'Jer', 52],
  ['LAM', 'Lamentations', 'Lam', 5],
  ['EZK', 'Ezekiel', 'Ezek', 48],
  ['DAN', 'Daniel', 'Dan', 12],
  ['HOS', 'Hosea', 'Hos', 14],
  ['JOL', 'Joel', 'Joel', 3],
  ['AMO', 'Amos', 'Amos', 9],
  ['OBA', 'Obadiah', 'Obad', 1],
  ['JON', 'Jonah', 'Jonah', 4],
  ['MIC', 'Micah', 'Mic', 7],
  ['NAM', 'Nahum', 'Nah', 3],
  ['HAB', 'Habakkuk', 'Hab', 3],
  ['ZEP', 'Zephaniah', 'Zeph', 3],
  ['HAG', 'Haggai', 'Hag', 2],
  ['ZEC', 'Zechariah', 'Zech', 14],
  ['MAL', 'Malachi', 'Mal', 4],
  ['MAT', 'Matthew', 'Matt', 28],
  ['MRK', 'Mark', 'Mark', 16],
  ['LUK', 'Luke', 'Luke', 24],
  ['JHN', 'John', 'John', 21],
  ['ACT', 'Acts', 'Acts', 28],
  ['ROM', 'Romans', 'Rom', 16],
  ['1CO', '1 Corinthians', '1 Cor', 16],
  ['2CO', '2 Corinthians', '2 Cor', 13],
  ['GAL', 'Galatians', 'Gal', 6],
  ['EPH', 'Ephesians', 'Eph', 6],
  ['PHP', 'Philippians', 'Phil', 4],
  ['COL', 'Colossians', 'Col', 4],
  ['1TH', '1 Thessalonians', '1 Th', 5],
  ['2TH', '2 Thessalonians', '2 Th', 3],
  ['1TI', '1 Timothy', '1 Tim', 6],
  ['2TI', '2 Timothy', '2 Tim', 4],
  ['TIT', 'Titus', 'Titus', 3],
  ['PHM', 'Philemon', 'Phlm', 1],
  ['HEB', 'Hebrews', 'Heb', 13],
  ['JAS', 'James', 'James', 5],
  ['1PE', '1 Peter', '1 Pet', 5],
  ['2PE', '2 Peter', '2 Pet', 3],
  ['1JN', '1 John', '1 Jhn', 5],
  ['2JN', '2 John', '2 Jhn', 1],
  ['3JN', '3 John', '3 Jhn', 1],
  ['JUD', 'Jude', 'Jude', 1],
  ['REV', 'Revelation', 'Rev', 22],
]

export function getPreviousChapter(passageId: PassageId) {
  return getChapter(passageId, -1)
}

export function getNextChapter(passageId: PassageId) {
  return getChapter(passageId, 1)
}

function getChapter(passageId: PassageId, direction: -1 | 1) {
  const bookIndex = bookInfo.findIndex(([id]) => id === passageId.book)
  const totalChapters = bookInfo[bookIndex][3]

  let bookId = passageId.book
  let chapterId = parseInt(passageId.chapter) + direction

  if (direction === -1 && chapterId < 1) {
    bookId = bookInfo[bookIndex - 1]?.[0]
    chapterId = bookInfo[bookIndex - 1]?.[3]

    if (!bookId || !chapterId) {
      return null
    }
  } else if (direction === 1 && chapterId > totalChapters) {
    bookId = bookInfo[bookIndex + 1]?.[0]
    chapterId = 1

    if (!bookId) {
      return null
    }
  }

  return `/${buildChapterId({
    book: bookId,
    chapter: chapterId.toString(),
    translation: passageId.translation,
    verses: null,
  })}`
}

export function getPassageName(passageId: PassageId) {
  const bookName = bookInfo.find(([Id]) => Id === passageId.book)![1]

  return `${bookName} ${passageId.chapter} ${passageId.translation}`
}

export function getBookIdFromTitle(title: string): string | null {
  const book = bookInfo.find(([, bookTitle]) => bookTitle === title)
  return book ? book[0] : null
}

export function getBookTitle(book: string) {
  return bookInfo.find(([id]) => id === book)?.[1] ?? book
}

export function parsePassageQuery(query: string): { book: string; chapter: string } | null {
  const normalized = query.trim().toLowerCase()

  // Try to match: "book chapter" or "book:chapter" or "book.chapter"
  const match = normalized.match(/^(.+?)[\s:.](\d+)$/)

  let bookName: string
  let chapter: string

  if (match) {
    // Has chapter number
    const [, bookPart, chapterPart] = match
    bookName = bookPart.trim()
    chapter = chapterPart
  } else {
    // No chapter number, try to match just the book name
    bookName = normalized
    chapter = '1' // Default to chapter 1
  }

  // Try to find book by:
  // 1. Exact ID match (e.g., "JHN")
  // 2. Full title match (e.g., "John", "Genesis")
  // 3. Abbreviation match (e.g., "Gen", "Matt")
  // 4. Partial title match (e.g., "1 cor" -> "1 Corinthians")

  const book = bookInfo.find(([id, title, abbreviation]) => {
    const idLower = id.toLowerCase()
    const titleLower = title.toLowerCase()
    const abbrevLower = abbreviation.toLowerCase()

    return (
      idLower === bookName ||
      titleLower === bookName ||
      abbrevLower === bookName ||
      titleLower.startsWith(bookName) ||
      abbrevLower.startsWith(bookName)
    )
  })

  if (!book) {
    return null
  }

  // Validate chapter number is within valid range
  const chapterNum = parseInt(chapter, 10)
  const [, , , maxChapters] = book
  if (chapterNum < 1 || chapterNum > maxChapters) {
    return null
  }

  return {
    book: book[0],
    chapter,
  }
}
