import { notFound } from 'next/navigation'
import { bible, sql } from './db'
import { buildChapterRef, parsePassageRef } from './passageRef'

const GET_PASSAGE = sql`
  SELECT book.title as bookTitle, chapter.data
  FROM chapter
  JOIN book ON book.ref = chapter.book_ref
  WHERE chapter.ref = ?
`

export async function getPassage(ref: string, defaultTranslation = 'ESV') {
	const passageRef = parsePassageRef(ref, defaultTranslation)
	if (!passageRef) {
		notFound()
	}

	const chapterRef = buildChapterRef(passageRef)
	const result = await bible.execute({ args: [chapterRef], sql: GET_PASSAGE })
	const row = result.rows[0] as unknown as
		| { bookTitle: string; data: string }
		| undefined

	if (!row) {
		notFound()
	}

	const nodes = passageRef.verses
		? JSON.parse(row.data)
		: [['cl', row.bookTitle, passageRef.chapter], ...JSON.parse(row.data)]

	return {
		nodes,
		ref: passageRef,
	}
}
