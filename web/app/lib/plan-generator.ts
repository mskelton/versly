import { addDays } from 'date-fns'
import metadataData from './metadata.json'
import type {
  ChapterMetadata,
  CreatePlanRequest,
  Day,
  Reading,
} from './plan-types'

const metadata = metadataData as ChapterMetadata[]

export function loadMetadata(): ChapterMetadata[] {
  return metadata
}

/**
 * Calculate total reading days, which is the total duration, minus the number
 * of rest days that will occur during the plan lifetime.
 */
export function calculateTotalReadingDays(options: CreatePlanRequest): number {
  const startDate = new Date(options.startDate)
  const startWeekDay = startDate.getDay()
  const restDays = options.restDays || []
  let totalRestDays = 0

  for (let i = 0; i < options.duration; i++) {
    const currentWeekDay = (startWeekDay + i) % 7

    if (restDays.includes(currentWeekDay)) {
      totalRestDays++
    }
  }

  return options.duration - totalRestDays
}

/**
 * Create a map of book names to their chapter metadata for easier lookup
 */
function createBookMap(
  metadata: ChapterMetadata[],
): Map<string, ChapterMetadata[]> {
  const bookMap = new Map<string, ChapterMetadata[]>()
  for (const chapter of metadata) {
    const existing = bookMap.get(chapter.book) || []
    existing.push(chapter)
    bookMap.set(chapter.book, existing)
  }
  return bookMap
}

/**
 * Prepare grouped metadata based on the groups specified in options
 */
function prepareGroupMetadata(
  bookMap: Map<string, ChapterMetadata[]>,
  groups: string[][],
): ChapterMetadata[][] {
  const groupMetadata: ChapterMetadata[][] = []
  for (const group of groups) {
    const groupChapters: ChapterMetadata[] = []
    for (const book of group) {
      const chunks = bookMap.get(book)
      if (chunks) {
        groupChapters.push(...chunks)
      }
    }
    groupMetadata.push(groupChapters)
  }
  return groupMetadata
}

/**
 * Calculate target words per day
 */
function calculateWordsPerDay(
  metadata: ChapterMetadata[][],
  totalReadingDays: number,
): { totalWords: number; wordsPerDay: number } {
  let totalWordCount = 0

  for (const group of metadata) {
    for (const chunk of group) {
      totalWordCount += chunk.wordCount
    }
  }

  return {
    totalWords: totalWordCount,
    wordsPerDay: totalWordCount / totalReadingDays,
  }
}

export function parseId(s: string): { book: string; chapter: number } {
  const parts = s.split('.')
  if (parts.length !== 2) {
    throw new Error('invalid id')
  }

  const chapter = parseInt(parts[1], 10)
  if (isNaN(chapter)) {
    throw new Error('invalid chapter number')
  }

  return {
    book: parts[0],
    chapter,
  }
}

/**
 * Generate a reading plan based on the provided options.
 * Returns days with readings, including rest days with empty readings arrays.
 */
export function generate(
  metadata: ChapterMetadata[],
  options: CreatePlanRequest,
): Day[] {
  const allDays: Day[] = []

  // Prepare metadata structures
  const bookMap = createBookMap(metadata)
  const groupMetadata = prepareGroupMetadata(bookMap, options.groups)
  const totalReadingDays = calculateTotalReadingDays(options)
  const { totalWords, wordsPerDay } = calculateWordsPerDay(
    groupMetadata,
    totalReadingDays,
  )

  // Store the total words read so far
  let currentWordsRead = 0

  // Store the reading progress for each group
  const groupProgress: number[] = new Array(groupMetadata.length).fill(0)

  // Loop through all days in the plan duration
  let date = new Date(options.startDate)
  const startWeekDay = date.getDay()

  for (let i = 0; i < options.duration; i++) {
    const dateString = date.toISOString().split('T')[0]

    // Check if it's a rest day
    const dayOfWeek = (startWeekDay + i) % 7
    const isRestDay = options.restDays?.includes(dayOfWeek)

    const expectedProgress = wordsPerDay * (i + 1)

    // If it's a rest day, add an empty day
    if (isRestDay) {
      allDays.push({
        date: dateString,
        id: crypto.randomUUID(),
        readings: [],
        wordCount: 0,
      })

      continue
    }

    // Reading day - calculate readings for this day
    const readings: Reading[] = []
    let dayWordCount = 0

    // Add readings until we reach the the progress we should be at for this day
    // Allow for 10% overage/underage
    // do {} while (currentWordsRead < expectedProgress)

    // Ensure all remaining chunks are added to the last reading day
    if (i === options.duration - 1) {
      for (let j = 0; j < groupMetadata.length; j++) {
        const chunks = groupMetadata[j]
        while (groupProgress[j] < chunks.length) {
          const chunk = chunks[groupProgress[j]]

          dayWordCount += chunk.wordCount
          currentWordsRead += chunk.wordCount
          groupProgress[j]++

          readings.push({
            book: chunk.book,
            chapter: chunk.chapter,
            id: crypto.randomUUID(),
            range: null,
          })
        }
      }
    }

    // Create and add the day
    allDays.push({
      date: dateString,
      id: crypto.randomUUID(),
      readings,
      wordCount: dayWordCount,
    })

    // Increment the date
    date = addDays(date, 1)
  }

  return allDays
}
