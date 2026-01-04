import { format, isBefore, parseISO } from 'date-fns'
import { Metadata } from 'next'
import Link from 'next/link'
import { Calendar } from 'react-feather'
import { isToday } from '@/app/lib/date'
import { buildChapterId, parsePassageId } from '@/app/lib/passageId'
import plan from '@/app/lib/plan.json'
import { getBookTitle } from '../../lib/bookInfo'

export const metadata: Metadata = {
  title: 'Reading Plan Calendar - Versly',
}

export default function CalendarPage() {
  return (
    <main className="px-6 py-12 mx-auto">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">Reading Plan</h1>
          <Link
            className="flex items-center gap-2 text-sm text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 transition-colors"
            href="/plan"
          >
            <Calendar className="h-4 w-4" />
            Back to today
          </Link>
        </div>

        <div className="space-y-4">
          {plan.plan.days.map((day) => {
            const isTodayReading = isToday(parseISO(day.date))
            const isPast = isBefore(parseISO(day.date), new Date())

            return (
              <div
                key={day.id}
                className={`p-4 rounded-lg border ${
                  isTodayReading
                    ? 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800'
                    : isPast
                      ? 'bg-gray-50 dark:bg-gray-900/50 border-gray-200 dark:border-gray-800 opacity-60'
                      : 'bg-white dark:bg-gray-950 border-gray-200 dark:border-gray-800'
                }`}
              >
                <div className="flex items-center justify-between mb-2">
                  <h3 className="font-semibold text-gray-900 dark:text-gray-100">
                    {format(parseISO(day.date), 'MMMM d, yyyy')}
                  </h3>
                  <span className="text-sm text-gray-500 dark:text-gray-400">
                    {day.wordCount} words
                  </span>
                </div>
                <div className="flex flex-wrap gap-2">
                  {day.readings.map((reading, readingIndex) => {
                    const passageId = parsePassageId(`${reading.book}.${reading.chapter}.ESV`)
                    if (!passageId) return null
                    const href = `/${buildChapterId(passageId)}`
                    return (
                      <Link
                        key={readingIndex}
                        className={`px-3 py-1 rounded text-sm ${
                          isTodayReading
                            ? 'bg-blue-100 dark:bg-blue-900/40 text-blue-900 dark:text-blue-100 hover:bg-blue-200 dark:hover:bg-blue-900/60'
                            : isPast
                              ? 'bg-gray-200 dark:bg-gray-800 text-gray-700 dark:text-gray-300'
                              : 'bg-gray-100 dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-700'
                        } transition-colors`}
                        href={href}
                      >
                        {getBookTitle(reading.book)} {reading.chapter}
                      </Link>
                    )
                  })}
                </div>
              </div>
            )
          })}
        </div>
      </div>
    </main>
  )
}
