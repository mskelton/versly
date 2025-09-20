import Database from 'better-sqlite3'
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

  const formData = await request.formData()
  const file = formData.get('file') as File

  const blob = await file.arrayBuffer()
  const db = new Database(Buffer.from(blob), { readonly: true })

  // bible.exec(sql`DELETE FROM translation`)
  // bible.exec(sql`DELETE FROM book`)
  // bible.exec(sql`DELETE FROM chapter`)
  // bible.exec(sql`DELETE FROM node_range`)

  return NextResponse.json({ message: 'ok' })
}
