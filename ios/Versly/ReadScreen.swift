import SwiftUI

struct ReadScreen: View {
    @ObservedObject var appPreferences: AppPreferences
    @ObservedObject var bibleDatabase: BibleDatabase

    @State private var passages: [Passage] = []
    @State private var isLoading = false
    @State private var books: [BookMetadata] = []
    @State private var showBookPicker = false
    @State private var showChapterPicker = false
    @State private var totalChapters = 0

    private let maxPassages = 10

    var body: some View {
        NavigationView {
            ZStack {
                if isLoading && passages.isEmpty {
                    LoadingSpinner()
                } else if !passages.isEmpty {
                    ScrollViewReader { proxy in
                        ScrollView {
                            LazyVStack(alignment: .leading, spacing: 0) {
                                ForEach(passages.flatMap { $0.nodes }, id: \.id) { node in
                                    ReaderNodeView(node: node)
                                        .padding(.horizontal, 16)
                                        .onAppear {
                                            if node.id == passages.first?.nodes.first?.id {
                                                loadPreviousChapter()
                                            } else if node.id == passages.last?.nodes.last?.id {
                                                loadNextChapter()
                                            }
                                        }
                                }

                                Spacer()
                                    .frame(height: 120)
                            }
                        }
                        .onChange(of: appPreferences.selectedBook) { _ in
                            Task {
                                await reloadCurrentChapter()
                                withAnimation {
                                    proxy.scrollTo(passages.first?.nodes.first?.id, anchor: .top)
                                }
                            }
                        }
                        .onChange(of: appPreferences.selectedChapter) { _ in
                            Task {
                                await reloadCurrentChapter()
                                withAnimation {
                                    proxy.scrollTo(passages.first?.nodes.first?.id, anchor: .top)
                                }
                            }
                        }
                        .onChange(of: appPreferences.selectedTranslation) { _ in
                            Task {
                                await loadInitialData()
                                withAnimation {
                                    proxy.scrollTo(passages.first?.nodes.first?.id, anchor: .top)
                                }
                            }
                        }
                    }

                    VStack {
                        Spacer()

                        ChapterNavigationFooter(
                            selectedBook: appPreferences.selectedBook,
                            selectedChapter: appPreferences.selectedChapter,
                            books: books
                        ) {
                            showBookPicker = true
                        }
                        .background(Color(UIColor.systemBackground))
                    }
                }

                if showBookPicker {
                    BookPickerView(books: books) { selectedBook in
                        appPreferences.selectedBook = selectedBook.id
                        appPreferences.selectedChapter = "1"
                        totalChapters = selectedBook.chapterCount
                        showBookPicker = false

                        if selectedBook.chapterCount > 1 {
                            showChapterPicker = true
                        }
                    } onCancel: {
                        showBookPicker = false
                    }
                } else if showChapterPicker {
                    ChapterPickerView(
                        totalChapters: totalChapters,
                        selectedChapter: appPreferences.selectedChapter
                    ) { selectedChapter in
                        appPreferences.selectedChapter = selectedChapter
                        showChapterPicker = false
                    } onCancel: {
                        showChapterPicker = false
                    }
                }
            }
        }
        .task {
            await loadInitialData()
        }
    }

    @MainActor
    private func loadInitialData() async {
        isLoading = true
        books = bibleDatabase.getBookList(appPreferences.selectedTranslation)

        if let passage = bibleDatabase.getPassage(
            book: appPreferences.selectedBook,
            chapter: appPreferences.selectedChapter,
            translation: appPreferences.selectedTranslation
        ) {
            passages = [passage]
        }

        isLoading = false
    }

    @MainActor
    private func reloadCurrentChapter() async {
        if let passage = bibleDatabase.getPassage(
            book: appPreferences.selectedBook,
            chapter: appPreferences.selectedChapter,
            translation: appPreferences.selectedTranslation
        ) {
            passages = [passage]
        }
    }

    private func loadPreviousChapter() {
        guard !isLoading, let firstPassage = passages.first else { return }

        isLoading = true

        Task {
            if let previousPassage = await loadPreviousChapter(from: firstPassage) {
                await MainActor.run {
                    passages = ([previousPassage] + passages).suffix(maxPassages)
                    isLoading = false
                }
            } else {
                await MainActor.run {
                    isLoading = false
                }
            }
        }
    }

    private func loadNextChapter() {
        guard !isLoading, let lastPassage = passages.last else { return }

        isLoading = true

        Task {
            if let nextPassage = await loadNextChapter(from: lastPassage) {
                await MainActor.run {
                    // passages = (passages + [nextPassage]).prefix(maxPassages)
                    isLoading = false
                }
            } else {
                await MainActor.run {
                    isLoading = false
                }
            }
        }
    }

    private func loadPreviousChapter(from passage: Passage) async -> Passage? {
        let chapter = Int(passage.chapter) ?? 1

        if chapter > 1 {
            return bibleDatabase.getPassage(
                book: passage.book,
                chapter: String(chapter - 1),
                translation: passage.translation
            )
        }

        if let previousBook = bibleDatabase.getPreviousBook(passage) {
            return bibleDatabase.getPassage(
                book: previousBook.id,
                chapter: String(previousBook.chapterCount),
                translation: passage.translation
            )
        }

        return nil
    }

    private func loadNextChapter(from passage: Passage) async -> Passage? {
        let chapter = Int(passage.chapter) ?? 1

        if let metadata = bibleDatabase.getBookMetadata(passage.book, passage.translation),
            chapter < metadata.chapterCount
        {
            return bibleDatabase.getPassage(
                book: passage.book,
                chapter: String(chapter + 1),
                translation: passage.translation
            )
        }

        if let nextBook = bibleDatabase.getNextBook(passage) {
            return bibleDatabase.getPassage(
                book: nextBook.id,
                chapter: "1",
                translation: passage.translation
            )
        }

        return nil
    }
}

struct ChapterNavigationFooter: View {
    let selectedBook: String
    let selectedChapter: String
    let books: [BookMetadata]
    let onTap: () -> Void

    var body: some View {
        let selectedBookTitle = books.first { $0.id == selectedBook }?.title ?? selectedBook

        HStack {
            Spacer()

            Button(action: onTap) {
                Text("\(selectedBookTitle) \(selectedChapter)")
                    .font(.headline)
                    .fontWeight(.bold)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                    .background(Color.accentColor.opacity(0.1))
                    .cornerRadius(25)
            }
            .buttonStyle(.plain)

            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color(UIColor.systemBackground))
    }
}

struct LoadingSpinner: View {
    var body: some View {
        VStack {
            Spacer()
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle())
                .scaleEffect(1.5)
            Spacer()
        }
    }
}
