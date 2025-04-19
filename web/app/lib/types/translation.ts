import type { Node } from './usfm'

export type Chapter = {
	html?: string
	nodes: Node[]
	ref: number
}

export type Book = {
	chapters: Chapter[]
	ref: string
	title: string
}

export type Translation = {
	books: Book[]
	ref: string
	title: string
}
