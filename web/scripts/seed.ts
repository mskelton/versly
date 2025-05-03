import env from '@next/env'

env.loadEnvConfig(process.cwd())

const { bible, sql } = await import('@/app/lib/db')

bible.exec('PRAGMA journal_mode = WAL;')
bible.exec(sql`
  CREATE TABLE IF NOT EXISTS translation (
    id TEXT PRIMARY KEY,
    version INTEGER NOT NULL,
    title TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS book (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    translation_id TEXT NOT NULL,
    FOREIGN KEY (translation_id) REFERENCES translation (id)
  );

  CREATE TABLE IF NOT EXISTS chapter (
    id TEXT PRIMARY KEY,
    data JSON NOT NULL,
    book_id TEXT NOT NULL,
    FOREIGN KEY (book_id) REFERENCES book (id)
  );

  CREATE TABLE IF NOT EXISTS range (
    start_index INTEGER NOT NULL,
    end_index INTEGER NOT NULL,
    word_count INTEGER NOT NULL,
    chapter_id TEXT NOT NULL,
    PRIMARY KEY (start, end),
    FOREIGN KEY (chapter_id) REFERENCES chapter (id)
  );
`)
