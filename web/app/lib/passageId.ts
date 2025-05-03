export function parsePassageId(id: string): PassageId | null {
	const match = id.match(
		/^([A-z\d]{3})\.(\d+)(?:\.(\d+)(?:-(\d+))?)?(?:\.([A-z]+))?$/,
	)

	if (!match) {
		return null
	}

	const [_, book, chapter, rangeStart, rangeEnd, translation] = match

	return {
		book: book.toUpperCase(),
		chapter,
		translation: translation?.toUpperCase(),
		verses: rangeStart ? [rangeStart, rangeEnd ?? rangeStart] : null,
	}
}

export type PassageId = {
	book: string
	chapter: string
	translation: string
	verses: [start: string, end: string] | null
}

export function buildChapterId(id: PassageId, separator = '.'): string {
	return id.book + separator + id.chapter + separator + id.translation
}

export function buildPassageId(id: PassageId, separator = '.'): string {
	if (id.verses) {
		const [start, end] = id.verses
		const versePart = start === end ? start : `${start}-${end}`

		return (
			id.book +
			separator +
			id.chapter +
			separator +
			versePart +
			separator +
			id.translation
		)
	}

	return buildChapterId(id, separator)
}
