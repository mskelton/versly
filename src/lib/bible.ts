import esv from "../data/esv.json"

type BibleChapter = {
  ref: number
  content: string
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

type TranslationKey = "esv"

export const translations: Record<TranslationKey, BibleTranslation> = {
  esv,
}

export const DEFAULT_TRANSLATION: TranslationKey = "esv"
export const DEFAULT_BOOK = "JHN"
