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
  logger.debug('Updating Bible database...')

  const formData = await request.formData()
  const file = formData.get('file') as File
  const blob = await file.arrayBuffer()
  const buffer = Buffer.from(blob)

  // Close the database connection so we can write the file to disk
  bible.close()

  // Swap the database file
  logger.debug('Saving Bible database to disk...')
  await fs.writeFile(process.env.BIBLE_DATABASE_PATH!, buffer)

  // Re-open the database connection
  logger.debug('Re-open Bible database...')
  bible.swap(
    new Database(process.env.BIBLE_DATABASE_PATH!, { readonly: false }),
  )

  logger.debug('Bible database updated successfully')
  return NextResponse.json({ message: 'ok' })
}
