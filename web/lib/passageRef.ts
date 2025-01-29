export function parsePassageRef(ref: string): PassageRef | null {
	const match = ref.match(
		/^([A-Z\d]{3})\.(\d+)(?:\.(\d+)(?:-(\d+))?)?(?:\.([A-z]+))?$/,
	)

	if (!match) {
		return null
	}

	const [_, book, chapter, rangeStart, rangeEnd, translation] = match

	return {
		book,
		chapter,
		translation: translation ?? null,
		verses: rangeStart ? [rangeStart, rangeEnd ?? rangeStart] : null,
	}
}

export type PassageRef = {
	book: string
	chapter: string
	translation: string | null
	verses: [start: string, end: string] | null
}

export function buildChapterRef(
	ref: PassageRef,
	defaultTranslation: string,
): string {
	return `${ref.book}.${ref.chapter}.${ref.translation ?? defaultTranslation}`
}
