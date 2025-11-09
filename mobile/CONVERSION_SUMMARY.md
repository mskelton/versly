# Versly React Native Conversion Summary

This document summarizes the conversion of the Versly Android app (Jetpack Compose) to React Native with NativeWind.

## Completed Features

### Core Infrastructure
- ✅ React Native project setup with TypeScript
- ✅ NativeWind (Tailwind CSS) configuration
- ✅ Navigation setup with bottom tabs (Read, Plans, Search)
- ✅ Theme system with light/dark mode support
- ✅ State management with Zustand

### Data Layer
- ✅ TypeScript type definitions for all data models
- ✅ SQLite database service with schema initialization
- ✅ AppPreferences service for persistent settings
- ✅ API service for downloading translations
- ✅ Database methods for:
  - Getting book lists
  - Loading passages
  - Navigation (next/previous chapter)
  - Translation management

### UI Components
- ✅ USFM text rendering with support for:
  - Paragraphs (p, m, pr, cls, etc.)
  - Poetry (q1-q4)
  - Headings (s1-s3, ms, d, sp)
  - Lists (li1-li4)
  - Inline styles (bold, italic, small caps)
  - Words of Jesus highlighting
  - Verse numbers
- ✅ BookPicker modal
- ✅ ChapterPicker modal
- ✅ BottomToolbar for navigation
- ✅ LoadingScreen and LoadingSpinner
- ✅ Chapter header display

### Screens
- ✅ ReadScreen - Main Bible reading interface with:
  - Passage display
  - Previous/Next chapter buttons
  - Book and chapter selection
- ✅ PlansScreen - Daily reading plans with:
  - Date-based plan loading
  - Multiple passages per day
  - Empty state handling
- ✅ SearchScreen - Placeholder for future search functionality

### Assets
- ✅ Copied schema.sql to Android assets
- ✅ Copied plan.json to Android assets

## Project Structure

```
mobile/
├── src/
│   ├── components/          # Reusable UI components
│   │   ├── ReaderNode.tsx   # USFM text rendering
│   │   ├── BookPicker.tsx
│   │   ├── ChapterPicker.tsx
│   │   ├── BottomToolbar.tsx
│   │   ├── LoadingScreen.tsx
│   │   └── LoadingSpinner.tsx
│   ├── screens/             # Main app screens
│   │   ├── ReadScreen.tsx
│   │   ├── PlansScreen.tsx
│   │   └── SearchScreen.tsx
│   ├── services/            # Business logic layer
│   │   ├── database.ts      # SQLite operations
│   │   ├── preferences.ts   # AsyncStorage wrapper
│   │   └── api.ts           # Network requests
│   ├── models/              # TypeScript types
│   │   └── types.ts
│   ├── store/               # State management
│   │   └── appStore.ts      # Zustand store
│   ├── theme/               # Theme system
│   │   ├── colors.ts
│   │   └── ThemeContext.tsx
│   ├── navigation/          # Navigation setup
│   │   └── AppNavigator.tsx
│   └── assets/              # Static assets
│       ├── schema.sql
│       └── plan.json
├── android/                 # Android native code
└── App.tsx                  # Root component
```

## Key Implementation Details

### USFM Rendering
The USFM text rendering supports most of the original Android implementation:
- Paragraph styles with appropriate indentation
- Poetry with multi-level indentation
- Inline character styles (bold, italic, small caps)
- Words of Jesus in custom color
- Verse numbers as superscript-style text

### Database Integration
- Uses `react-native-sqlite-storage` for SQLite access
- Schema is loaded from Android assets on first run
- Supports downloading translations from the API
- Batch inserts for performance

### State Management
- Zustand store handles all app state
- Async actions for database and API operations
- Reactive updates to UI components

### Navigation
- Bottom tab navigator with 3 tabs
- Material-style theming
- Responsive to theme changes

## Known Limitations & Future Work

1. **iOS Support**: iOS pod installation had issues with RNWorklets dependency. The Android build should work, but iOS needs additional configuration.

2. **Fonts**: Rubik font configuration is not yet implemented. Need to:
   - Copy Rubik font files to assets
   - Configure react-native.config.js
   - Link fonts properly

3. **Background Sync**: Translation update background worker not yet implemented. Would need:
   - `react-native-background-fetch` or similar
   - Periodic translation update checks

4. **Advanced USFM Features**: Some complex USFM features may need refinement:
   - Tables
   - Complex nested styles
   - Text alignment (right, center)

5. **Performance Optimizations**:
   - Virtualized lists for long passages
   - Memoization of rendered nodes
   - Lazy loading of translations

6. **Testing**: No unit or integration tests yet

## Running the App

```bash
cd mobile

# Install dependencies
npm install

# Run on Android
npm run android

# Run on iOS (after fixing pod issues)
npm run ios
```

## Differences from Original Android App

### Architecture
- **Android**: Jetpack Compose with ViewModels and Flows
- **React Native**: Functional components with hooks and Zustand

### UI Framework
- **Android**: Material 3 Compose components
- **React Native**: Custom components with NativeWind styling

### Database
- **Android**: Direct SQLite with SQLiteOpenHelper
- **React Native**: react-native-sqlite-storage wrapper

### Preferences
- **Android**: DataStore with Flow
- **React Native**: AsyncStorage with Promises

### Navigation
- **Android**: Compose Navigation with bottom bar
- **React Native**: React Navigation with bottom tabs

## Migration Notes

This is a functional 1:1 port of the core Bible reading features. The app should:
1. Initialize and download ESV translation on first launch
2. Display Bible passages with proper USFM formatting
3. Navigate between books and chapters
4. Show daily reading plans
5. Support light/dark themes
6. Persist reading position

The codebase is clean, typed, and follows React Native best practices. It's ready for further development and customization.
