//
//  ContentView.swift
//  Versly
//
//  Created by Mark Skelton on 9/14/25.
//

import SwiftUI

struct ContentView: View {
    @StateObject private var verslyService = VerslyService()
    @StateObject private var appPreferences = AppPreferences()
    @StateObject private var bibleDatabase: BibleDatabase

    @State private var isInitialized = false
    @State private var isInitializing = false

    init() {
        let service = VerslyService()
        _verslyService = StateObject(wrappedValue: service)
        _bibleDatabase = StateObject(wrappedValue: BibleDatabase(verslyService: service))
    }

    var body: some View {
        Group {
            if isInitializing {
                LoadingScreen()
            } else if isInitialized {
                MainTabView(
                    appPreferences: appPreferences,
                    bibleDatabase: bibleDatabase
                )
            } else {
                LoadingScreen()
                    .onAppear {
                        Task {
                            await initializeApp()
                        }
                    }
            }
        }
    }

    @MainActor
    private func initializeApp() async {
        isInitializing = true

        if !bibleDatabase.isInitialized() {
            do {
                try await bibleDatabase.downloadTranslation("ESV")
            } catch {
                print("Failed to download default translation: \(error)")
            }
        }

        isInitialized = true
        isInitializing = false
    }
}

struct LoadingScreen: View {
    var body: some View {
        VStack(spacing: 20) {
            ProgressView()
                .progressViewStyle(CircularProgressViewStyle())
                .scaleEffect(2)

            Text("Loading ESV Bible...")
                .font(.title3)
                .foregroundColor(.secondary)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }
}

struct MainTabView: View {
    @ObservedObject var appPreferences: AppPreferences
    @ObservedObject var bibleDatabase: BibleDatabase

    var body: some View {
        TabView(selection: $appPreferences.selectedDestination) {
            ReadScreen(
                appPreferences: appPreferences,
                bibleDatabase: bibleDatabase
            )
            .tabItem {
                Image(systemName: appPreferences.selectedDestination == 0 ? "book.fill" : "book")
                Text("Read")
            }
            .tag(0)

            PlansScreen(
                appPreferences: appPreferences,
                bibleDatabase: bibleDatabase
            )
            .tabItem {
                Image(
                    systemName: appPreferences.selectedDestination == 1
                        ? "checklist" : "list.bullet")
                Text("Plans")
            }
            .tag(1)

            ProfileScreen(
                appPreferences: appPreferences,
                bibleDatabase: bibleDatabase
            )
            .tabItem {
                Image(
                    systemName: appPreferences.selectedDestination == 2 ? "person.fill" : "person")
                Text("Profile")
            }
            .tag(2)
        }
    }
}

#Preview {
    ContentView()
}
