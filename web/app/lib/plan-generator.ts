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

function parseId(s: string): { book: string; chapter: number } {
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

  // Create a map of book to its chapters for easier lookup
  const bookMap = new Map<string, ChapterMetadata[]>()
  for (const chapter of metadata) {
    const existing = bookMap.get(chapter.book) || []
    existing.push(chapter)
    bookMap.set(chapter.book, existing)
  }

  // Prepare grouped metadata
  const groupMetadata: ChapterMetadata[][] = []
  for (const group of options.groups) {
    const groupChapters: ChapterMetadata[] = []
    for (const book of group) {
      const chunks = bookMap.get(book)
      if (chunks) {
        groupChapters.push(...chunks)
      }
    }
    groupMetadata.push(groupChapters)
  }

  const totalReadingDays = calculateTotalReadingDays(options)

  // Calculate word count per group
  const groupWordCounts: number[] = []
  for (const group of groupMetadata) {
    let wordCount = 0
    for (const chunk of group) {
      wordCount += chunk.wordCount
    }
    groupWordCounts.push(wordCount)
  }

  // Count the total words per day for each group
  const groupWordsPerDay: number[] = []
  for (const wordCount of groupWordCounts) {
    groupWordsPerDay.push(Math.floor(wordCount / totalReadingDays))
  }

  // Store the total words read from each group as we build the plan
  const groupWordsRead: number[] = new Array(groupMetadata.length).fill(0)

  // Store the reading progress for each group
  const groupProgress: number[] = new Array(groupMetadata.length).fill(0)

  // Generate reading days
  const readingDays: Day[] = []

  for (let day = 0; day < totalReadingDays; day++) {
    const readings: Reading[] = []

    for (let groupIndex = 0; groupIndex < groupMetadata.length; groupIndex++) {
      const chunks = groupMetadata[groupIndex]

      // Each day, determine the total number of words that should have been
      // read by this point in the plan. From that, we try to get as close as
      // possible to the target number of words for the day.
      const accruedWords = groupWordsPerDay[groupIndex] * (day + 1)

      while (groupProgress[groupIndex] < chunks.length) {
        const progress = groupProgress[groupIndex]
        const chunk = chunks[progress]

        const readWords = groupWordsRead[groupIndex]
        const remainingWords = accruedWords - readWords
        if (remainingWords < chunk.wordCount) {
          break
        }

        // Add the chunk to the readings
        // Since chunk.range from metadata is always the full chapter range,
        // we set it to null to indicate full chapter
        readings.push({
          book: chunk.book,
          chapter: chunk.chapter,
          id: crypto.randomUUID(),
          range: null, // Full chapter, so range is null
        })

        // Update the total words read from this group
        groupWordsRead[groupIndex] += chunk.wordCount

        // Update the progress
        groupProgress[groupIndex]++
      }
    }

    // Create a day with the readings
    readingDays.push({
      // Will be assigned later
      date: '',
      id: crypto.randomUUID(),
      readings,
    })
  }

  // Now merge reading days with rest days to create the full plan
  const startDate = new Date(options.startDate)
  const restDays = options.restDays || []
  const startWeekDay = startDate.getDay()

  let readingDayCounter = 0

  for (let i = 0; i < options.duration; i++) {
    const currentDate = new Date(startDate)
    currentDate.setDate(currentDate.getDate() + i)
    const currentWeekDay = (startWeekDay + i) % 7

    const isRestDay = restDays.includes(currentWeekDay)

    if (isRestDay) {
      // Rest day - empty readings
      allDays.push({
        date: currentDate.toISOString().split('T')[0],
        id: crypto.randomUUID(),
        readings: [],
      })
    } else {
      // Reading day
      const readingDay = readingDays[readingDayCounter]
      if (readingDay) {
        allDays.push({
          date: currentDate.toISOString().split('T')[0],
          id: crypto.randomUUID(),
          readings: readingDay.readings,
        })
        readingDayCounter++
      }
    }
  }

  return allDays
}

export { parseId }
