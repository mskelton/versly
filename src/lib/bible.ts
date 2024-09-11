import esv from "../data/esv.json"

export type BibleVerse = {
  ref: string
  text: string
}

export type BibleChapter = {
  ref: string
  verses: BibleVerse[]
}

export type BibleBook = {
  ref: string
  chapters: BibleChapter[]
}

export type Bible = {
  books: BibleBook[]
}

export const bible = esv as Bible
