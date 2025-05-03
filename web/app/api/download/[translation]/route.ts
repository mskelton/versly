import { bible, sql } from '@/app/lib/db'

export const dynamic = 'force-dynamic'

export async function GET(
	_request: Request,
	{ params }: { params: Promise<{ translation: string }> },
) {
	const { translation } = await params

	const encoder = new TextEncoder()
	const stream = new ReadableStream({
		start(controller) {
			const writeRows = (type: string, stmt: string, params: string[] = []) => {
				const iter = bible.prepare<any, any>(stmt).iterate(params)

				for (const row of iter) {
					const data = Object.values(row)

					controller.enqueue(
						encoder.encode(`${JSON.stringify([type, ...data])}\n`),
					)
				}
			}

			writeRows('translation', sql`SELECT ref, title FROM translation`)

			writeRows(
				'book',
				sql`SELECT ref, title FROM book WHERE translation_ref = ?`,
				[translation],
			)

			writeRows(
				'chapter',
				sql`SELECT ref, data FROM chapter WHERE ref LIKE ?`,
				[`%.${translation}`],
			)

			writeRows(
				'range',
				sql`SELECT start, end, word_count FROM range WHERE chapter_ref = ?`,
				[`%.${translation}`],
			)

			controller.close()
		},
	})

	return new Response(stream, {
		headers: {
			'Content-Type': 'application/x-ndjson',
		},
	})
}
