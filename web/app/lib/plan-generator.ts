import { addDays } from 'date-fns'
import { v4 } from 'uuid'
import metadataData from './metadata.json'
import type { ChapterMetadata, CreatePlanRequest, Day, Range, Reading } from './plan-types'

export function loadMetadata(): ChapterMetadata[] {
  return metadataData
}

/**
 * Generate a reading plan based on the provided options.
 * Returns days with readings, including rest days with empty readings arrays.
 */
export function generate(metadata: ChapterMetadata[], options: CreatePlanRequest): Day[] {
  const allDays: Day[] = []

  // Prepare metadata structures
  const bookMap = createBookMap(metadata)
  const groupMetadata = prepareGroupMetadata(bookMap, options.groups)
  const totalReadingDays = calculateTotalReadingDays(options)
  const { wordsPerDay } = calculateWordsPerDay(groupMetadata, totalReadingDays)

  // Store the total words read so far
  let currentProgress = 0

  // Store the reading progress for each group (chapter index)
  const groupProgress: number[] = new Array(groupMetadata.length).fill(0)

  // Store the range progress for each group and chapter (range index within chapter)
  // groupRangeProgress[groupIndex][chapterIndex] = current range index
  const groupRangeProgress: number[][] = groupMetadata.map(() => [])

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

    // Check if it's a rest day
    const dayOfWeek = (startWeekDay + i) % 7
    const isRestDay = options.restDays?.includes(dayOfWeek)

    // Calculate the target progress we should be at by this day
    // For rest days, use the same target as the previous reading day
    // For reading days, use readingDayIndex + 1 (the day we're about to process)
    const targetProgress = wordsPerDay * (readingDayIndex + 1)

    // If it's a rest day, add an empty day
    if (isRestDay) {
      allDays.push({
        id: uuid(),
        date: dateString,
        wordCount: 0,
        targetProgress,
        currentProgress,
        readings: [],
      })

      continue
    }

    // Collect readings per group, then flatten in group order
    const readingsByGroup: Reading[][] = Array.from({ length: groupMetadata.length }, () => [])

    // Track the word count for this day
    let wordCount = 0

    // For all days but the last, add readings based on progress
    if (i < options.duration - 1) {
      // Calculate the remaining reading days
      const remainingDays = totalReadingDays - readingDayIndex

      // Gradual correction daily target
      const smoothingFactor = 0.2
      const progressGap = targetProgress - currentProgress
      const dailyTarget = previousDayWordCount + (progressGap / remainingDays) * smoothingFactor

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
          groupRangeProgress,
          totalReadingDays,
          allowPartialChapters: options.allowPartialChapters,
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

        // Initialize range progress for this chapter if needed
        if (!groupRangeProgress[selectedGroup][chunkIndex]) {
          groupRangeProgress[selectedGroup][chunkIndex] = 0
        }
        const currentRangeIndex = groupRangeProgress[selectedGroup][chunkIndex]

        // Check if we can use the full chapter (when allowPartialChapters is false, or when it fits)
        const canUseFull =
          !options.allowPartialChapters ||
          (currentRangeIndex === 0 &&
            canUseFullChapter(chunk, wordCount, dailyTarget, minDailyWords, maxDailyWords))

        if (canUseFull) {
          // Use full chapter as before
          const maybeWordCount = wordCount + chunk.wordCount
          const wouldExceedMax = maybeWordCount > maxDailyWords

          // Only skip if would exceed max AND we're already at or above minimum
          if (wouldExceedMax && wordCount >= minDailyWords) {
            // Would exceed max and we're already at minimum - skip this chunk
            break
          }

          // Dry run: calculate distance from both cumulative and daily targets
          const maybeProgress = currentProgress + chunk.wordCount

          // Exponential scoring: use squared distance for stronger penalty on deviations
          const cumulativeDistanceIfIncluded = Math.pow(maybeProgress - targetProgress, 2)
          const cumulativeDistanceIfExcluded = Math.pow(currentProgress - targetProgress, 2)
          const dailyDistanceIfIncluded = Math.pow(maybeWordCount - dailyTarget, 2)
          const dailyDistanceIfExcluded = Math.pow(wordCount - dailyTarget, 2)

          // Add exponential penalty for approaching or exceeding bounds
          const penaltyIfIncluded = calculateBoundsPenalty(
            maybeWordCount,
            minDailyWords,
            maxDailyWords,
          )
          const penaltyIfExcluded = calculateBoundsPenalty(wordCount, minDailyWords, maxDailyWords)

          // Combined score: 60% weight on cumulative target, 40% on daily target, plus penalties
          const scoreIfIncluded =
            cumulativeDistanceIfIncluded * 0.6 + dailyDistanceIfIncluded * 0.4 + penaltyIfIncluded
          const scoreIfExcluded =
            cumulativeDistanceIfExcluded * 0.6 + dailyDistanceIfExcluded * 0.4 + penaltyIfExcluded

          // Add chunk if it improves the combined score (or is equal)
          if (scoreIfIncluded <= scoreIfExcluded) {
            readingsByGroup[selectedGroup].push({
              id: uuid(),
              book: chunk.book,
              chapter: chunk.chapter,
              range: null,
              wordCount: chunk.wordCount,
            })

            wordCount += chunk.wordCount
            currentProgress += chunk.wordCount
            groupProgress[selectedGroup]++
            // Clear range progress for this chapter since it's fully consumed
            groupRangeProgress[selectedGroup][chunkIndex] = 0
          } else {
            // Adding chunk would worsen the combined score - stop
            break
          }
        } else {
          // Use ranges from the chapter
          const availableRanges = getAvailableRanges(chunk, currentRangeIndex)
          if (availableRanges.length === 0) {
            // No more ranges in this chapter, move to next chapter and try again
            groupProgress[selectedGroup]++
            groupRangeProgress[selectedGroup][chunkIndex] = 0
            continue
          }

          // Collect ranges to use (consecutive ranges that fit)
          const rangesToUse: Range[] = []
          let rangesWordCount = 0

          for (const range of availableRanges) {
            const maybeWordCount = wordCount + rangesWordCount + range.wordCount
            const wouldExceedMax = maybeWordCount > maxDailyWords

            // If adding this range would exceed max and we're already at minimum, stop
            if (wouldExceedMax && wordCount + rangesWordCount >= minDailyWords) {
              break
            }

            // Try adding this range and see if it improves the score
            const maybeProgress = currentProgress + rangesWordCount + range.wordCount
            const maybeTotalWordCount = wordCount + rangesWordCount + range.wordCount

            // Calculate scores
            const cumulativeDistanceIfIncluded = Math.pow(maybeProgress - targetProgress, 2)
            const cumulativeDistanceIfExcluded = Math.pow(
              currentProgress + rangesWordCount - targetProgress,
              2,
            )
            const dailyDistanceIfIncluded = Math.pow(maybeTotalWordCount - dailyTarget, 2)
            const dailyDistanceIfExcluded = Math.pow(wordCount + rangesWordCount - dailyTarget, 2)

            // Penalties
            const penaltyIfIncluded = calculateBoundsPenalty(
              maybeTotalWordCount,
              minDailyWords,
              maxDailyWords,
            )
            const currentTotalWordCount = wordCount + rangesWordCount
            const penaltyIfExcluded = calculateBoundsPenalty(
              currentTotalWordCount,
              minDailyWords,
              maxDailyWords,
            )

            const scoreIfIncluded =
              cumulativeDistanceIfIncluded * 0.6 + dailyDistanceIfIncluded * 0.4 + penaltyIfIncluded
            const scoreIfExcluded =
              cumulativeDistanceIfExcluded * 0.6 + dailyDistanceIfExcluded * 0.4 + penaltyIfExcluded

            if (scoreIfIncluded <= scoreIfExcluded) {
              rangesToUse.push(range)
              rangesWordCount += range.wordCount
            } else {
              // Adding this range would worsen the score - stop collecting ranges
              break
            }
          }

          // If we collected any ranges, create a reading
          if (rangesToUse.length > 0) {
            const mergedRange = mergeRanges(rangesToUse)
            readingsByGroup[selectedGroup].push({
              id: uuid(),
              book: chunk.book,
              chapter: chunk.chapter,
              range: mergedRange,
              wordCount: mergedRange.wordCount,
            })

            wordCount += mergedRange.wordCount
            currentProgress += mergedRange.wordCount
            groupRangeProgress[selectedGroup][chunkIndex] += rangesToUse.length

            // If all ranges are consumed, move to next chapter
            if (groupRangeProgress[selectedGroup][chunkIndex] >= chunk.ranges.length) {
              groupProgress[selectedGroup]++
              groupRangeProgress[selectedGroup][chunkIndex] = 0
            }
          } else {
            // No ranges fit for this day - leave the chapter for the next day
            // Don't move to next chapter, just break and try again tomorrow
            break
          }
        }
      } while (currentProgress < targetProgress)
    }
    // Ensure all remaining chunks are added to the last reading day
    else {
      for (let j = 0; j < groupMetadata.length; j++) {
        const chunks = groupMetadata[j]
        while (groupProgress[j] < chunks.length) {
          const chunk = chunks[groupProgress[j]]

          // Initialize range progress for this chapter if needed
          if (!groupRangeProgress[j][groupProgress[j]]) {
            groupRangeProgress[j][groupProgress[j]] = 0
          }
          const currentRangeIndex = groupRangeProgress[j][groupProgress[j]]

          const currentChapterIndex = groupProgress[j]
          if (options.allowPartialChapters && currentRangeIndex < chunk.ranges.length) {
            // There are remaining ranges in this chapter
            const remainingRanges = getAvailableRanges(chunk, currentRangeIndex)
            if (remainingRanges.length > 0) {
              // Merge all remaining ranges into one reading
              const mergedRange = mergeRanges(remainingRanges)
              readingsByGroup[j].push({
                id: uuid(),
                book: chunk.book,
                chapter: chunk.chapter,
                range: mergedRange,
                wordCount: mergedRange.wordCount,
              })

              wordCount += mergedRange.wordCount
              currentProgress += mergedRange.wordCount
            }
            // Move to next chapter
            groupRangeProgress[j][currentChapterIndex] = 0
            groupProgress[j]++
          } else {
            // Use full chapter (either allowPartialChapters is false, or chapter is fully consumed)
            readingsByGroup[j].push({
              id: uuid(),
              book: chunk.book,
              chapter: chunk.chapter,
              range: null,
              wordCount: chunk.wordCount,
            })

            wordCount += chunk.wordCount
            currentProgress += chunk.wordCount
            groupRangeProgress[j][currentChapterIndex] = 0
            groupProgress[j]++
          }
        }
      }
    }

    // Flatten readings in group order (all group 0, then all group 1, etc.)
    const readings = readingsByGroup.flat()

    // Create and add the day
    allDays.push({
      id: uuid(),
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
function createBookMap(metadata: ChapterMetadata[]): Map<string, ChapterMetadata[]> {
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
 * Get available ranges from a chapter starting at the given range index
 */
function getAvailableRanges(chapter: ChapterMetadata, startRangeIndex: number): Range[] {
  if (startRangeIndex >= chapter.ranges.length) {
    return []
  }
  return chapter.ranges.slice(startRangeIndex)
}

/**
 * Merge consecutive ranges into a single Range
 */
function mergeRanges(ranges: Range[]): Range {
  if (ranges.length === 0) {
    throw new Error('Cannot merge empty ranges array')
  }
  if (ranges.length === 1) {
    return ranges[0]
  }

  const totalWordCount = ranges.reduce((sum, range) => sum + range.wordCount, 0)
  return {
    start: ranges[0].start,
    end: ranges[ranges.length - 1].end,
    wordCount: totalWordCount,
  }
}

/**
 * Determine if a full chapter can be used within the daily bounds
 */
function canUseFullChapter(
  chapter: ChapterMetadata,
  currentWordCount: number,
  dailyTarget: number,
  minDailyWords: number,
  maxDailyWords: number,
): boolean {
  const maybeWordCount = currentWordCount + chapter.wordCount
  return maybeWordCount <= maxDailyWords
}

/**
 * Calculate penalty for a word count that's outside the bounds
 */
function calculateBoundsPenalty(
  wordCount: number,
  minDailyWords: number,
  maxDailyWords: number,
): number {
  if (wordCount < minDailyWords) {
    return Math.pow(minDailyWords - wordCount, 2) * 10
  } else if (wordCount > maxDailyWords) {
    return Math.pow(wordCount - maxDailyWords, 2) * 10
  }
  return 0
}

/**
 * Select the next group to read from, prioritizing groups that are behind their expected progress
 * Returns the group index, or -1 if no groups have remaining chunks
 */
function selectNextGroup({
  allowPartialChapters,
  day,
  groupMetadata,
  groupProgress,
  groupRangeProgress,
  totalReadingDays,
}: {
  allowPartialChapters?: boolean
  day: number
  groupMetadata: ChapterMetadata[][]
  groupProgress: number[]
  groupRangeProgress: number[][]
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
    // Add word counts from fully consumed chapters
    for (let j = 0; j < groupProgress[groupIndex]; j++) {
      groupActualProgress += groupMetadata[groupIndex][j].wordCount
    }

    // If allowPartialChapters is true, add progress from partially consumed current chapter
    if (allowPartialChapters && groupProgress[groupIndex] < groupMetadata[groupIndex].length) {
      const currentChapter = groupMetadata[groupIndex][groupProgress[groupIndex]]
      const rangeIndex = groupRangeProgress[groupIndex][groupProgress[groupIndex]] || 0
      if (rangeIndex > 0 && rangeIndex < currentChapter.ranges.length) {
        // Add word counts from consumed ranges in the current chapter
        for (let r = 0; r < rangeIndex; r++) {
          groupActualProgress += currentChapter.ranges[r].wordCount
        }
      }
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

let uuidCounter = 0

function uuid(): string {
  if (process.env.NODE_ENV === 'production') {
    return crypto.randomUUID()
  }

  const random = Uint8Array.of(
    0x10,
    0x91,
    0x56,
    0xbe,
    0xc4,
    0xfb,
    0xc1,
    0xea,
    0x71,
    0xb4,
    0xef,
    0xe1,
    0x67,
    0x1c,
    0x58,
    0x36,
  )

  uuidCounter++
  random[15] = uuidCounter & 0xff
  random[14] = (uuidCounter >> 8) & 0xff

  return v4({ random })
}
