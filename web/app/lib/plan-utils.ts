import { getBookTitle } from './bookInfo'
import { Reading } from './plan-types'

/**
 * Combines consecutive chapters from the same book into ranges.
 * Returns an array of formatted strings like "Genesis 1" or "Genesis 1-3".
 */
export function joinReadings(readings: Reading[]): string[] {
  const result: string[] = []

  for (let i = 0; i < readings.length; i++) {
    const reading = readings[i]
    const chapters: number[] = [reading.chapter]

    // Collect consecutive chapters from the same book
    while (
      i + 1 < readings.length &&
      readings[i + 1].book === reading.book &&
      readings[i + 1].chapter === reading.chapter + chapters.length
    ) {
      chapters.push(readings[i + 1].chapter)
      i++
    }

    const firstChapter = chapters[0]
    const text =
      chapters.length === 1
        ? `${getBookTitle(reading.book)} ${firstChapter}`
        : `${getBookTitle(reading.book)} ${firstChapter}-${chapters.at(-1)}`

    result.push(text)
  }

  return result
}
