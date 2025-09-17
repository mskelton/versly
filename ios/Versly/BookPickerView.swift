import SwiftUI

struct BookPickerView: View {
    let books: [BookMetadata]
    let onBookSelected: (BookMetadata) -> Void
    let onCancel: () -> Void

    @State private var searchText = ""

    var filteredBooks: [BookMetadata] {
        if searchText.isEmpty {
            return books
        } else {
            return books.filter {
                $0.title.lowercased().contains(searchText.lowercased())
                    || $0.abbreviation.lowercased().contains(searchText.lowercased())
            }
        }
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture {
                    onCancel()
                }

            VStack {
                // Header
                HStack {
                    Button("Cancel") {
                        onCancel()
                    }

                    Spacer()

                    Text("Select Book")
                        .font(.headline)
                        .fontWeight(.semibold)

                    Spacer()

                    // Invisible button to balance layout
                    Button("Cancel") {
                        onCancel()
                    }
                    .opacity(0)
                }
                .padding()

                // Search bar
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.secondary)

                    TextField("Search books", text: $searchText)
                        .textFieldStyle(.plain)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color(.systemGray6))
                .cornerRadius(8)
                .padding(.horizontal)

                // Book list
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(filteredBooks) { book in
                            Button(action: {
                                onBookSelected(book)
                            }) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(book.title)
                                            .font(.body)
                                            .fontWeight(.medium)
                                            .foregroundColor(.primary)

                                        Text("\(book.chapterCount) chapters")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                    }

                                    Spacer()

                                    Text(book.abbreviation)
                                        .font(.caption)
                                        .fontWeight(.medium)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color.accentColor.opacity(0.2))
                                        .cornerRadius(4)
                                }
                                .padding(.horizontal, 16)
                                .padding(.vertical, 12)
                            }
                            .buttonStyle(.plain)

                            if book.id != filteredBooks.last?.id {
                                Divider()
                                    .padding(.horizontal, 16)
                            }
                        }
                    }
                }
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .padding(.horizontal)

                Spacer()
            }
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .padding()
            .shadow(radius: 10)
        }
    }
}

struct ChapterPickerView: View {
    let totalChapters: Int
    let selectedChapter: String
    let onChapterSelected: (String) -> Void
    let onCancel: () -> Void

    private let columns = Array(repeating: GridItem(.flexible()), count: 6)

    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture {
                    onCancel()
                }

            VStack {
                // Header
                HStack {
                    Button("Cancel") {
                        onCancel()
                    }

                    Spacer()

                    Text("Select Chapter")
                        .font(.headline)
                        .fontWeight(.semibold)

                    Spacer()

                    // Invisible button to balance layout
                    Button("Cancel") {
                        onCancel()
                    }
                    .opacity(0)
                }
                .padding()

                // Chapter grid
                ScrollView {
                    LazyVGrid(columns: columns, spacing: 12) {
                        ForEach(1...totalChapters, id: \.self) { chapter in
                            Button(action: {
                                onChapterSelected(String(chapter))
                            }) {
                                Text(String(chapter))
                                    .font(.body)
                                    .fontWeight(.medium)
                                    .frame(width: 44, height: 44)
                                    .background(
                                        selectedChapter == String(chapter)
                                            ? Color.accentColor
                                            : Color(.systemGray5)
                                    )
                                    .foregroundColor(
                                        selectedChapter == String(chapter)
                                            ? .white
                                            : .primary
                                    )
                                    .cornerRadius(8)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding()
                }
                .background(Color(.systemBackground))
                .cornerRadius(12)
                .padding(.horizontal)

                Spacer()
            }
            .background(Color(.systemBackground))
            .cornerRadius(12)
            .padding()
            .shadow(radius: 10)
        }
    }
}
