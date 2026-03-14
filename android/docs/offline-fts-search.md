# Offline FTS5 Search

Replace the server-side search API with SQLite FTS5 full-text search, indexing
verse-level plaintext locally when translations are downloaded.

## Current state

Search calls `VerslyService.search()` on the server, gets back
book/chapter/range references, then hydrates them locally from SQLite via
`bulkGetPassages()`. The Bible text data is already local — only the search
index lives on the server.

## Schema

Add an FTS5 virtual table to `schema.sql` and as a migration (version 6) in
`VerslyDatabase.onUpgrade`:

```sql
CREATE VIRTUAL TABLE verse_fts USING fts5(
    text,
    book_id UNINDEXED,
    chapter_id UNINDEXED,
    verse UNINDEXED,
    translation_id UNINDEXED
);
```

Each row is one verse's plaintext plus its location metadata. `UNINDEXED`
columns are stored but not tokenized — they identify which passage matched
without bloating the index.

## Populating the index

During `VerslyDatabase.downloadTranslation()`, after inserting each `"c"`
(chapter) row, parse the chapter JSON to extract per-verse plaintext and insert
into `verse_fts`.

The chapter JSON contains `"zv"` (verse marker) and `"zt"` (text) nodes. Add a
helper method:

```kotlin
fun extractAllVerseTexts(chapterData: JSONArray): Map<Int, String>
```

This walks the chapter data, tracks the current verse number from `"zv"` nodes,
accumulates text from `"zt"` nodes, and returns a map of `{verseNum ->
plaintext}`. Each entry gets inserted into `verse_fts` with the chapter's
`book_id`, `chapter_id`, and `translation_id`.

Also add FTS row cleanup in `deleteTranslation()`:

```sql
DELETE FROM verse_fts WHERE translation_id = ?
```

## Querying

Add a method to `VerslyDatabase`:

```kotlin
fun searchVerses(
    query: String,
    translationId: String,
    limit: Int = 50,
): List<HydratedSearchResult>
```

Query:

```sql
SELECT
    book_id,
    chapter_id,
    verse,
    book.title,
    snippet(verse_fts, 0, '', '', '...', 30) AS text
FROM verse_fts
JOIN book ON book.id = verse_fts.book_id
    AND book.translation_id = verse_fts.translation_id
WHERE verse_fts MATCH ?
    AND verse_fts.translation_id = ?
ORDER BY rank
LIMIT ?
```

FTS5's `rank` column uses BM25 scoring automatically. The `snippet()` function
provides context around the match.

### FTS5 query capabilities

FTS5 supports prefix search (`lov*`), phrase search (`"son of man"`), and
boolean operators (AND, OR, NOT) out of the box. User input should be sanitized
before passing to `MATCH` to avoid syntax errors on special characters.

## ViewModel changes

Replace the API call chain in `SearchViewModel.searchResults` with a call to
`db.searchVerses()`. This simplifies the flow significantly — no more
`verslyService.search()`, no more `bulkGetPassages()` hydration, no more
combining with the translation preference after the fact.

The new flow:

1. Debounce query (300ms)
2. Call `db.searchVerses(query, translation)` on `Dispatchers.IO`
3. Emit results

Remove the `VerslyService` dependency from `SearchViewModel`'s constructor.

## Cleanup

- Remove `search()` from the `VerslyService` interface
- Remove `SearchResult` and `SearchResponse` data classes from `VerslyService.kt`

## Files to change

- `app/src/main/assets/schema.sql` — add FTS5 table
- `app/src/main/java/dev/mskelton/versly/persistence/VerslyDatabase.kt` — migration, indexing, query method, delete cleanup
- `app/src/main/java/dev/mskelton/versly/persistence/SearchViewModel.kt` — replace API with local query
- `app/src/main/java/dev/mskelton/versly/api/VerslyService.kt` — remove search endpoint and related types
