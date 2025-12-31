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

  // Track previous day's word count for smoothing (initialize to wordsPerDay)
  let previousDayWordCount = wordsPerDay

  // Track which reading day we're on (not counting rest days)
  let readingDayIndex = 0

  // Get the starting date of the plan
  let date = new Date(options.startDate)
  const startWeekDay = date.getDay()

  // Loop through all days in the plan duration, including rest days
  for (let i = 0; i < options.duration; i++) {
    const dateString = date.toISOString().split('T')[0]

    // Calculate the target progress we should be at by this day
    const targetProgress = wordsPerDay * (i + 1)

    // Check if it's a rest day
    const dayOfWeek = (startWeekDay + i) % 7
    const isRestDay = options.restDays?.includes(dayOfWeek)

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

    // Collect readings per group, then flatten in group order
    const readingsByGroup: Reading[][] = Array.from(
      { length: groupMetadata.length },
      () => [],
    )

    // Track the word count for this day
    let wordCount = 0

    // For all days but the last, add readings based on progress
    if (i < options.duration - 1) {
      // Calculate the remaining reading days
      const remainingDays = totalReadingDays - readingDayIndex

      // Gradual correction daily target
      const smoothingFactor = 0.2
      const progressGap = targetProgress - currentProgress
      const dailyTarget =
        previousDayWordCount + (progressGap / remainingDays) * smoothingFactor

      // Hard bounds: 85% to 115% of wordsPerDay
      const minDailyWords = wordsPerDay * 0.85
      const maxDailyWords = wordsPerDay * 1.15

      // Add readings until we reach the progress we should be at for this day
      do {
        // Select the next group to read from
        const selectedGroup = selectNextGroup({
          day: readingDayIndex + 1,
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

        // Chunk size filtering: check if chunk would violate hard bounds
        const chunk = chunks[chunkIndex]
        const maybeWordCount = wordCount + chunk.wordCount
        const wouldExceedMax = maybeWordCount > maxDailyWords

        // Only skip if would exceed max AND we're already at or above minimum
        if (wouldExceedMax && wordCount >= minDailyWords) {
          // Would exceed max and we're already at minimum - skip this chunk
          break
        }

        // If would be below min, that's okay - we'll add penalty in scoring
        // Don't break here, let the scoring handle it

        // Dry run: calculate distance from both cumulative and daily targets
        const maybeProgress = currentProgress + chunk.wordCount

        // Exponential scoring: use squared distance for stronger penalty on deviations
        const cumulativeDistanceIfIncluded = Math.pow(
          maybeProgress - targetProgress,
          2,
        )
        const cumulativeDistanceIfExcluded = Math.pow(
          currentProgress - targetProgress,
          2,
        )
        const dailyDistanceIfIncluded = Math.pow(
          maybeWordCount - dailyTarget,
          2,
        )
        const dailyDistanceIfExcluded = Math.pow(wordCount - dailyTarget, 2)

        // Add exponential penalty for approaching or exceeding bounds
        let penaltyIfIncluded = 0
        if (maybeWordCount < minDailyWords) {
          penaltyIfIncluded = Math.pow(minDailyWords - maybeWordCount, 2) * 10
        } else if (maybeWordCount > maxDailyWords) {
          penaltyIfIncluded = Math.pow(maybeWordCount - maxDailyWords, 2) * 10
        }

        let penaltyIfExcluded = 0
        if (wordCount < minDailyWords) {
          penaltyIfExcluded = Math.pow(minDailyWords - wordCount, 2) * 10
        } else if (wordCount > maxDailyWords) {
          penaltyIfExcluded = Math.pow(wordCount - maxDailyWords, 2) * 10
        }

        // Combined score: 60% weight on cumulative target, 40% on daily target, plus penalties
        const scoreIfIncluded =
          cumulativeDistanceIfIncluded * 0.6 +
          dailyDistanceIfIncluded * 0.4 +
          penaltyIfIncluded
        const scoreIfExcluded =
          cumulativeDistanceIfExcluded * 0.6 +
          dailyDistanceIfExcluded * 0.4 +
          penaltyIfExcluded

        // Add chunk if it improves the combined score (or is equal)
        if (scoreIfIncluded <= scoreIfExcluded) {
          readingsByGroup[selectedGroup].push({
            id: crypto.randomUUID(),
            book: chunk.book,
            chapter: chunk.chapter,
            range: null,
            wordCount: chunk.wordCount,
          })

          wordCount += chunk.wordCount
          currentProgress += chunk.wordCount
          groupProgress[selectedGroup]++
        } else {
          // Adding chunk would worsen the combined score - stop
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

          wordCount += chunk.wordCount
          currentProgress += chunk.wordCount
          groupProgress[j]++

          readingsByGroup[j].push({
            id: crypto.randomUUID(),
            book: chunk.book,
            chapter: chunk.chapter,
            range: null,
            wordCount: chunk.wordCount,
          })
        }
      }
    }

    // Flatten readings in group order (all group 0, then all group 1, etc.)
    const readings = readingsByGroup.flat()

    // Create and add the day
    allDays.push({
      id: crypto.randomUUID(),
      date: dateString,
      wordCount,
      targetProgress,
      currentProgress,
      readings,
    })

    // Update previous day word count for next iteration (only for reading days)
    if (!isRestDay) {
      previousDayWordCount = wordCount
      readingDayIndex++
    }

    // Increment the date
    date = addDays(date, 1)
  }

  return allDays
}
