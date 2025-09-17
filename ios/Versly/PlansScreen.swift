import SwiftUI

struct PlansScreen: View {
    @ObservedObject var appPreferences: AppPreferences
    @ObservedObject var bibleDatabase: BibleDatabase

    @State private var passages: [Passage] = []
    @State private var isLoading = true
    @State private var errorMessage: String?

    var body: some View {
        NavigationView {
            Group {
                if isLoading {
                    LoadingSpinner()
                } else if let errorMessage = errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 48))
                            .foregroundColor(.orange)

                        Text("No Readings for Today")
                            .font(.title2)
                            .fontWeight(.bold)

                        Text(errorMessage)
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(32)
                } else if passages.isEmpty {
                    VStack(spacing: 16) {
                        Image(systemName: "book.closed")
                            .font(.system(size: 48))
                            .foregroundColor(.secondary)

                        Text("No Readings for Today")
                            .font(.title2)
                            .fontWeight(.bold)

                        Text("Check back tomorrow for your daily reading plan.")
                            .font(.body)
                            .foregroundColor(.secondary)
                            .multilineTextAlignment(.center)
                    }
                    .padding(32)
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 0) {
                            // Reading references header
                            HStack(spacing: 8) {
                                ForEach(passages, id: \.id) { passage in
                                    Text("\(passage.bookAbbreviation) \(passage.chapter)")
                                        .font(.caption)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color.accentColor.opacity(0.1))
                                        .cornerRadius(4)
                                }
                            }
                            .padding(.horizontal, 16)
                            .padding(.vertical, 8)

                            // Reading content
                            LazyVStack(alignment: .leading, spacing: 0) {
                                ForEach(passages.flatMap { $0.nodes }, id: \.id) { node in
                                    ReaderNodeView(node: node)
                                        .padding(.horizontal, 16)
                                }

                                Spacer()
                                    .frame(height: 32)
                            }
                        }
                    }
                }
            }
            .navigationTitle("Today's Reading")
            .refreshable {
                await loadTodaysReadings()
            }
        }
        .task {
            await loadTodaysReadings()
        }
        .onChange(of: appPreferences.selectedTranslation) { _ in
            Task {
                await loadTodaysReadings()
            }
        }
    }

    @MainActor
    private func loadTodaysReadings() async {
        guard !appPreferences.selectedTranslation.isEmpty else {
            isLoading = false
            errorMessage = "No translation selected"
            return
        }

        isLoading = true
        errorMessage = nil

        do {
            let readings = try await getTodaysReadings()
            var loadedPassages: [Passage] = []

            for reading in readings {
                if let passage = bibleDatabase.getPassage(
                    book: reading.book,
                    chapter: String(reading.chapter),
                    translation: appPreferences.selectedTranslation
                ) {
                    loadedPassages.append(passage)
                }
            }

            passages = loadedPassages

            if passages.isEmpty && !readings.isEmpty {
                errorMessage = "Readings not available in selected translation"
            }

        } catch {
            errorMessage = "Failed to load reading plan: \(error.localizedDescription)"
        }

        isLoading = false
    }

    private func getTodaysReadings() async throws -> [Reading] {
        guard let path = Bundle.main.path(forResource: "plan", ofType: "json"),
            let data = NSData(contentsOfFile: path) as? Data
        else {
            throw PlanError.fileNotFound
        }

        let readingPlan = try JSONDecoder().decode(ReadingPlan.self, from: data)
        let today = getCurrentDateString()

        guard let todaysDay = readingPlan.plan.days.first(where: { $0.date == today }) else {
            throw PlanError.noReadingsForToday
        }

        return todaysDay.readings
    }

    private func getCurrentDateString() -> String {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date())
    }
}

enum PlanError: LocalizedError {
    case fileNotFound
    case noReadingsForToday

    var errorDescription: String? {
        switch self {
        case .fileNotFound:
            return "Reading plan file not found"
        case .noReadingsForToday:
            return "No readings scheduled for today"
        }
    }
}
