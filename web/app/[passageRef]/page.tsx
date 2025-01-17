import { Reader } from '@/components/Reader'

export default async function Page({
	params,
}: {
	params: Promise<{ passageRef: string }>
}) {
	const { passageRef } = await params

	return (
		<main className='px-6 py-12 mx-auto'>
			<div className='mt-4 text-lg max-w-lg mx-auto'>
				<Reader passageRef={passageRef} />
			</div>
		</main>
	)
}
