import { parseISO } from 'date-fns'
import { Metadata } from 'next'
import Link from 'next/link'
import { notFound } from 'next/navigation'
import { Calendar } from 'react-feather'
import { Reader } from '@/app/components/Reader'
import { isToday } from '@/app/lib/date'
import { getPassage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'
import plan from '@/app/lib/plan.json'
import { getBookTitle } from '../lib/bookInfo'
import { Reading } from '../lib/plan-types'

export const metadata: Metadata = {
  title: 'Reading Plan - Versly',
}

export default async function PlanPage() {
  const day = plan.plan.days.find((day) => isToday(parseISO(day.date)))
  if (!day) {
    return notFound()
  }

  const passages = await Promise.all(
    day.readings.map((reading) =>
      getPassage(parsePassageId(`${reading.book}.${reading.chapter}.ESV`)!),
    ),
  )

  return (
    <main>
      <div className="px-6 pt-6">
        <div className="max-w-xl mx-auto">
          <div className="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-100 dark:border-blue-900 rounded-lg">
            <div className="flex items-start justify-between mb-4">
              <h1 className="text-lg font-semibold text-blue-900 dark:text-blue-100">
                {parseISO(day.date).toLocaleDateString('en-US', {
                  month: 'long',
                  day: 'numeric',
                  year: 'numeric',
                })}
              </h1>

              <Link
                className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
                href="/plan/calendar"
              >
                <Calendar className="size-4" />
                Show calendar
              </Link>
            </div>

            <div className="space-y-2">
              {joinReadings(day.readings).map((reading, index) => {
                return <p key={index}>{reading}</p>
              })}
            </div>
          </div>
        </div>
      </div>

      <div className="px-6 py-12 mx-auto">
        <div className="mt-4 text-lg max-w-xl mx-auto space-y-20">
          {passages.map((passage) => (
            <Reader key={passage.id} passage={passage} />
          ))}
        </div>
      </div>
    </main>
  )
}

function joinReadings(readings: Reading[]): string[] {
  return readings
    .reduce(
      (acc, reading) => {
        const book = acc.find((book) => book.book === reading.book)
        if (book) {
          book.chapters.push(reading.chapter)
        } else {
          acc.push({ book: reading.book, chapters: [reading.chapter] })
        }
        return acc
      },
      [] as { book: string; chapters: number[] }[],
    )
    .map((reading) => {
      if (reading.chapters.length === 1) {
        return `${getBookTitle(reading.book)} ${reading.chapters[0]}`
      }

      return `${getBookTitle(reading.book)} ${reading.chapters[0]}-${reading.chapters.at(-1)}`
    })
}
