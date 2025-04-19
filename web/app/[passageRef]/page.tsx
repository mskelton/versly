import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ChevronLeft, ChevronRight } from 'react-feather'
import { Reader } from '@/app/components/Reader'
import {
	getNextChapter,
	getPassageName,
	getPreviousChapter,
} from '@/app/lib/bookInfo'
import { getPassage } from '@/app/lib/passage'
import { parsePassageRef } from '@/app/lib/passageRef'
import { ReaderNavLink } from '../components/ReaderNavLink'

type Props = {
	params: Promise<{ passageRef: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
	const { passageRef } = await params
	const parsedRef = parsePassageRef(passageRef, 'ESV')
	if (!parsedRef) {
		notFound()
	}

	return {
		title: getPassageName(parsedRef),
	}
}

export default async function Page({ params }: Props) {
	const { passageRef } = await params
	const { nodes, ref } = await getPassage(passageRef, 'ESV')

	const previousHref = getPreviousChapter(ref)
	const nextHref = getNextChapter(ref)

	return (
		<main className="px-6 py-12 mx-auto">
			<div className="mt-4 text-lg max-w-lg mx-auto">
				<ReaderNavLink
					href={previousHref}
					icon={<ChevronLeft />}
					label="Previous chapte"
					side="left"
				/>

				<Reader nodes={nodes} />

				<ReaderNavLink
					href={nextHref}
					icon={<ChevronRight />}
					label="Next chapter"
					side="right"
				/>
			</div>
		</main>
	)
}
