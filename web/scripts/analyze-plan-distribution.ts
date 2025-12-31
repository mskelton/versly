#!/usr/bin/env tsx
/**
 * Analyzes word count distribution across days in a generated plan
 * Calculates statistics to measure how evenly words are distributed
 */

import { readFileSync, writeFileSync } from 'node:fs'
import { join } from 'node:path'

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

async function analyzePlan() {
  const requestBody = JSON.parse(readFileSync(PLAN_REQUEST_FILE, 'utf-8'))

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
  writeFileSync('after.json', JSON.stringify(data, null, 2))
  const days = data.plan.days.filter((day) => day.readings.length > 0) // Only reading days

  const wordCounts = days.map((day) => day.wordCount)
  const totalWords = wordCounts.reduce((a, b) => a + b, 0)
  const targetWordsPerDay = totalWords / wordCounts.length

  // Calculate statistics
  const mean = wordCounts.reduce((a, b) => a + b, 0) / wordCounts.length
  const variance =
    wordCounts.reduce((sum, count) => sum + Math.pow(count - mean, 2), 0) /
    wordCounts.length
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
  const within10Percent = wordCounts.filter(
    (count) => Math.abs(count - mean) / mean <= 0.1,
  ).length
  const within10PercentPct = (within10Percent / wordCounts.length) * 100

  // Days within 20% of mean
  const within20Percent = wordCounts.filter(
    (count) => Math.abs(count - mean) / mean <= 0.2,
  ).length
  const within20PercentPct = (within20Percent / wordCounts.length) * 100

  console.log('='.repeat(60))
  console.log('PLAN WORD COUNT DISTRIBUTION ANALYSIS')
  console.log('='.repeat(60))
  console.log()
  console.log(`Total reading days: ${wordCounts.length}`)
  console.log(`Total words: ${totalWords.toLocaleString()}`)
  console.log(`Target words per day: ${targetWordsPerDay.toFixed(2)}`)
  console.log()
  console.log('Basic Statistics:')
  console.log(`  Mean:              ${mean.toFixed(2)} words/day`)
  console.log(`  Standard Deviation: ${stdDev.toFixed(2)} words`)
  console.log(`  Min:                ${min.toLocaleString()} words`)
  console.log(`  Max:                ${max.toLocaleString()} words`)
  console.log(
    `  Range:              ${range.toLocaleString()} words (${((range / mean) * 100).toFixed(1)}% of mean)`,
  )
  console.log(
    `  Coefficient of Variation: ${coefficientOfVariation.toFixed(2)}%`,
  )
  console.log()
  console.log('Percentiles:')
  console.log(`  P25 (Q1):  ${p25.toLocaleString()} words`)
  console.log(`  P50 (Median): ${p50.toLocaleString()} words`)
  console.log(`  P75 (Q3):  ${p75.toLocaleString()} words`)
  console.log(`  P90:       ${p90.toLocaleString()} words`)
  console.log(`  P95:       ${p95.toLocaleString()} words`)
  console.log()
  console.log('Distribution Quality:')
  console.log(
    `  Days within 10% of mean: ${within10Percent} (${within10PercentPct.toFixed(1)}%)`,
  )
  console.log(
    `  Days within 20% of mean: ${within20Percent} (${within20PercentPct.toFixed(1)}%)`,
  )
  console.log()
  console.log('Interpretation:')
  console.log(`  Lower CV% = more even distribution (good)`)
  console.log(`  Higher % within 10-20% = more consistent (good)`)
  console.log(`  Smaller range = less variation (good)`)
  console.log('='.repeat(60))
}

analyzePlan().catch((error) => {
  console.error('Error:', error)
  process.exit(1)
})
