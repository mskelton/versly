import { CSSProperties } from 'react'
import { Passage } from '@/app/lib/passage'
import { buildChapterRef } from '@/app/lib/passageRef'

export function Progress({
	passages,
}: {
	passages: Passage[]
}) {
	return (
		<div className="flex gap-2 w-full py-2 px-6 sticky top-0 mt-6 bg-white dark:text-gray-50 dark:bg-gray-950 z-10">
			{passages.map((passage) => (
				<ProgresPill key={passage.ref.ref} passage={passage} />
			))}
		</div>
	)
}

function ProgresPill({
	passage,
}: {
	passage: Passage
}) {
	return (
		<div className="rounded-full bg-gray-200 dark:bg-gray-800 px-4 text-xs py-1 w-full text-center font-semibold relative">
			<span
				className="absolute left-0 top-0 bottom-0 bg-blue-500 w-0 rounded-full animate-progress [animation:progress_1ms_linear] [animation-fill-mode:both] [animation-range:entry_25%_cover_50%]"
				style={
					{
						// animationTimeline: 'scroll()',
						animationTimeline: `--reader-${buildChapterRef(passage.ref, '-')}`,
					} as CSSProperties
				}
			/>
			<div className="relative">
				{passage.bookTitle} {passage.ref.chapter}
			</div>
		</div>
	)
}
