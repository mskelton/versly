export function parsePassageId(id: string): PassageId | null {
  const match = id.match(/^([A-z\d]{3})\.(\d+)(?:\.(\d+)(?:-(\d+))?)?(?:\.([A-z]+))?$/)

  if (!match) {
    return null
  }

  const [_, book, chapter, rangeStart, rangeEnd, translation] = match

  return {
    book: book.toUpperCase(),
    chapter,
    translation: translation?.toUpperCase() ?? 'ESV',
    range: rangeStart ? [rangeStart, rangeEnd ?? rangeStart] : null,
  }
}

export type PassageId = {
  book: string
  chapter: string
  range: [start: string, end: string] | null
  translation: string
}

export function buildPassageId(id: PassageId, separator = '.'): string {
  if (id.range) {
    const [start, end] = id.range
    const versePart = start === end ? start : `${start}-${end}`

    return id.book + separator + id.chapter + separator + versePart + separator + id.translation
  }

  return id.book + separator + id.chapter + separator + id.translation
}
