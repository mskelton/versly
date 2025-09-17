import Combine
import Foundation

class AppPreferences: ObservableObject {
    private let userDefaults = UserDefaults.standard

    @Published var selectedBook: String = "GEN"
    @Published var selectedChapter: String = "1"
    @Published var selectedTranslation: String = "ESV"
    @Published var selectedDestination: Int = 0

    private var cancellables = Set<AnyCancellable>()

    init() {
        loadPreferences()
        setupBindings()
    }

    private func loadPreferences() {
        selectedBook = userDefaults.string(forKey: "selectedBook") ?? "GEN"
        selectedChapter = userDefaults.string(forKey: "selectedChapter") ?? "1"
        selectedTranslation = userDefaults.string(forKey: "selectedTranslation") ?? "ESV"
        selectedDestination = userDefaults.integer(forKey: "selectedDestination")
    }

    private func setupBindings() {
        $selectedBook
            .sink { [weak self] value in
                self?.userDefaults.set(value, forKey: "selectedBook")
            }
            .store(in: &cancellables)

        $selectedChapter
            .sink { [weak self] value in
                self?.userDefaults.set(value, forKey: "selectedChapter")
            }
            .store(in: &cancellables)

        $selectedTranslation
            .sink { [weak self] value in
                self?.userDefaults.set(value, forKey: "selectedTranslation")
            }
            .store(in: &cancellables)

        $selectedDestination
            .sink { [weak self] value in
                self?.userDefaults.set(value, forKey: "selectedDestination")
            }
            .store(in: &cancellables)
    }
}
