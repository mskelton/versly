CREATE TABLE translation (
    id TEXT PRIMARY KEY,
    version INTEGER NOT NULL,
    title TEXT NOT NULL
);

CREATE TABLE book (
    id TEXT,
    title TEXT NOT NULL,
    abbreviation TEXT NOT NULL,
    translation_id TEXT NOT NULL,
    PRIMARY KEY (id, translation_id),
    FOREIGN KEY (translation_id) REFERENCES translation (id)
);

CREATE TABLE chapter (
    id INTEGER,
    data JSON NOT NULL,
    book_id TEXT NOT NULL,
    translation_id TEXT NOT NULL,
    PRIMARY KEY (id, book_id, translation_id),
    FOREIGN KEY (book_id, translation_id) REFERENCES book (id, translation_id)
);

CREATE TABLE range (
    start_index INTEGER NOT NULL,
    end_index INTEGER NOT NULL,
    word_count INTEGER NOT NULL,
    chapter_id INTEGER NOT NULL,
    book_id TEXT NOT NULL,
    translation_id TEXT NOT NULL,
    PRIMARY KEY (start_index, end_index),
    FOREIGN KEY (chapter_id, book_id, translation_id) REFERENCES chapter (id, book_id, translation_id)
);
