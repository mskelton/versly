import { bible } from '@/lib/db'

if (process.env.BIBLE_DATABASE_SYNC_URL) {
	await bible.sync()
}
