import SwiftUI

struct ContentView: View {
    var body: some View {
        TabView {
            Tab("Read", systemImage: "tray.and.arrow.down.fill") {
                ReadView()
            }

            Tab("Plans", systemImage: "tray.and.arrow.up.fill") {
                PlansView()
            }

            Tab("Search", systemImage: "person.crop.circle.fill") {
                SearchView()
            }

            Tab("Settings", systemImage: "person.crop.circle.fill") {
                SettingsView()
            }
        }
    }
}

#Preview {
    ContentView()
}
