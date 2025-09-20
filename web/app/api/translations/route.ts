import { NextResponse } from 'next/server'
import { requireToken } from '@/app/lib/auth'
import { bible, sql } from '@/app/lib/db'

export const dynamic = 'force-dynamic'

export async function GET() {
	const translations = bible
		.prepare(sql`SELECT id, title, last_updated FROM translation`)
		.all()

	return Response.json(translations)
}

export async function POST(request: Request) {
  requireToken(request)

// 	const blob = await request.blob()
//
// 	bible.exec(sql`DELETE FROM translation`)
// 	bible.exec(sql`DELETE FROM book`)
// 	bible.exec(sql`DELETE FROM chapter`)
// 	bible.exec(sql`DELETE FROM node_range`)

  return NextResponse.json({ message: "ok" })
}
