import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { Reader } from '@/components/Reader'
import { getPassageName } from '@/lib/bookInfo'
import { getPassage } from '@/lib/passage'
import { parsePassageRef } from '@/lib/passageRef'

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

	return (
		<main className="px-6 py-12 mx-auto">
			<div className="mt-4 text-lg max-w-lg mx-auto">
				<Reader nodes={nodes} passageRef={ref} />
			</div>
		</main>
	)
}
