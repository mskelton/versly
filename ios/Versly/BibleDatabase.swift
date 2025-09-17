import Foundation
import SQLite3

class BibleDatabase: ObservableObject {
    private var db: OpaquePointer?
    private let verslyService: VerslyService

    init(verslyService: VerslyService) {
        self.verslyService = verslyService
        openDatabase()
        createTables()
    }

    deinit {
        sqlite3_close(db)
    }

    private func openDatabase() {
        let fileURL = try! FileManager.default
            .url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
            .appendingPathComponent("bible.db")

        if sqlite3_open(fileURL.path, &db) != SQLITE_OK {
            print("Unable to open database at \(fileURL.path)")
        }
    }

    private func createTables() {
        let createTranslationsTable = """
                CREATE TABLE IF NOT EXISTS translations (
                    id TEXT PRIMARY KEY,
                    title TEXT NOT NULL,
                    version INTEGER NOT NULL
                );
            """

        let createBooksTable = """
                CREATE TABLE IF NOT EXISTS books (
                    translation TEXT,
                    id TEXT,
                    title TEXT,
                    abbreviation TEXT,
                    chapter_count INTEGER,
                    PRIMARY KEY (translation, id)
                );
            """

        let createNodesTable = """
                CREATE TABLE IF NOT EXISTS nodes (
                    id TEXT PRIMARY KEY,
                    translation TEXT,
                    book TEXT,
                    chapter TEXT,
                    data TEXT
                );
            """

        executeSQL(createTranslationsTable)
        executeSQL(createBooksTable)
        executeSQL(createNodesTable)
    }

    private func executeSQL(_ sql: String) {
        var statement: OpaquePointer?
        if sqlite3_prepare_v2(db, sql, -1, &statement, nil) == SQLITE_OK {
            sqlite3_step(statement)
        }
        sqlite3_finalize(statement)
    }

    func isInitialized() -> Bool {
        var statement: OpaquePointer?
        let query = "SELECT COUNT(*) FROM books LIMIT 1"

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else {
            return false
        }

        var count = 0
        if sqlite3_step(statement) == SQLITE_ROW {
            count = Int(sqlite3_column_int(statement, 0))
        }

        sqlite3_finalize(statement)
        return count > 0
    }

    func downloadTranslation(_ translationId: String) async throws {
        let data = try await verslyService.downloadTranslation(translationId)

        // Process the SQLite data file
        let tempURL = FileManager.default.temporaryDirectory.appendingPathComponent(
            "temp_translation.db")
        try data.write(to: tempURL)

        // Copy data from temp database to main database
        var tempDB: OpaquePointer?
        guard sqlite3_open(tempURL.path, &tempDB) == SQLITE_OK else {
            throw DatabaseError.failedToOpen
        }

        defer {
            sqlite3_close(tempDB)
            try? FileManager.default.removeItem(at: tempURL)
        }

        // Copy translations
        copyTranslations(from: tempDB, translationId: translationId)

        // Copy books
        copyBooks(from: tempDB)

        // Copy nodes
        copyNodes(from: tempDB)
    }

    private func copyTranslations(from sourceDB: OpaquePointer?, translationId: String) {
        var statement: OpaquePointer?
        let query = "SELECT id, title, version FROM translations WHERE id = ?"

        guard sqlite3_prepare_v2(sourceDB, query, -1, &statement, nil) == SQLITE_OK else { return }
        sqlite3_bind_text(statement, 1, translationId, -1, nil)

        if sqlite3_step(statement) == SQLITE_ROW {
            let id = String(cString: sqlite3_column_text(statement, 0))
            let title = String(cString: sqlite3_column_text(statement, 1))
            let version = sqlite3_column_int(statement, 2)

            var insertStatement: OpaquePointer?
            let insertQuery =
                "INSERT OR REPLACE INTO translations (id, title, version) VALUES (?, ?, ?)"

            if sqlite3_prepare_v2(db, insertQuery, -1, &insertStatement, nil) == SQLITE_OK {
                sqlite3_bind_text(insertStatement, 1, id, -1, nil)
                sqlite3_bind_text(insertStatement, 2, title, -1, nil)
                sqlite3_bind_int(insertStatement, 3, version)
                sqlite3_step(insertStatement)
            }
            sqlite3_finalize(insertStatement)
        }
        sqlite3_finalize(statement)
    }

