import env from '@next/env'

env.loadEnvConfig(process.cwd())

const { bible, sql } = await import('@/app/lib/db')

bible.exec('PRAGMA journal_mode = WAL;')
bible.exec(sql`
  CREATE TABLE IF NOT EXISTS translation (
    ref TEXT PRIMARY KEY,
    title TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS book (
    ref TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    translation_ref TEXT NOT NULL,
    FOREIGN KEY (translation_ref) REFERENCES translation (ref)
  );

  CREATE TABLE IF NOT EXISTS chapter (
    ref TEXT PRIMARY KEY,
    data JSON NOT NULL,
    book_ref TEXT NOT NULL,
    FOREIGN KEY (book_ref) REFERENCES book (ref)
  );

  CREATE TABLE IF NOT EXISTS range (
    start INTEGER NOT NULL,
    end INTEGER NOT NULL,
    word_count INTEGER NOT NULL,
    chapter_ref TEXT NOT NULL,
    PRIMARY KEY (start, end),
    FOREIGN KEY (chapter_ref) REFERENCES chapter (ref)
  );
`)
