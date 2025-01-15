import sqlite from 'better-sqlite3'
import path from 'path'

export const sql = String.raw

export const db = sqlite(path.join(process.cwd(), 'lib/data/db.sqlite'), {
	fileMustExist: true,
	readonly: true,
})

// https://github.com/WiseLibs/better-sqlite3/blob/master/docs/performance.md
// db.pragma('journal_mode = WAL')
