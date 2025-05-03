import { CSSProperties } from 'react'
import { Passage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'

export function Progress({
	passages,
}: {
	passages: Passage[]
}) {
	return (
		<div className="flex gap-2 w-full py-2 px-6 sticky top-0 mt-6 bg-white dark:text-gray-50 dark:bg-gray-950 z-10">
			{passages.map((passage, i) => (
				<ProgresPill key={i} passage={passage} />
			))}
		</div>
	)
}

function ProgresPill({
	passage,
}: {
	passage: Passage
}) {
	const { chapter } = parsePassageId(passage.id)!

	return (
		<div className="rounded-full bg-gray-200 dark:bg-gray-800 px-4 text-xs py-1 w-full text-center font-semibold relative">
			<span
				className="absolute left-0 top-0 bottom-0 bg-blue-500 w-0 rounded-full animate-progress [animation-fill-mode:both] [animation-range:entry_25%_cover_50%]"
				style={
					{
						// animationTimeline: 'scroll()',
						animationTimeline: `--reader-${passage.id.replace(/\./g, '-')}`,
					} as CSSProperties
				}
			/>
			<div className="relative">
				{passage.bookTitle} {chapter}
			</div>
		</div>
	)
}
