export function parsePassageRef(
	ref: string,
	defaultTranslation: string,
): PassageRef | null {
	const match = ref.match(
		/^([A-z\d]{3})\.(\d+)(?:\.(\d+)(?:-(\d+))?)?(?:\.([A-z]+))?$/,
	)

	if (!match) {
		return null
	}

	const [_, book, chapter, rangeStart, rangeEnd, translation] = match

	return {
		book: book.toUpperCase(),
		chapter,
		ref,
		translation: translation?.toUpperCase() ?? defaultTranslation,
		verses: rangeStart ? [rangeStart, rangeEnd ?? rangeStart] : null,
	}
}

export type PassageRef = {
	book: string
	chapter: string
	ref: string
	translation: string
	verses: [start: string, end: string] | null
}

export function buildChapterRef(ref: Omit<PassageRef, 'ref'>): string {
	return `${ref.book}.${ref.chapter}.${ref.translation}`
}
