import { createClient } from '@libsql/client'
import { yolo } from './yolo'

export const bible = yolo(() => {
	return createClient({
		authToken: process.env.BIBLE_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.BIBLE_DATABASE_DATABASE_URL,
		url: process.env.BIBLE_DATABASE_URL!,
	})
})

export const versly = yolo(() => {
	return createClient({
		authToken: process.env.VERSLY_DATABASE_AUTH_TOKEN,
		syncUrl: process.env.VERSLY_DATABASE_DATABASE_URL,
		url: process.env.VERSLY_DATABASE_URL!,
	})
})
