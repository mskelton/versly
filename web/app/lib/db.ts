import Database from 'better-sqlite3'
import * as fs from 'node:fs'
import { hotSwap } from './hotSwap'

export const bible = hotSwap(() => {
  const buffer = fs.readFileSync(process.env.BIBLE_DATABASE_PATH!)

  return new Database(buffer, { readonly: true })
})
