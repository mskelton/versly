import { format, isBefore, parseISO } from 'date-fns'
import { Metadata } from 'next'
import Link from 'next/link'
import { Calendar } from 'react-feather'
import { isToday } from '@/app/lib/date'
import { joinReadings } from '@/app/lib/plan-utils'
import plan from '@/app/lib/plan.json'
import { ScrollToToday } from './ScrollToToday'

export const metadata: Metadata = {
  title: 'Calendar - Versly',
}

export default function CalendarPage() {
  return (
    <main className="px-6 py-12 mx-auto">
      <ScrollToToday />
      <div className="max-w-xl mx-auto">
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
              <Link
                key={day.id}
                className={`block p-4 rounded-lg border cursor-pointer transition-colors ${
                  isTodayReading
                    ? 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-800 hover:bg-blue-100 dark:hover:bg-blue-900/30'
                    : isPast
                      ? 'bg-neutral-50 dark:bg-neutral-900/50 border-gray-200 dark:border-gray-800 opacity-60 hover:opacity-80'
                      : 'bg-white dark:bg-neutral-950 border-gray-200 dark:border-gray-800 hover:bg-neutral-50 dark:hover:bg-neutral-900'
                }`}
                href={`/plan/day/${day.date}`}
                id={isTodayReading ? 'today' : undefined}
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
                  {joinReadings(day.readings).map((reading, readingIndex) => {
                    return (
                      <span
                        key={readingIndex}
                        className={`px-3 py-1 rounded text-sm ${
                          isTodayReading
                            ? 'bg-blue-100 dark:bg-blue-900/40 text-blue-900 dark:text-blue-100'
                            : isPast
                              ? 'bg-neutral-200 dark:bg-neutral-800 text-gray-700 dark:text-gray-300'
                              : 'bg-neutral-100 dark:bg-neutral-800 text-gray-700 dark:text-gray-300'
                        }`}
                      >
                        {reading}
                      </span>
                    )
                  })}
                </div>
              </Link>
            )
          })}
        </div>
      </div>
    </main>
  )
}
