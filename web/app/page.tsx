import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { Reader } from '@/app/components/Reader'
import { isToday, parseISO } from '@/app/lib/date'
import { getPassage } from '@/app/lib/passage'
import plan from '@/app/lib/plan.json'
import { Progress } from './Progress'

export const metadata: Metadata = {
	title: 'Versly',
}

export default async function Page() {
	const day = plan.plan.days.find((day) => isToday(parseISO(day.date)))
	if (!day) {
		return notFound()
	}

	const passages = await Promise.all(
		day.readings.map((reading) =>
			getPassage(`${reading.book}.${reading.chapter}`, 'ESV'),
		),
	)

	return (
		<main>
			<Progress passages={passages} />

			<div className="px-6 py-12 mx-auto">
				<div className="mt-4 text-lg max-w-lg mx-auto space-y-20">
					{passages.map(({ nodes, ref }) => (
						<Reader key={ref.ref} nodes={nodes} passageRef={ref} />
					))}
				</div>
			</div>
		</main>
	)
}
