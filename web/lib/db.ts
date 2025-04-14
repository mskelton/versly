import sqlite from 'better-sqlite3'
import { yolo } from './yolo'

export const sql = String.raw

export const bible = yolo(() => {
	return sqlite(process.env.BIBLE_DATABASE_PATH, {
		fileMustExist: true,
		readonly: true,
	})
})
