import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { PlanDay } from '@/app/components/PlanDay'
import plan from '@/app/lib/plan.json'

export const metadata: Metadata = {
  title: 'Reading Plan - Versly',
}

interface DayPageProps {
  params: Promise<{ date: string }>
}

export default async function DayPage({ params }: DayPageProps) {
  const { date } = await params
  const day = plan.plan.days.find((day) => day.date === date)

  if (!day) {
    return notFound()
  }

  return <PlanDay day={day} />
}
