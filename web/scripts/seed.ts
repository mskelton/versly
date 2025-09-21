import env from '@next/env'
import Database from 'better-sqlite3'
import fs from 'node:fs/promises'

env.loadEnvConfig(process.cwd())

const bible = Database(process.env.BIBLE_DATABASE_PATH)

bible.pragma('journal_mode = WAL')
bible.exec(
  await fs.readFile(
    new URL('../../android/app/src/main/assets/schema.sql', import.meta.url),
    'utf8',
  ),
)
