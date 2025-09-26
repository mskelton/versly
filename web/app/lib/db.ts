import Database from 'better-sqlite3'
import { hotSwap } from './hotSwap'

export const bible = hotSwap(() => {
  return new Database(process.env.BIBLE_DATABASE_PATH!, { readonly: true })
})
