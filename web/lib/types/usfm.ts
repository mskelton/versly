// Introductions
// https://ubsicap.github.io/usfm/introductions/index.html
export type Introduction = [type: 'iex', text: string]

// Titles, Headings, and Labels
// https://ubsicap.github.io/usfm/titles_headings/index.html
export type MajorSection = [type: 'ms', text: string]
export type Section = [type: 's1' | 's2' | 's3', text: string]
export type DescriptiveTitle = [type: 'd', children: ChildNode[]]
export type Speaker = [type: 'sp', text: string]

// Chapters and Verses
// https://ubsicap.github.io/usfm/chapters_verses/index.html
export type ChapterLabel = [type: 'cl', book: string, num: number]
export type VerseNumber = [type: 'v', text: string]

// Paragraphs
// https://ubsicap.github.io/usfm/paragraphs/index.html
export type Paragraph = [type: 'p', children: ChildNode[]]
export type ContinuationParagraph = [type: 'm', children: ChildNode[]]
export type ParagraphRightAligned = [type: 'pr', children: ChildNode[]]
export type Closure = [type: 'cls', children: ChildNode[]]
export type EmbeddedTextOpening = [type: 'pmo', children: ChildNode[]]
export type EmbeddedText = [type: 'pm', children: ChildNode[]]
export type EmbeddedTextClosing = [type: 'pmc', children: ChildNode[]]
export type EmbeddedTextRefrain = [type: 'pmr', children: ChildNode[]]
export type IndentedParagraph = [
	type: 'pi1' | 'pi2' | 'pi3',
	children: ChildNode[],
]
export type IndentedFlushLeftParagraph = [type: 'mi', children: ChildNode[]]
export type NoBreak = [type: 'nb', children: ChildNode[]]
export type ParagraphCentered = [type: 'pc', children: ChildNode[]]
export type BlankLine = [type: 'b']

// Poetry
// https://ubsicap.github.io/usfm/poetry/index.html
export type PoeticLine = [
	type: 'q1' | 'q2' | 'q3' | 'q4',
	children: ChildNode[],
]
export type PoeticLineRightAligned = [type: 'qr', children: ChildNode[]]
export type PoeticLineCentered = [type: 'qc', children: ChildNode[]]
export type Selah = [type: 'qs', text: string]
export type AcrosticHeading = [type: 'qa', text: string]
export type EmbeddedTextPoeticLine = [
	type: 'qm1' | 'qm2',
	children: ChildNode[],
]

// Lists
// https://ubsicap.github.io/usfm/lists/index.html
export type ListItem = [
	type: 'li1' | 'li2' | 'li3' | 'li4',
	children: ChildNode[],
]
export type EmbeddedListItem = [type: 'lim', children: ChildNode[]]
export type ListEntryTotal = [type: 'litl', text: string]

// Tables
// https://ubsicap.github.io/usfm/tables/index.html
export type Table = [type: 'table', rows: (TableHeading[] | TableCell[])[]]
export type TableHeading = [type: 'th', children: ChildNode[]]
export type TableCell = [type: 'td', children: ChildNode[]]

// Special Text
// https://ubsicap.github.io/usfm/characters/index.html#special-text
export type BookTitle = [type: 'bk', text: string]
export type NameOfGod = [type: 'nd', text: string]
export type QuotedText = [type: 'qt', text: string]
export type Signature = [type: 'sig', text: string]
export type SecondaryLanguageSource = [type: 'sls', text: string]
export type Transliteration = [type: 'tl', text: string]
export type WordsOfJesus = [type: 'wj', text: string]

// Character Styling
// https://ubsicap.github.io/usfm/characters/index.html#character-styling
export type EmphasisText = [type: 'em', text: string]
export type Bold = [type: 'bd', text: string]
export type Italic = [type: 'it', text: string]
export type SmallCap = [type: 'sc', text: string]
export type Superscript = [type: 'sup', text: string]

// Custom
export type Text = [type: 't', text: string]

export type Node =
	| Introduction
	| MajorSection
	| Section
	| DescriptiveTitle
	| Speaker
	| ChapterLabel
	| Paragraph
	| ContinuationParagraph
	| ParagraphRightAligned
	| Closure
	| EmbeddedTextOpening
	| EmbeddedText
	| EmbeddedTextClosing
	| EmbeddedTextRefrain
	| IndentedParagraph
	| IndentedFlushLeftParagraph
	| NoBreak
	| ParagraphCentered
	| BlankLine
	| PoeticLine
	| PoeticLineRightAligned
	| PoeticLineCentered
	| AcrosticHeading
	| EmbeddedTextPoeticLine
	| ListItem
	| EmbeddedListItem
	| Table

export type ChildNode =
	| VerseNumber
	| ListEntryTotal
	| BookTitle
	| Selah
	| NameOfGod
	| QuotedText
	| Signature
	| SecondaryLanguageSource
	| Transliteration
	| WordsOfJesus
	| EmphasisText
	| Bold
	| Italic
	| SmallCap
	| Superscript
	| Text

export type NodeType = Node[0] | ChildNode[0] | TableHeading[0] | TableCell[0]
