export interface Range {
  end: number
  start: number
  wordCount: number
}

export interface ChapterMetadata {
  book: string
  chapter: number
  ranges: Range[]
  wordCount: number
}

export interface CreatePlanRequest {
  /** When true, chapters can be broken into sections for more even reading. */
  allowPartialChapters?: boolean
  /** Total number of days to complete the plan. This includes rest days. */
  duration: number
  /** Groups define which books to read together. Each group is a slice of book references. The plan will read from each group every day (in order) evenly distributing readings in each group across the plan. */
  groups: string[][]
  /** Which days of the week to rest and not complete any readings. 0 = Sunday, 6 = Saturday. */
  restDays?: number[]
  /** The first day of the plan. This can be in the past. */
  startDate: string
}

export interface Reading {
  book: string
  chapter: number
  id: string
  range: Range | null
  wordCount: number
}

export interface Day {
  currentProgress: number
  date: string
  id: string
  readings: Reading[]
  targetProgress: number
  wordCount: number
}

export interface Plan {
  days: Day[]
  id: string
}

export interface PlanResponse {
  plan: Plan
}
