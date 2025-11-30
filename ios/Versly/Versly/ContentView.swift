import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            Tab("Read", systemImage: "book.fill") {
                ReadView()
            }

            Tab("Plans", systemImage: "checklist") {
                PlansView()
            }

            Tab("Search", systemImage: "magnifyingglass") {
                SearchView()
            }

            Tab("Settings", systemImage: "gear") {
                SettingsView()
            }
        }
    }
}

#Preview {
    ContentView()
}