    private func copyBooks(from sourceDB: OpaquePointer?) {
        var statement: OpaquePointer?
        let query = "SELECT translation, id, title, abbreviation, chapter_count FROM books"

        guard sqlite3_prepare_v2(sourceDB, query, -1, &statement, nil) == SQLITE_OK else { return }

        while sqlite3_step(statement) == SQLITE_ROW {
            let translation = String(cString: sqlite3_column_text(statement, 0))
            let id = String(cString: sqlite3_column_text(statement, 1))
            let title = String(cString: sqlite3_column_text(statement, 2))
            let abbreviation = String(cString: sqlite3_column_text(statement, 3))
            let chapterCount = sqlite3_column_int(statement, 4)

            var insertStatement: OpaquePointer?
            let insertQuery =
                "INSERT OR REPLACE INTO books (translation, id, title, abbreviation, chapter_count) VALUES (?, ?, ?, ?, ?)"

            if sqlite3_prepare_v2(db, insertQuery, -1, &insertStatement, nil) == SQLITE_OK {
                sqlite3_bind_text(insertStatement, 1, translation, -1, nil)
                sqlite3_bind_text(insertStatement, 2, id, -1, nil)
                sqlite3_bind_text(insertStatement, 3, title, -1, nil)
                sqlite3_bind_text(insertStatement, 4, abbreviation, -1, nil)
                sqlite3_bind_int(insertStatement, 5, chapterCount)
                sqlite3_step(insertStatement)
            }
            sqlite3_finalize(insertStatement)
        }
        sqlite3_finalize(statement)
    }

    private func copyNodes(from sourceDB: OpaquePointer?) {
        var statement: OpaquePointer?
        let query = "SELECT id, translation, book, chapter, data FROM nodes"

        guard sqlite3_prepare_v2(sourceDB, query, -1, &statement, nil) == SQLITE_OK else { return }

        while sqlite3_step(statement) == SQLITE_ROW {
            let id = String(cString: sqlite3_column_text(statement, 0))
            let translation = String(cString: sqlite3_column_text(statement, 1))
            let book = String(cString: sqlite3_column_text(statement, 2))
            let chapter = String(cString: sqlite3_column_text(statement, 3))
            let data = String(cString: sqlite3_column_text(statement, 4))

            var insertStatement: OpaquePointer?
            let insertQuery =
                "INSERT OR REPLACE INTO nodes (id, translation, book, chapter, data) VALUES (?, ?, ?, ?, ?)"

            if sqlite3_prepare_v2(db, insertQuery, -1, &insertStatement, nil) == SQLITE_OK {
                sqlite3_bind_text(insertStatement, 1, id, -1, nil)
                sqlite3_bind_text(insertStatement, 2, translation, -1, nil)
                sqlite3_bind_text(insertStatement, 3, book, -1, nil)
                sqlite3_bind_text(insertStatement, 4, chapter, -1, nil)
                sqlite3_bind_text(insertStatement, 5, data, -1, nil)
                sqlite3_step(insertStatement)
            }
            sqlite3_finalize(insertStatement)
        }
        sqlite3_finalize(statement)
    }

    func getPassage(book: String, chapter: String, translation: String) -> Passage? {
        var statement: OpaquePointer?
        let query = """
                SELECT n.id, n.data, b.title, b.abbreviation
                FROM nodes n
                JOIN books b ON n.book = b.id AND n.translation = b.translation
                WHERE n.book = ? AND n.chapter = ? AND n.translation = ?
                ORDER BY CAST(n.id AS INTEGER)
            """

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else { return nil }

        sqlite3_bind_text(statement, 1, book, -1, nil)
        sqlite3_bind_text(statement, 2, chapter, -1, nil)
        sqlite3_bind_text(statement, 3, translation, -1, nil)

        var nodes: [Node] = []
        var bookTitle = ""
        var bookAbbreviation = ""

        while sqlite3_step(statement) == SQLITE_ROW {
            let nodeId = String(cString: sqlite3_column_text(statement, 0))
            let dataString = String(cString: sqlite3_column_text(statement, 1))
            bookTitle = String(cString: sqlite3_column_text(statement, 2))
            bookAbbreviation = String(cString: sqlite3_column_text(statement, 3))

            if let jsonData = dataString.data(using: .utf8),
                let jsonArray = try? JSONSerialization.jsonObject(with: jsonData) as? [Any]
            {
                let node = Node(id: nodeId, data: jsonArray)
                nodes.append(node)
            }
        }

        sqlite3_finalize(statement)

        guard !nodes.isEmpty else { return nil }

        return Passage(
            translation: translation,
            book: book,
            bookTitle: bookTitle,
            bookAbbreviation: bookAbbreviation,
            chapter: chapter,
            nodes: nodes
        )
    }

