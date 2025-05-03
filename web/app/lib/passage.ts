import { notFound } from 'next/navigation'
import { bible, sql } from './db'
import { buildChapterId, buildPassageId, PassageId } from './passageId'
import { Node } from './types/usfm'

const getPassageQuery = bible.prepare<
	{ chapterId: string },
	{ bookTitle: string; data: string }
>(
	sql`
    SELECT book.title as bookTitle, chapter.data
    FROM chapter
    JOIN book ON book.id = chapter.book_id
    WHERE chapter.id = @chapterId
  `,
)

export type Passage = {
	bookTitle: string
	id: string
	nodes: Node[]
}

export async function getPassage(passageId: PassageId): Promise<Passage> {
	const chapterId = buildChapterId(passageId)
	const row = getPassageQuery.get({ chapterId })
	if (!row) {
		notFound()
	}

	const nodes: Node[] = passageId.verses
		? JSON.parse(row.data)
		: [['cl', row.bookTitle, passageId.chapter], ...JSON.parse(row.data)]

	return {
		bookTitle: row.bookTitle,
		id: buildPassageId(passageId),
		nodes,
	}
}
