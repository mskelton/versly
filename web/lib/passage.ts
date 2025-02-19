import { notFound } from 'next/navigation'
import { db, sql } from './db'
import { buildChapterRef, parsePassageRef } from './passageRef'

const getPassageQuery = db.prepare<
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

export async function getPassage(ref: string, defaultTranslation = 'ESV') {
	const passageRef = parsePassageRef(ref, defaultTranslation)
	if (!passageRef) {
		notFound()
	}

	const chapterRef = buildChapterRef(passageRef)
	const row = getPassageQuery.get({ chapterRef })
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
