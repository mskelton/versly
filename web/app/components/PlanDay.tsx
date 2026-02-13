import { parseISO } from 'date-fns'
import Link from 'next/link'
import { Calendar } from 'react-feather'
import { Reader } from '@/app/components/Reader'
import { getPassage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'
import { Day } from '@/app/lib/plan-types'
import { joinReadings } from '@/app/lib/plan-utils'

interface PlanDayProps {
  day: Day
}

function readingToPassageId(reading: Day['readings'][0], translation: string): string {
  // null or start===0 => full chapter; else subset (1-indexed)
  const r = reading.range
  if (!r || r.start === 0) {
    return `${reading.book}.${reading.chapter}.${translation}`
  }
  return `${reading.book}.${reading.chapter}.${r.start}-${r.end}.${translation}`
}

const PLAN_TRANSLATION = 'ESV'

export async function PlanDay({ day }: PlanDayProps) {
  const passages = await Promise.all(
    day.readings.map((reading) =>
      getPassage(parsePassageId(readingToPassageId(reading, PLAN_TRANSLATION))!),
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
