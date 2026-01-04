import { notFound } from 'next/navigation'
import { bible } from './db'
import { buildPassageId, PassageId } from './passageId'
import { Node } from './types/usfm'

const getPassageQuery = bible.prepare<
  { bookId: string; chapterId: string; translationId: string },
  { bookAbbreviation: string; bookTitle: string; data: string }
>(
  `
    SELECT book.title as bookTitle, book.abbreviation as bookAbbreviation, chapter.data
    FROM chapter
    JOIN book ON book.id = chapter.book_id
    WHERE book.id = @bookId AND chapter.id = @chapterId AND chapter.translation_id = @translationId
  `,
)

export type Passage = {
  bookAbbreviation: string
  bookTitle: string
  id: string
  nodes: Node[]
}

export async function getPassage(passageId: PassageId): Promise<Passage> {
  const row = getPassageQuery.get({
    chapterId: passageId.chapter,
    translationId: passageId.translation,
    bookId: passageId.book,
  })
  if (!row) {
    notFound()
  }

  const nodes: Node[] = passageId.verses
    ? JSON.parse(row.data)
    : [['c', passageId.chapter], ...JSON.parse(row.data)]

  return {
    bookAbbreviation: row.bookAbbreviation,
    bookTitle: row.bookTitle,
    id: buildPassageId(passageId),
    nodes,
  }
}
