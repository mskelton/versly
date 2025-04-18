import { Passage } from '@/lib/passage'

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
		<div className="rounded-full bg-gray-200 dark:bg-gray-800 px-4 text-xs py-1 w-full text-center font-semibold relative before:absolute before:left-0 before:top-0 before:bottom-0 before:bg-blue-500 before:w-0 before:rounded-full before:transition-[width]">
			<div className="relative">
				{passage.bookTitle} {passage.ref.chapter}
			</div>
		</div>
	)
}
// .tracked-element {
//   view-timeline-name: --progress;
//   view-timeline-axis: block;
// }
//
// .animated-element {
//   animation: fadeIn linear;
//   animation-timeline: --progress;
//   animation-range: entry 0% to exit 100%;
// }
//
// @keyframes fadeIn {
//   from { opacity: 0; }
//   to { opacity: 1; }
// }
