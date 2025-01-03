import ESV from "../data/ESV.json"

type BibleChapter = {
  ref: number
  html: string
}

type BibleBook = {
  ref: string
  title: string
  chapters: BibleChapter[]
}

type BibleTranslation = {
  ref: string
  title: string
  books: BibleBook[]
}

type TranslationKey = "ESV"

export const translations: Record<TranslationKey, BibleTranslation> = {
  ESV,
}

export const DEFAULT_TRANSLATION: TranslationKey = "ESV"
export const DEFAULT_BOOK = "JHN"
