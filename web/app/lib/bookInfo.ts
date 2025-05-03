import { buildChapterId, PassageId } from './passageId'

export const bookInfo: [bookId: string, bookName: string, chapters: number][] =
	[
		['GEN', 'Genesis', 50],
		['EXO', 'Exodus', 40],
		['LEV', 'Leviticus', 27],
		['NUM', 'Numbers', 36],
		['DEU', 'Deuteronomy', 34],
		['JOS', 'Joshua', 24],
		['JDG', 'Judges', 21],
		['RUT', 'Ruth', 4],
		['1SA', '1 Samuel', 31],
		['2SA', '2 Samuel', 24],
		['1KI', '1 Kings', 22],
		['2KI', '2 Kings', 25],
		['1CH', '1 Chronicles', 29],
		['2CH', '2 Chronicles', 36],
		['EZR', 'Ezra', 10],
		['NEH', 'Nehemiah', 13],
		['EST', 'Esther', 10],
		['JOB', 'Job', 42],
		['PSA', 'Psalms', 150],
		['PRO', 'Proverbs', 31],
		['ECC', 'Ecclesiastes', 12],
		['SNG', 'Song of Songs', 8],
		['ISA', 'Isaiah', 66],
		['JER', 'Jeremiah', 52],
		['LAM', 'Lamentations', 5],
		['EZK', 'Ezekiel', 48],
		['DAN', 'Daniel', 12],
		['HOS', 'Hosea', 14],
		['JOL', 'Joel', 3],
		['AMO', 'Amos', 9],
		['OBA', 'Obadiah', 1],
		['JON', 'Jonah', 4],
		['MIC', 'Micah', 7],
		['NAM', 'Nahum', 3],
		['HAB', 'Habakkuk', 3],
		['ZEP', 'Zephaniah', 3],
		['HAG', 'Haggai', 2],
		['ZEC', 'Zechariah', 14],
		['MAL', 'Malachi', 4],
		['MAT', 'Matthew', 28],
		['MRK', 'Mark', 16],
		['LUK', 'Luke', 24],
		['JHN', 'John', 21],
		['ACT', 'Acts', 28],
		['ROM', 'Romans', 16],
		['1CO', '1 Corinthians', 16],
		['2CO', '2 Corinthians', 13],
		['GAL', 'Galatians', 6],
		['EPH', 'Ephesians', 6],
		['PHP', 'Philippians', 4],
		['COL', 'Colossians', 4],
		['1TH', '1 Thessalonians', 5],
		['2TH', '2 Thessalonians', 3],
		['1TI', '1 Timothy', 6],
		['2TI', '2 Timothy', 4],
		['TIT', 'Titus', 3],
		['PHM', 'Philemon', 1],
		['HEB', 'Hebrews', 13],
		['JAS', 'James', 5],
		['1PE', '1 Peter', 5],
		['2PE', '2 Peter', 3],
		['1JN', '1 John', 5],
		['2JN', '2 John', 1],
		['3JN', '3 John', 1],
		['JUD', 'Jude', 1],
		['REV', 'Revelation', 22],
	]

export function getPreviousChapter(passageId: PassageId) {
	return getChapter(passageId, -1)
}

export function getNextChapter(passageId: PassageId) {
	return getChapter(passageId, 1)
}

function getChapter(passageId: PassageId, direction: -1 | 1) {
	const bookIndex = bookInfo.findIndex(([id]) => id === passageId.book)
	const totalChapters = bookInfo[bookIndex][2]

	let bookId = passageId.book
	let chapterId = parseInt(passageId.chapter) + direction

	if (direction === -1 && chapterId < 1) {
		bookId = bookInfo[bookIndex - 1][0]
		chapterId = bookInfo[bookIndex - 1][2]
	} else if (direction === 1 && chapterId > totalChapters) {
		bookId = bookInfo[bookIndex + 1][0]
		chapterId = 1
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
