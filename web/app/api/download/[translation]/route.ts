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

			writeRows('t', sql`SELECT id, version, title FROM translation`)

			writeRows(
				'b',
				sql`SELECT id, title FROM book WHERE translation_id = ?`,
				[translation],
			)

			writeRows('c', sql`SELECT book_id, id, data FROM chapter WHERE translation_id = ?`, [
				translation
			])

			// writeRows(
			// 	'r',
			// 	sql`SELECT start_index, end_index, word_count FROM range WHERE chapter_id = ?`,
			// 	[translation],
			// )

			controller.close()
		},
	})

	return new Response(stream, {
		headers: {
			'Content-Type': 'application/x-ndjson',
		},
	})
}
