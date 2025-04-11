import { createClient } from '@libsql/client'
import { yolo } from './yolo'

export const sql = String.raw

export const bible = yolo(() => {
	return createClient({
		authToken: process.env.BIBLE_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.BIBLE_DATABASE_SYNC_URL,
		url: process.env.BIBLE_DATABASE_URL!,
	})
})

export const versly = yolo(() => {
	return createClient({
		authToken: process.env.VERSLY_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.VERSLY_DATABASE_SYNC_URL,
		url: process.env.VERSLY_DATABASE_URL!,
	})
})
