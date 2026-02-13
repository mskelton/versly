import { notFound } from 'next/navigation'
import { bible } from './db'
import { buildPassageId, PassageId } from './passageId'
import { Node, Span } from './types/usfm'

const getPassageQuery = bible.prepare<
  { bookId: string; chapterId: string; translationId: string },
  { bookAbbreviation: string; bookTitle: string; data: string }
>(
  `
    SELECT book.title as bookTitle, book.abbreviation as bookAbbreviation, chapter.data
    FROM chapter
    JOIN book ON book.id = chapter.book_id
    WHERE book.id = @bookId AND chapter.id = @chapterId AND chapter.translation_id = @translationId
  `,
)

export type Passage = {
  bookAbbreviation: string
  bookTitle: string
  id: string
  nodes: Node[]
  range: [start: string, end: string] | null
}

const STRUCTURAL_NODE_TYPES = new Set(['ms', 's1', 's2', 's3', 'sp', 'd', 'iex'])

function isStructuralNode(type: string): boolean {
  return STRUCTURAL_NODE_TYPES.has(type)
}

function getNodeChildren(node: Node): Span[] {
  if (node[0] === 'table') return []
  const children = node[1]
  return typeof children === 'string' ? [] : (children as Span[])
}

/**
 * Find the next verse number in nodes starting at startIndex.
 * Uses initialVerse if no verse marker is found in the remainder.
 */
function findNextVerse(nodes: Node[], startIndex: number, initialVerse: number): number {
  for (let i = startIndex; i < nodes.length; i++) {
    const children = getNodeChildren(nodes[i])
    for (const span of children) {
      if (typeof span !== 'string' && span[0] === 'v') {
        const n = parseInt(span[1], 10)
        return Number.isNaN(n) ? initialVerse : n
      }
    }
  }
  return initialVerse
}

/**
 * Filter spans by verse range. currentVerse defaults to 1 for spans before any verse marker.
 * Returns [filteredSpans, lastVerseInRange].
 */
function filterSpansByRange(
  spans: Span[],
  startVerse: number,
  endVerse: number,
  initialVerse: number,
): [Span[], number | null] {
  const filtered: Span[] = []
  let currentVerse = initialVerse
  let lastVerseInRange: number | null = null

  for (const span of spans) {
    if (typeof span === 'string') {
      if (currentVerse >= startVerse && currentVerse <= endVerse) {
        filtered.push(span)
        if (lastVerseInRange === null || currentVerse > lastVerseInRange) {
          lastVerseInRange = currentVerse
        }
      }
    } else {
      if (span[0] === 'v') {
        const verseNum = parseInt(span[1], 10)
        if (!Number.isNaN(verseNum)) currentVerse = verseNum

        if (verseNum < startVerse) {
          // before range
        } else if (verseNum >= startVerse && verseNum <= endVerse) {
          filtered.push(span)
          lastVerseInRange = verseNum
        } else if (verseNum > endVerse) {
          break
        }
      } else {
        if (currentVerse >= startVerse && currentVerse <= endVerse) {
          filtered.push(span)
          if (lastVerseInRange === null || currentVerse > lastVerseInRange) {
            lastVerseInRange = currentVerse
          }
        }
      }
    }
  }

  return [filtered, lastVerseInRange]
}

function getLastVerseFromSpans(spans: Span[]): number | null {
  let last: number | null = null
  for (const span of spans) {
    if (typeof span !== 'string' && span[0] === 'v') {
      const n = parseInt(span[1], 10)
      if (!Number.isNaN(n)) last = n
    }
  }
  return last
}

/**
 * Filter chapter nodes by verse range. Assumes range is always a subset (never full chapter).
 * When range is null, caller returns full chapter without calling this.
 */
function filterNodesByVerseRange(nodes: Node[], range: [string, string]): Node[] {
  const startVerse = parseInt(range[0], 10)
  const endVerse = parseInt(range[1], 10)
  if (Number.isNaN(startVerse) || Number.isNaN(endVerse)) return nodes

  const result: Node[] = []
  let currentVerse = 1

  for (let i = 0; i < nodes.length; i++) {
    const node = nodes[i]
    const type = node[0]

    if (type === 'b') {
      if (currentVerse >= startVerse && currentVerse <= endVerse) {
        result.push(node)
      }
      continue
    }

    if (isStructuralNode(type)) {
      const nextVerse = findNextVerse(nodes, i + 1, currentVerse)
      if (nextVerse >= startVerse && nextVerse <= endVerse) {
        result.push(node)
      }
      continue
    }

    if (type === 'table') {
      if (currentVerse >= startVerse && currentVerse <= endVerse) {
        result.push(node)
      }
      continue
    }

    const children = getNodeChildren(node)
    if (children.length > 0) {
      const [filteredSpans, lastVerse] = filterSpansByRange(
        children,
        startVerse,
        endVerse,
        currentVerse,
      )
      if (filteredSpans.length > 0) {
        result.push([type, filteredSpans] as Node)
      }
      // Update currentVerse from verse markers in this node even when we skip it
      if (lastVerse !== null) currentVerse = lastVerse
      else {
        const lastVerseInSpans = getLastVerseFromSpans(children)
        if (lastVerseInSpans !== null) currentVerse = lastVerseInSpans
      }
    }
  }

  return result
}

export async function getPassage(passageId: PassageId): Promise<Passage> {
  const row = getPassageQuery.get({
    chapterId: passageId.chapter,
    translationId: passageId.translation,
    bookId: passageId.book,
  })
  if (!row) {
    notFound()
  }

  const rawNodes: Node[] = JSON.parse(row.data)
  const chapterNode: Node = ['c', passageId.chapter]

  const nodes: Node[] =
    passageId.range != null
      ? [chapterNode, ...filterNodesByVerseRange(rawNodes, passageId.range)]
      : [chapterNode, ...rawNodes]

  return {
    bookAbbreviation: row.bookAbbreviation,
    bookTitle: row.bookTitle,
    id: buildPassageId(passageId),
    nodes,
    range: passageId.range ?? null,
  }
}
