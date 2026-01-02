#!/usr/bin/env tsx
/**
 * Analyzes word count distribution across days in a generated plan
 * Calculates statistics to measure how evenly words are distributed
 */

import fs from 'node:fs'
import { join } from 'node:path'
import { CreatePlanRequest } from '@/app/lib/plan-types'

const PLAN_REQUEST_FILE = join(process.cwd(), 'fixtures', 'plan.json')

interface PlanResponse {
  plan: {
    days: Array<{
      date: string
      readings: unknown[]
      wordCount: number
    }>
  }
}

async function analyzePlan(requestBody: CreatePlanRequest, output: fs.WriteStream) {
  const response = await fetch('http://localhost:3000/api/plans', {
    body: JSON.stringify(requestBody),
    headers: { 'Content-Type': 'application/json' },
    method: 'POST',
  })

  if (!response.ok) {
    console.error(`API request failed: ${response.status}`)
    process.exit(1)
  }

  const data = (await response.json()) as PlanResponse
  const days = data.plan.days.filter((day) => day.readings.length > 0) // Only reading days

  const wordCounts = days.map((day) => day.wordCount)
  const totalWords = wordCounts.reduce((a, b) => a + b, 0)
  const targetWordsPerDay = totalWords / wordCounts.length

  // Calculate statistics
  const mean = wordCounts.reduce((a, b) => a + b, 0) / wordCounts.length
  const variance =
    wordCounts.reduce((sum, count) => sum + Math.pow(count - mean, 2), 0) / wordCounts.length
  const stdDev = Math.sqrt(variance)
  const min = Math.min(...wordCounts)
  const max = Math.max(...wordCounts)
  const range = max - min
  const coefficientOfVariation = (stdDev / mean) * 100 // As percentage

  // Percentiles
  const sorted = [...wordCounts].sort((a, b) => a - b)
  const p25 = sorted[Math.floor(sorted.length * 0.25)]
  const p50 = sorted[Math.floor(sorted.length * 0.5)]
  const p75 = sorted[Math.floor(sorted.length * 0.75)]
  const p90 = sorted[Math.floor(sorted.length * 0.9)]
  const p95 = sorted[Math.floor(sorted.length * 0.95)]

  // Days within 10% of mean
  const within10Percent = wordCounts.filter((count) => Math.abs(count - mean) / mean <= 0.1).length
  const within10PercentPct = (within10Percent / wordCounts.length) * 100

  // Days within 20% of mean
  const within20Percent = wordCounts.filter((count) => Math.abs(count - mean) / mean <= 0.2).length
  const within20PercentPct = (within20Percent / wordCounts.length) * 100

  output.write(`${'='.repeat(60)}\n`)
  output.write('PLAN WORD COUNT DISTRIBUTION ANALYSIS\n')
  output.write(`${'='.repeat(60)}\n`)
  output.write('\n')
  output.write(`Total reading days: ${wordCounts.length}\n`)
  output.write(`Total words: ${totalWords.toLocaleString()}\n`)
  output.write(`Target words per day: ${targetWordsPerDay.toFixed(2)}\n`)
  output.write(`Allow partial chapters: ${requestBody.allowPartialChapters}\n`)
  output.write('\n')
  output.write('Basic Statistics:\n')
  output.write(`  Mean:              ${mean.toFixed(2)} words/day\n`)
  output.write(`  Standard Deviation: ${stdDev.toFixed(2)} words\n`)
  output.write(`  Min:                ${min.toLocaleString()} words\n`)
  output.write(`  Max:                ${max.toLocaleString()} words\n`)
  output.write(
    `  Range:              ${range.toLocaleString()} words (${((range / mean) * 100).toFixed(1)}% of mean)\n`,
  )
  output.write(`  Coefficient of Variation: ${coefficientOfVariation.toFixed(2)}%\n`)
  output.write('\n')
  output.write('Percentiles:\n')
  output.write(`  P25 (Q1):  ${p25.toLocaleString()} words\n`)
  output.write(`  P50 (Median): ${p50.toLocaleString()} words\n`)
  output.write(`  P75 (Q3):  ${p75.toLocaleString()} words\n`)
  output.write(`  P90:       ${p90.toLocaleString()} words\n`)
  output.write(`  P95:       ${p95.toLocaleString()} words\n`)
  output.write('\n')
  output.write('Distribution Quality:\n')
  output.write(
    `  Days within 10% of mean: ${within10Percent} (${within10PercentPct.toFixed(1)}%)\n`,
  )
  output.write(
    `  Days within 20% of mean: ${within20Percent} (${within20PercentPct.toFixed(1)}%)\n`,
  )
  output.write('\n')
  output.write('Interpretation:\n')
  output.write('  Lower CV% = more even distribution (good)\n')
  output.write('  Higher % within 10-20% = more consistent (good)\n')
  output.write('  Smaller range = less variation (good)\n')
  output.write('\n')

  return data
}

const requestBody = JSON.parse(await fs.promises.readFile(PLAN_REQUEST_FILE, 'utf-8'))
const output = fs.createWriteStream('analysis/distribution.txt')

for (const val of [true, false]) {
  const plan = await analyzePlan({ ...requestBody, allowPartialChapters: val }, output)

  await fs.promises.writeFile(
    `analysis/plan-${val ? 'partial' : 'full'}.json`,
    JSON.stringify(plan, null, 2),
  )
}