    func getBookList(_ translation: String) -> [BookMetadata] {
        var statement: OpaquePointer?
        let query =
            "SELECT id, title, abbreviation, chapter_count FROM books WHERE translation = ? ORDER BY rowid"

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else { return [] }
        sqlite3_bind_text(statement, 1, translation, -1, nil)

        var books: [BookMetadata] = []

        while sqlite3_step(statement) == SQLITE_ROW {
            let id = String(cString: sqlite3_column_text(statement, 0))
            let title = String(cString: sqlite3_column_text(statement, 1))
            let abbreviation = String(cString: sqlite3_column_text(statement, 2))
            let chapterCount = Int(sqlite3_column_int(statement, 3))

            books.append(
                BookMetadata(
                    id: id, title: title, abbreviation: abbreviation, chapterCount: chapterCount))
        }

        sqlite3_finalize(statement)
        return books
    }

    func getAvailableTranslations() -> [Translation] {
        // Return hardcoded list for now - in a real app this would come from API
        return [
            Translation(
                id: "ESV", title: "English Standard Version", version: 1,
                isDownloaded: hasTranslation("ESV")),
            Translation(
                id: "NIV", title: "New International Version", version: 1,
                isDownloaded: hasTranslation("NIV")),
            Translation(
                id: "KJV", title: "King James Version", version: 1,
                isDownloaded: hasTranslation("KJV")),
        ]
    }

    private func hasTranslation(_ translationId: String) -> Bool {
        var statement: OpaquePointer?
        let query = "SELECT COUNT(*) FROM translations WHERE id = ?"

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else { return false }
        sqlite3_bind_text(statement, 1, translationId, -1, nil)

        var count = 0
        if sqlite3_step(statement) == SQLITE_ROW {
            count = Int(sqlite3_column_int(statement, 0))
        }

        sqlite3_finalize(statement)
        return count > 0
    }

    func getPreviousBook(_ passage: Passage) -> BookMetadata? {
        let books = getBookList(passage.translation)
        guard let currentIndex = books.firstIndex(where: { $0.id == passage.book }),
            currentIndex > 0
        else { return nil }
        return books[currentIndex - 1]
    }

    func getNextBook(_ passage: Passage) -> BookMetadata? {
        let books = getBookList(passage.translation)
        guard let currentIndex = books.firstIndex(where: { $0.id == passage.book }),
            currentIndex < books.count - 1
        else { return nil }
        return books[currentIndex + 1]
    }

    func getBookMetadata(_ bookId: String, _ translation: String) -> BookMetadata? {
        var statement: OpaquePointer?
        let query =
            "SELECT id, title, abbreviation, chapter_count FROM books WHERE id = ? AND translation = ?"

        guard sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK else { return nil }
        sqlite3_bind_text(statement, 1, bookId, -1, nil)
        sqlite3_bind_text(statement, 2, translation, -1, nil)

        var book: BookMetadata?
        if sqlite3_step(statement) == SQLITE_ROW {
            let id = String(cString: sqlite3_column_text(statement, 0))
            let title = String(cString: sqlite3_column_text(statement, 1))
            let abbreviation = String(cString: sqlite3_column_text(statement, 2))
            let chapterCount = Int(sqlite3_column_int(statement, 3))

            book = BookMetadata(
                id: id, title: title, abbreviation: abbreviation, chapterCount: chapterCount)
        }

        sqlite3_finalize(statement)
        return book
    }
}

enum DatabaseError: Error {
    case failedToOpen
    case failedToQuery
}
