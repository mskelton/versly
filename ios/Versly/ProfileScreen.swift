import SwiftUI

struct ProfileScreen: View {
    @ObservedObject var appPreferences: AppPreferences
    @ObservedObject var bibleDatabase: BibleDatabase

    @State private var translations: [Translation] = []
    @State private var downloadingTranslation: String?
    @State private var refreshTrigger = 0

    var downloadedTranslations: [Translation] {
        translations.filter { $0.isDownloaded }
    }

    var remoteTranslations: [Translation] {
        translations.filter { !$0.isDownloaded }
    }

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    if !downloadedTranslations.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Downloaded Translations")
                                .font(.title3)
                                .fontWeight(.medium)
                                .padding(.horizontal, 16)

                            VStack(spacing: 0) {
                                ForEach(downloadedTranslations) { translation in
                                    TranslationRow(
                                        translation: translation,
                                        isSelected: appPreferences.selectedTranslation
                                            == translation.id,
                                        isDownloading: downloadingTranslation == translation.id
                                    ) {
                                        appPreferences.selectedTranslation = translation.id
                                    }

                                    if translation.id != downloadedTranslations.last?.id {
                                        Divider()
                                            .padding(.horizontal, 16)
                                    }
                                }
                            }
                            .background(Color(.systemBackground))
                            .cornerRadius(12)
                            .padding(.horizontal, 16)
                        }
                    }

                    if !remoteTranslations.isEmpty {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("Available for Download")
                                .font(.title3)
                                .fontWeight(.medium)
                                .padding(.horizontal, 16)

                            VStack(spacing: 0) {
                                ForEach(remoteTranslations) { translation in
                                    TranslationRow(
                                        translation: translation,
                                        isSelected: appPreferences.selectedTranslation
                                            == translation.id,
                                        isDownloading: downloadingTranslation == translation.id
                                    ) {
                                        Task {
                                            await downloadTranslation(translation.id)
                                        }
                                    }

                                    if translation.id != remoteTranslations.last?.id {
                                        Divider()
                                            .padding(.horizontal, 16)
                                    }
                                }
                            }
                            .background(Color(.systemBackground))
                            .cornerRadius(12)
                            .padding(.horizontal, 16)
                        }
                    }

                    Spacer(minLength: 100)
                }
                .padding(.top)
            }
            .navigationTitle("Settings")
            .refreshable {
                await loadTranslations()
            }
        }
        .task {
            await loadTranslations()
        }
    }

    @MainActor
    private func loadTranslations() async {
        translations = bibleDatabase.getAvailableTranslations()
        refreshTrigger += 1
    }

    @MainActor
    private func downloadTranslation(_ translationId: String) async {
        guard downloadingTranslation == nil else { return }

        downloadingTranslation = translationId

        do {
            try await bibleDatabase.downloadTranslation(translationId)
            await loadTranslations()
            appPreferences.selectedTranslation = translationId
        } catch {
            print("Failed to download translation \(translationId): \(error)")
        }

        downloadingTranslation = nil
    }
}

struct TranslationRow: View {
    let translation: Translation
    let isSelected: Bool
    let isDownloading: Bool
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 12) {
                // Radio button
                Circle()
                    .fill(isSelected ? Color.accentColor : Color.clear)
                    .frame(width: 20, height: 20)
                    .overlay(
                        Circle()
                            .stroke(isSelected ? Color.accentColor : Color.secondary, lineWidth: 2)
                    )
                    .overlay(
                        Circle()
                            .fill(Color.white)
                            .frame(width: 8, height: 8)
                            .opacity(isSelected ? 1 : 0)
                    )

                VStack(alignment: .leading, spacing: 4) {
                    Text(translation.id)
                        .font(.body)
                        .fontWeight(isSelected ? .bold : .medium)
                        .foregroundColor(.primary)

                    Text(translation.title)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }

                Spacer()

                if isDownloading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                        .scaleEffect(0.8)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .disabled(isDownloading)
    }
}
