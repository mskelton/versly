import { create } from 'zustand'
import { PassageId, Passage, BookMetadata, Translation } from '../models/types'
import { database } from '../services/database'
import { preferences } from '../services/preferences'
import { api } from '../services/api'

interface AppStore {
  currentPassage: PassageId | null
  selectedTab: number
  passage: Passage | null
  books: BookMetadata[]
  translations: Translation[]
  isLoading: boolean
  isInitialized: boolean
  error: string | null

  init: () => Promise<void>
  setCurrentPassage: (passage: PassageId) => Promise<void>
  setSelectedTab: (tab: number) => Promise<void>
  loadPassage: (passageId: PassageId) => Promise<void>
  loadBooks: (translationId: string) => Promise<void>
  goToNextChapter: () => Promise<void>
  goToPreviousChapter: () => Promise<void>
  downloadTranslation: (translationId: string) => Promise<void>
  loadAvailableTranslations: () => Promise<void>
}

export const useAppStore = create<AppStore>((set, get) => ({
  currentPassage: null,
  selectedTab: 0,
  passage: null,
  books: [],
  translations: [],
  isLoading: false,
  isInitialized: false,
  error: null,

  init: async () => {
    try {
      set({ isLoading: true, error: null })

      await database.init()

      const initialized = await database.isInitialized()

      if (!initialized) {
        await get().downloadTranslation('ESV')
      }

      const [currentPassage, selectedTab] = await Promise.all([
        preferences.getPassage(),
        preferences.getDestination(),
      ])

      set({ currentPassage, selectedTab, isInitialized: true })

      await get().loadPassage(currentPassage)
      await get().loadBooks(currentPassage.translation)
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Failed to initialize',
      })
    } finally {
      set({ isLoading: false })
    }
  },

  setCurrentPassage: async (passage: PassageId) => {
    set({ currentPassage: passage })
    await preferences.setPassage(passage)
    await get().loadPassage(passage)
  },

  setSelectedTab: async (tab: number) => {
    set({ selectedTab: tab })
    await preferences.setDestination(tab)
  },

  loadPassage: async (passageId: PassageId) => {
    try {
      set({ isLoading: true, error: null })
      const passage = await database.getPassage(
        passageId.book,
        passageId.chapter,
        passageId.translation,
      )
      set({ passage, isLoading: false })
    } catch (error) {
      set({
        error:
          error instanceof Error ? error.message : 'Failed to load passage',
        isLoading: false,
      })
    }
  },

  loadBooks: async (translationId: string) => {
    try {
      const books = await database.getBookList(translationId)
      set({ books })
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : 'Failed to load books',
      })
    }
  },

  goToNextChapter: async () => {
    const { currentPassage } = get()
    if (!currentPassage) return

    try {
      const next = await database.getNextChapter(
        currentPassage.book,
        currentPassage.chapter,
        currentPassage.translation,
      )

      if (next) {
        await get().setCurrentPassage(next)
      }
    } catch (error) {
      set({
        error:
          error instanceof Error
            ? error.message
            : 'Failed to go to next chapter',
      })
    }
  },

  goToPreviousChapter: async () => {
    const { currentPassage } = get()
    if (!currentPassage) return

    try {
      const prev = await database.getPreviousChapter(
        currentPassage.book,
        currentPassage.chapter,
        currentPassage.translation,
      )

      if (prev) {
        await get().setCurrentPassage(prev)
      }
    } catch (error) {
      set({
        error:
          error instanceof Error
            ? error.message
            : 'Failed to go to previous chapter',
      })
    }
  },

  downloadTranslation: async (translationId: string) => {
    try {
      set({ isLoading: true, error: null })
      const data = await api.downloadTranslation(translationId)
      await database.downloadTranslation(translationId, data)
      set({ isLoading: false })
    } catch (error) {
      set({
        error:
          error instanceof Error
            ? error.message
            : 'Failed to download translation',
        isLoading: false,
      })
    }
  },

  loadAvailableTranslations: async () => {
    try {
      const [available, downloaded] = await Promise.all([
        api.getTranslations(),
        database.getTranslations(),
      ])

      const downloadedIds = new Set(downloaded.map((t) => t.id))
      const translations: Translation[] = available.map((t) => ({
        id: t.id,
        title: t.name,
        lastUpdated: t.lastUpdated,
        isDownloaded: downloadedIds.has(t.id),
      }))

      set({ translations })
    } catch (error) {
      set({
        error:
          error instanceof Error
            ? error.message
            : 'Failed to load translations',
      })
    }
  },
}))
