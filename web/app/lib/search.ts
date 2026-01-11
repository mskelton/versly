// import * as sqliteVec from 'sqlite-vec'
import { bible } from './db'
import { Node, Span } from './types/usfm'

// sqliteVec.load(bible)

export type SearchResult = {
  book: string
  chapter: string
  range: [string, string]
  relevance?: number
  translation_id: string
}

type ChapterRow = {
  bookAbbreviation: string
  bookId: string
  bookTitle: string
  chapterId: string
  data: string
  translationId: string
}

const getAllChaptersQuery = bible.prepare<{ translationId: string }, ChapterRow>(
  `
    SELECT
      book.id as bookId,
      book.title as bookTitle,
      book.abbreviation as bookAbbreviation,
      chapter.id as chapterId,
      chapter.data,
      chapter.translation_id as translationId
    FROM chapter
    JOIN book ON book.id = chapter.book_id
    WHERE chapter.translation_id = @translationId
  `,
)

export const extractTextFromSpan = (span: Span): string => {
  if (typeof span === 'string') {
    return span
  }

  const [type, content] = span

  if (type === 'v') {
    return ''
  }

  if (typeof content === 'string') {
    return content
  }

  return ''
}

export const extractTextFromNode = (node: Node): string => {
  if (node[0] === 'b') {
    return ''
  }

  if (node[0] === 'table') {
    const rows = node[1] as unknown as [string, Span[]][]
    return rows.map((row) => row[1].map(extractTextFromSpan).join(' ')).join(' ')
  }

  const children = node[1] as Span[]
  return children.map(extractTextFromSpan).join('')
}

const splitIntoVerses = (nodes: Node[]): Map<string, string> => {
  const verses = new Map<string, string>()
  let currentVerseNumber = '1'
  let currentVerseText: string[] = []

  for (const node of nodes) {
    if (node[0] === 'b') {
      continue
    }

    const children = node[0] === 'table' ? [] : (node[1] as Span[])

    for (const span of children) {
      if (typeof span === 'string') {
        currentVerseText.push(span)
      } else if (span[0] === 'v') {
        if (currentVerseText.length > 0) {
          verses.set(currentVerseNumber, currentVerseText.join('').trim())
          currentVerseText = []
        }
        currentVerseNumber = span[1]
      } else if (span[1]) {
        currentVerseText.push(extractTextFromSpan(span))
      }
    }
  }

  if (currentVerseText.length > 0) {
    verses.set(currentVerseNumber, currentVerseText.join('').trim())
  }

  return verses
}

export const searchVerses = (query: string, translationId = 'ESV'): SearchResult[] => {
  if (process.env.NODE_ENV === 'production') {
    return [
      {
        book: 'GEN',
        chapter: '1',
        range: ['1', '1'],
        translation_id: 'ESV',
      },
    ]
  }

  const terms = query
    // Remove verse numbers (e.g., ":13" from "Romans 15:13")
    .replace(/:\d+/g, '')
    .toLowerCase()
    .split(/\s+/)
    .filter((term) => term.length > 0)

  if (terms.length === 0) {
    return []
  }

  const results: SearchResult[] = []
  const chapters = getAllChaptersQuery.all({ translationId })

  for (const chapter of chapters) {
    const nodes: Node[] = JSON.parse(chapter.data)
    const verses = splitIntoVerses(nodes)

    for (const [verseNumber, verseText] of verses) {
      const lowerText = verseText.toLowerCase()
      const matchesAll = terms.every((term) => lowerText.includes(term))

      if (matchesAll) {
        results.push({
          book: chapter.bookTitle,
          chapter: chapter.chapterId,
          range: [verseNumber, verseNumber],
          translation_id: chapter.translationId,
        })
      }
    }
  }

  return results
}
