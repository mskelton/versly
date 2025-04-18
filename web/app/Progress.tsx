import { Passage } from '@/lib/passage'

export function Progress({
	passages,
}: {
	passages: Passage[]
}) {
	return (
		<div className="flex gap-2 w-full py-4 px-6 sticky top-0 dark:text-gray-50 dark:bg-gray-950 z-10">
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
		<div className="rounded-full bg-gray-800 px-4 text-xs py-1 w-full text-center font-semibold">
			{passage.bookTitle} {passage.ref.chapter}
		</div>
	)
}
