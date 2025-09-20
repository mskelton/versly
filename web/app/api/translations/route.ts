import { bible, sql } from '@/app/lib/db'

export const dynamic = 'force-dynamic'

export async function GET() {
	const translations = bible
		.prepare(sql`SELECT id, title, last_updated FROM translation`)
		.all()

	return Response.json(translations)
}
