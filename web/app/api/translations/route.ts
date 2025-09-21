import Database from 'better-sqlite3'
import { NextResponse } from 'next/server'
import * as fs from 'node:fs/promises'
import { requireToken } from '@/app/lib/auth'
import { bible } from '@/app/lib/db'
import { logger } from '@/app/lib/logger'

export const dynamic = 'force-dynamic'

export async function GET() {
  const translations = bible
    .prepare(`SELECT id, title, last_updated FROM translation`)
    .all()

  return Response.json(translations)
}

export async function POST(request: Request) {
  requireToken(request)
  logger.info('Updating Bible database...')

  const formData = await request.formData()
  const file = formData.get('file') as File
  const blob = await file.arrayBuffer()
  const buffer = Buffer.from(blob)

  // Save the file to disk when the app restarts
  logger.info('Saving Bible database to disk...')
  await fs.writeFile(process.env.BIBLE_DATABASE_PATH!, buffer)

  // Swap the database in memory
  logger.info('Swapping Bible database in memory...')
  bible.swap(new Database(buffer, { readonly: false }))

  logger.info('Bible database updated successfully')
  return NextResponse.json({ message: 'ok' })
}
