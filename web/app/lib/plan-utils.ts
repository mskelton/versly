import { getBookTitle } from './bookInfo'
import { Range, Reading } from './plan-types'

/**
 * Combines consecutive chapters from the same book into ranges.
 * Returns an array of formatted strings like:
 * - "Genesis 1" (single full chapter)
 * - "Genesis 1:5-12" (single chapter with verse range)
 * - "Genesis 1-3" (multiple full chapters)
 * - "Psalm 119:13-120:12" (multi-chapter with verse ranges)
 */
export function joinReadings(readings: Reading[]): string[] {
  const result: string[] = []

  for (let i = 0; i < readings.length; i++) {
    const firstReading = readings[i]
    let lastReading = firstReading

    // Collect consecutive chapters from the same book
    while (
      i + 1 < readings.length &&
      readings[i + 1].book === firstReading.book &&
      readings[i + 1].chapter === lastReading.chapter + 1
    ) {
      i++
      lastReading = readings[i]
    }

    const text = formatRange(
      getBookTitle(firstReading.book),
      firstReading.chapter,
      firstReading.range,
      lastReading.chapter,
      lastReading.range,
    )

    result.push(text)
  }

  return result
}

function formatRange(
  book: string,
  startChapter: number,
  startRange: Range | null,
  endChapter: number,
  endRange: Range | null,
): string {
  const isSingleChapter = startChapter === endChapter

  if (isSingleChapter) {
    // Single chapter cases
    if (!startRange) {
      // Full chapter: "Genesis 1"
      return `${book} ${startChapter}`
    }
    // Verse range within chapter: "Genesis 1:5-12"
    return `${book} ${startChapter}:${startRange.start}-${startRange.end}`
  }

  // Multi-chapter cases
  const startPart = startRange ? `${startChapter}:${startRange.start}` : `${startChapter}`

  const endPart = endRange ? `${endChapter}:${endRange.end}` : `${endChapter}`

  return `${book} ${startPart}-${endPart}`
}
