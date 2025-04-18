import { notFound } from 'next/navigation'
import { bible, sql } from './db'
import { buildChapterRef, parsePassageRef, PassageRef } from './passageRef'
import { Node } from './types/usfm'

const getPassageQuery = bible.prepare<
	{ chapterRef: string },
	{ bookTitle: string; data: string }
>(
	sql`
    SELECT book.title as bookTitle, chapter.data
    FROM chapter
    JOIN book ON book.ref = chapter.book_ref
    WHERE chapter.ref = @chapterRef
  `,
)

export type Passage = {
	bookTitle: string
	nodes: Node[]
	ref: PassageRef
}

export async function getPassage(
	ref: string,
	defaultTranslation = 'ESV',
): Promise<Passage> {
	const passageRef = parsePassageRef(ref, defaultTranslation)
	if (!passageRef) {
		notFound()
	}

	const chapterRef = buildChapterRef(passageRef)
	const row = getPassageQuery.get({ chapterRef })
	if (!row) {
		notFound()
	}

	const nodes: Node[] = passageRef.verses
		? JSON.parse(row.data)
		: [['cl', row.bookTitle, passageRef.chapter], ...JSON.parse(row.data)]

	return {
		bookTitle: row.bookTitle,
		nodes,
		ref: passageRef,
	}
}
