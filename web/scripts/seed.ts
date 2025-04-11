import { bible, sql } from '@/lib/db'

bible.executeMultiple(sql`
  CREATE TABLE translation (
    ref TEXT PRIMARY KEY,
    title TEXT NOT NULL
  );

  CREATE TABLE book (
    ref TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    translation_ref TEXT NOT NULL,
    FOREIGN KEY (translation_ref) REFERENCES translation (ref)
  );

  CREATE TABLE chapter (
    ref TEXT PRIMARY KEY,
    data JSON NOT NULL,
    book_ref TEXT NOT NULL,
    FOREIGN KEY (book_ref) REFERENCES book (ref)
  );

  CREATE TABLE range (
    start INTEGER NOT NULL,
    end INTEGER NOT NULL,
    word_count INTEGER NOT NULL,
    chapter_ref TEXT NOT NULL,
    PRIMARY KEY (start, end),
    FOREIGN KEY (chapter_ref) REFERENCES chapter (ref)
  );
`)
