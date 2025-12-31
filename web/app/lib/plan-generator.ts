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

/**
 * Select the next group to read from, prioritizing groups that are behind their expected progress
 * Returns the group index, or -1 if no groups have remaining chunks
 */
function selectNextGroup({
  day,
  groupMetadata,
  groupProgress,
  totalReadingDays,
}: {
  day: number
  groupMetadata: ChapterMetadata[][]
  groupProgress: number[]
  totalReadingDays: number
}): number {
  let selectedGroup = -1
  let mostBehindAmount = -Infinity

  // Find the group that's most behind its expected progress
  for (let groupIndex = 0; groupIndex < groupMetadata.length; groupIndex++) {
    if (groupProgress[groupIndex] >= groupMetadata[groupIndex].length) {
      continue // This group is done
    }

    // Calculate expected progress for this group
    const groupTotalWords = groupMetadata[groupIndex].reduce(
      (sum, chunk) => sum + chunk.wordCount,
      0,
    )
    const groupWordsPerDay = groupTotalWords / totalReadingDays
    const groupExpectedProgress = groupWordsPerDay * day

    // Calculate actual progress for this group
    let groupActualProgress = 0
    for (let j = 0; j < groupProgress[groupIndex]; j++) {
      groupActualProgress += groupMetadata[groupIndex][j].wordCount
    }

    const groupBehindAmount = groupExpectedProgress - groupActualProgress

    // Prioritize groups that are behind
    if (groupBehindAmount > mostBehindAmount) {
      mostBehindAmount = groupBehindAmount
      selectedGroup = groupIndex
    }
  }

  // If no group is behind, pick the first available group
  if (selectedGroup === -1) {
    for (let groupIndex = 0; groupIndex < groupMetadata.length; groupIndex++) {
      if (groupProgress[groupIndex] < groupMetadata[groupIndex].length) {
        selectedGroup = groupIndex
        break
      }
    }
  }

  return selectedGroup
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
  const { wordsPerDay } = calculateWordsPerDay(groupMetadata, totalReadingDays)

  // Store the total words read so far
  let currentProgress = 0

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

    const targetProgress = wordsPerDay * (i + 1)

    // If it's a rest day, add an empty day
    if (isRestDay) {
      allDays.push({
        id: crypto.randomUUID(),
        date: dateString,
        wordCount: 0,
        targetProgress,
        currentProgress,
        readings: [],
      })

      continue
    }

    // Reading day - calculate readings for this day
    const readings: Reading[] = []
    let dayWordCount = 0

    // For all days but the last, add readings based on progress
    if (i < options.duration - 1) {
      // Add readings until we reach the progress we should be at for this day
      // Use "dry run" to determine if adding a chunk gets us closer to target
      do {
        // Select the next group to read from
        const selectedGroup = selectNextGroup({
          day: i + 1,
          groupMetadata,
          groupProgress,
          totalReadingDays,
        })

        // If no chunks remain, break
        if (selectedGroup === -1) {
          break
        }

        // Get the next chunk from the selected group
        const chunks = groupMetadata[selectedGroup]
        const chunkIndex = groupProgress[selectedGroup]

        if (chunkIndex >= chunks.length) {
          break
        }

        const chunk = chunks[chunkIndex]

        // Dry run: calculate distance from target if we include or exclude the chunk
        const ifIncluded = currentProgress + chunk.wordCount
        const ifExcluded = currentProgress

        const distanceIfIncluded = Math.abs(ifIncluded - targetProgress)
        const distanceIfExcluded = Math.abs(ifExcluded - targetProgress)

        // Add chunk if it gets us closer to the target (or equally close)
        if (distanceIfIncluded <= distanceIfExcluded) {
          readings.push({
            id: crypto.randomUUID(),
            book: chunk.book,
            chapter: chunk.chapter,
            range: null,
            wordCount: chunk.wordCount,
          })

          dayWordCount += chunk.wordCount
          currentProgress += chunk.wordCount
          groupProgress[selectedGroup]++
        } else {
          // Adding chunk would move us further from target - stop
          break
        }
      } while (currentProgress < targetProgress)
    }
    // Ensure all remaining chunks are added to the last reading day
    else {
      for (let j = 0; j < groupMetadata.length; j++) {
        const chunks = groupMetadata[j]
        while (groupProgress[j] < chunks.length) {
          const chunk = chunks[groupProgress[j]]

          dayWordCount += chunk.wordCount
          currentProgress += chunk.wordCount
          groupProgress[j]++

          readings.push({
            id: crypto.randomUUID(),
            book: chunk.book,
            chapter: chunk.chapter,
            range: null,
            wordCount: chunk.wordCount,
          })
        }
      }
    }

    // Create and add the day
    allDays.push({
      id: crypto.randomUUID(),
      date: dateString,
      wordCount: dayWordCount,
      targetProgress,
      currentProgress,
      readings,
    })

    // Increment the date
    date = addDays(date, 1)
  }

  return allDays
}
