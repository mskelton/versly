import Database from 'libsql'
import { yolo } from './yolo'

export const sql = String.raw

export const bible = yolo(() => {
	return new Database(process.env.BIBLE_DATABASE_URL!, {
		// @ts-expect-error missing types
		authToken: process.env.BIBLE_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.BIBLE_DATABASE_SYNC_URL,
	})
})

export const versly = yolo(() => {
	return new Database(process.env.VERSLY_DATABASE_URL!, {
		// @ts-expect-error missing types
		authToken: process.env.VERSLY_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.VERSLY_DATABASE_SYNC_URL,
	})
})
