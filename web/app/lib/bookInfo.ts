import { buildPassageId, PassageId } from './passageId'

const bookAliases: Record<string, string> = {
  // Genesis
  'gen': 'GEN',
  'ge': 'GEN',
  'gn': 'GEN',
  'genesis': 'GEN',
  'genisis': 'GEN',
  'genises': 'GEN',
  'genessis': 'GEN',
  '1st genesis': 'GEN',
  'first genesis': 'GEN',
  'i genesis': 'GEN',
  '1 genesis': 'GEN',

  // Exodus
  'exo': 'EXO',
  'ex': 'EXO',
  'exod': 'EXO',
  'exodus': 'EXO',
  'exodous': 'EXO',
  'exodis': 'EXO',
  '2nd exodus': 'EXO',
  'second exodus': 'EXO',
  'ii exodus': 'EXO',
  '2 exodus': 'EXO',

  // Leviticus
  'lev': 'LEV',
  'lv': 'LEV',
  'leviticus': 'LEV',
  'levitucus': 'LEV',
  'levitcus': 'LEV',
  'levit': 'LEV',
  '3rd leviticus': 'LEV',
  'third leviticus': 'LEV',
  'iii leviticus': 'LEV',
  '3 leviticus': 'LEV',

  // Numbers
  'num': 'NUM',
  'nu': 'NUM',
  'nm': 'NUM',
  'numbers': 'NUM',
  'number': 'NUM',
  'numb': 'NUM',
  '4th numbers': 'NUM',
  'fourth numbers': 'NUM',
  'iv numbers': 'NUM',
  '4 numbers': 'NUM',

  // Deuteronomy
  'deu': 'DEU',
  'dt': 'DEU',
  'deut': 'DEU',
  'deuteronomy': 'DEU',
  'deuteronomi': 'DEU',
  'deuteronmy': 'DEU',
  'deut': 'DEU',
  '5th deuteronomy': 'DEU',
  'fifth deuteronomy': 'DEU',
  'v deuteronomy': 'DEU',
  '5 deuteronomy': 'DEU',

  // Joshua
  'jos': 'JOS',
  'josh': 'JOS',
  'jsh': 'JOS',
  'joshua': 'JOS',
  'joshua': 'JOS',
  'josh': 'JOS',

  // Judges
  'jdg': 'JDG',
  'judg': 'JDG',
  'jud': 'JDG',
  'judges': 'JDG',
  'judge': 'JDG',
  'judgs': 'JDG',

  // Ruth
  'rut': 'RUT',
  'ru': 'RUT',
  'ruth': 'RUT',
  'rth': 'RUT',

  // 1 Samuel
  '1sa': '1SA',
  '1 sam': '1SA',
  '1 samuel': '1SA',
  '1st samuel': '1SA',
  'first samuel': '1SA',
  'i samuel': '1SA',
  '1 sam': '1SA',

  // 2 Samuel
  '2sa': '2SA',
  '2 sam': '2SA',
  '2 samuel': '2SA',
  '2nd samuel': '2SA',
  'second samuel': '2SA',
  'ii samuel': '2SA',
  '2 sam': '2SA',

  // 1 Kings
  '1ki': '1KI',
  '1 kg': '1KI',
  '1 kgs': '1KI',
  '1 kings': '1KI',
  '1st kings': '1KI',
  'first kings': '1KI',
  'i kings': '1KI',
  '1 kng': '1KI',
  '1 king': '1KI',

  // 2 Kings
  '2ki': '2KI',
  '2 kg': '2KI',
  '2 kgs': '2KI',
  '2 kings': '2KI',
  '2nd kings': '2KI',
  'second kings': '2KI',
  'ii kings': '2KI',
  '2 kng': '2KI',
  '2 king': '2KI',

  // 1 Chronicles
  '1ch': '1CH',
  '1 chr': '1CH',
  '1 chron': '1CH',
  '1 chronicles': '1CH',
  '1st chronicles': '1CH',
  'first chronicles': '1CH',
  'i chronicles': '1CH',
  '1 chron': '1CH',

  // 2 Chronicles
  '2ch': '2CH',
  '2 chr': '2CH',
  '2 chron': '2CH',
  '2 chronicles': '2CH',
  '2nd chronicles': '2CH',
  'second chronicles': '2CH',
  'ii chronicles': '2CH',
  '2 chron': '2CH',

  // Ezra
  'ezr': 'EZR',
  'ez': 'EZR',
  'ezra': 'EZR',
  'ezr': 'EZR',

  // Nehemiah
  'neh': 'NEH',
  'ne': 'NEH',
  'nehemiah': 'NEH',
  'nehemia': 'NEH',
  'nehem': 'NEH',

  // Esther
  'est': 'EST',
  'es': 'EST',
  'esth': 'EST',
  'esther': 'EST',
  'ester': 'EST',
  'est': 'EST',

  // Job
  'job': 'JOB',
  'jb': 'JOB',
  'job': 'JOB',

  // Psalms
  'psa': 'PSA',
  'ps': 'PSA',
  'psalm': 'PSA',
  'psalms': 'PSA',
  'pslm': 'PSA',
  'pslms': 'PSA',
  'ps': 'PSA',

  // Proverbs
  'pro': 'PRO',
  'pr': 'PRO',
  'prov': 'PRO',
  'proverbs': 'PRO',
  'proverb': 'PRO',
  'provs': 'PRO',

  // Ecclesiastes
  'ecc': 'ECC',
  'ec': 'ECC',
  'eccl': 'ECC',
  'ecclesiastes': 'ECC',
  'eccles': 'ECC',
  'eccle': 'ECC',
  'eccl': 'ECC',

  // Song of Songs
  'sng': 'SNG',
  'so': 'SNG',
  'song': 'SNG',
  'song of songs': 'SNG',
  'song of solomon': 'SNG',
  'songs': 'SNG',
  'sos': 'SNG',
  'canticles': 'SNG',
  'cant': 'SNG',

  // Isaiah
  'isa': 'ISA',
  'is': 'ISA',
  'isaiah': 'ISA',
  'isaia': 'ISA',
  'isai': 'ISA',

  // Jeremiah
  'jer': 'JER',
  'je': 'JER',
  'jr': 'JER',
  'jeremiah': 'JER',
  'jeremia': 'JER',
  'jerem': 'JER',

  // Lamentations
  'lam': 'LAM',
  'la': 'LAM',
  'lamentations': 'LAM',
  'lamentation': 'LAM',
  'lament': 'LAM',

  // Ezekiel
  'ezk': 'EZK',
  'ez': 'EZK',
  'ezek': 'EZK',
  'ezekiel': 'EZK',
  'ezekial': 'EZK',
  'ezek': 'EZK',

  // Daniel
  'dan': 'DAN',
  'da': 'DAN',
  'dn': 'DAN',
  'daniel': 'DAN',
  'danial': 'DAN',
  'danie': 'DAN',

  // Hosea
  'hos': 'HOS',
  'ho': 'HOS',
  'hosea': 'HOS',
  'hose': 'HOS',

  // Joel
  'jol': 'JOL',
  'jo': 'JOL',
  'jl': 'JOL',
  'joel': 'JOL',
  'joe': 'JOL',

  // Amos
  'amo': 'AMO',
  'am': 'AMO',
  'amos': 'AMO',
  'amoz': 'AMO',

  // Obadiah
  'oba': 'OBA',
  'ob': 'OBA',
  'obad': 'OBA',
  'obadiah': 'OBA',
  'obadia': 'OBA',
  'obad': 'OBA',

  // Jonah
  'jon': 'JON',
  'jnh': 'JON',
  'jonah': 'JON',
  'jona': 'JON',
  'jonas': 'JON',

  // Micah
  'mic': 'MIC',
  'mi': 'MIC',
  'micah': 'MIC',
  'mica': 'MIC',
  'mich': 'MIC',

  // Nahum
  'nam': 'NAM',
  'na': 'NAM',
  'nah': 'NAM',
  'nahum': 'NAM',
  'nahu': 'NAM',

  // Habakkuk
  'hab': 'HAB',
  'hb': 'HAB',
  'habakkuk': 'HAB',
  'habakuk': 'HAB',
  'habacuc': 'HAB',
  'hab': 'HAB',

  // Zephaniah
  'zep': 'ZEP',
  'zp': 'ZEP',
  'zeph': 'ZEP',
  'zephaniah': 'ZEP',
  'zephania': 'ZEP',
  'zeph': 'ZEP',

  // Haggai
  'hag': 'HAG',
  'hg': 'HAG',
  'haggai': 'HAG',
  'hagai': 'HAG',
  'hag': 'HAG',

  // Zechariah
  'zec': 'ZEC',
  'zc': 'ZEC',
  'zech': 'ZEC',
  'zechariah': 'ZEC',
  'zecharia': 'ZEC',
  'zech': 'ZEC',

  // Malachi
  'mal': 'MAL',
  'ml': 'MAL',
  'malachi': 'MAL',
  'malachai': 'MAL',
  'mal': 'MAL',

  // Matthew
  'mat': 'MAT',
  'mt': 'MAT',
  'matt': 'MAT',
  'matthew': 'MAT',
  'mathew': 'MAT',
  'matthe': 'MAT',

  // Mark
  'mrk': 'MRK',
  'mk': 'MRK',
  'mr': 'MRK',
  'mark': 'MRK',
  'mar': 'MRK',

  // Luke
  'luk': 'LUK',
  'lk': 'LUK',
  'lu': 'LUK',
  'luke': 'LUK',
  'luk': 'LUK',

  // John
  'jhn': 'JHN',
  'jn': 'JHN',
  'jo': 'JHN',
  'john': 'JHN',
  'joh': 'JHN',
  'jhn': 'JHN',

  // Acts
  'act': 'ACT',
  'ac': 'ACT',
  'acts': 'ACT',
  'act': 'ACT',

  // Romans
  'rom': 'ROM',
  'ro': 'ROM',
  'rm': 'ROM',
  'romans': 'ROM',
  'roman': 'ROM',
  'roms': 'ROM',

  // 1 Corinthians
  '1co': '1CO',
  '1 cor': '1CO',
  '1 corinthians': '1CO',
  '1st corinthians': '1CO',
  'first corinthians': '1CO',
  'i corinthians': '1CO',
  '1 cor': '1CO',
  '1 corin': '1CO',

  // 2 Corinthians
  '2co': '2CO',
  '2 cor': '2CO',
  '2 corinthians': '2CO',
  '2nd corinthians': '2CO',
  'second corinthians': '2CO',
  'ii corinthians': '2CO',
  '2 cor': '2CO',
  '2 corin': '2CO',

  // Galatians
  'gal': 'GAL',
  'ga': 'GAL',
  'galatians': 'GAL',
  'galatian': 'GAL',
  'gal': 'GAL',

  // Ephesians
  'eph': 'EPH',
  'ep': 'EPH',
  'ephesians': 'EPH',
  'ephesian': 'EPH',
  'ephes': 'EPH',
  'eph': 'EPH',

  // Philippians
  'php': 'PHP',
  'ph': 'PHP',
  'phil': 'PHP',
  'philippians': 'PHP',
  'philipians': 'PHP',
  'philippian': 'PHP',
  'phil': 'PHP',

  // Colossians
  'col': 'COL',
  'co': 'COL',
  'colossians': 'COL',
  'colossian': 'COL',
  'colos': 'COL',
  'col': 'COL',

  // 1 Thessalonians
  '1th': '1TH',
  '1 thes': '1TH',
  '1 thess': '1TH',
  '1 thessalonians': '1TH',
  '1st thessalonians': '1TH',
  'first thessalonians': '1TH',
  'i thessalonians': '1TH',
  '1 thes': '1TH',
  '1 thess': '1TH',

  // 2 Thessalonians
  '2th': '2TH',
  '2 thes': '2TH',
  '2 thess': '2TH',
  '2 thessalonians': '2TH',
  '2nd thessalonians': '2TH',
  'second thessalonians': '2TH',
  'ii thessalonians': '2TH',
  '2 thes': '2TH',
  '2 thess': '2TH',

  // 1 Timothy
  '1ti': '1TI',
  '1 tim': '1TI',
  '1 timothy': '1TI',
  '1st timothy': '1TI',
  'first timothy': '1TI',
  'i timothy': '1TI',
  '1 tim': '1TI',

  // 2 Timothy
  '2ti': '2TI',
  '2 tim': '2TI',
  '2 timothy': '2TI',
  '2nd timothy': '2TI',
  'second timothy': '2TI',
  'ii timothy': '2TI',
  '2 tim': '2TI',

  // Titus
  'tit': 'TIT',
  'ti': 'TIT',
  'tt': 'TIT',
  'titus': 'TIT',
  'titu': 'TIT',

  // Philemon
  'phm': 'PHM',
  'ph': 'PHM',
  'phlm': 'PHM',
  'philemon': 'PHM',
  'philem': 'PHM',
  'phlm': 'PHM',

  // Hebrews
  'heb': 'HEB',
  'he': 'HEB',
  'hb': 'HEB',
  'hebrews': 'HEB',
  'hebrew': 'HEB',
  'hebr': 'HEB',

  // James
  'jas': 'JAS',
  'ja': 'JAS',
  'jm': 'JAS',
  'james': 'JAS',
  'jame': 'JAS',
  'jam': 'JAS',

  // 1 Peter
  '1pe': '1PE',
  '1 pet': '1PE',
  '1 peter': '1PE',
  '1st peter': '1PE',
  'first peter': '1PE',
  'i peter': '1PE',
  '1 pet': '1PE',
  '1 pt': '1PE',

  // 2 Peter
  '2pe': '2PE',
  '2 pet': '2PE',
  '2 peter': '2PE',
  '2nd peter': '2PE',
  'second peter': '2PE',
  'ii peter': '2PE',
  '2 pet': '2PE',
  '2 pt': '2PE',

  // 1 John
  '1jn': '1JN',
  '1 jhn': '1JN',
  '1 john': '1JN',
  '1st john': '1JN',
  'first john': '1JN',
  'i john': '1JN',
  '1 jhn': '1JN',
  '1 jn': '1JN',

  // 2 John
  '2jn': '2JN',
  '2 jhn': '2JN',
  '2 john': '2JN',
  '2nd john': '2JN',
  'second john': '2JN',
  'ii john': '2JN',
  '2 jhn': '2JN',
  '2 jn': '2JN',

  // 3 John
  '3jn': '3JN',
  '3 jhn': '3JN',
  '3 john': '3JN',
  '3rd john': '3JN',
  'third john': '3JN',
  'iii john': '3JN',
  '3 jhn': '3JN',
  '3 jn': '3JN',

  // Jude
  'jud': 'JUD',
  'ju': 'JUD',
  'jude': 'JUD',
  'jud': 'JUD',

  // Revelation
  'rev': 'REV',
  're': 'REV',
  'rv': 'REV',
  'revelation': 'REV',
  'revelations': 'REV', // Common misspelling
  'revel': 'REV',
  'rev': 'REV',
  'apocalypse': 'REV',
  'apoc': 'REV',
}

const bookInfo: [bookId: string, title: string, abbreviation: string, chapters: number][] = [
  ['GEN', 'Genesis', 'Gen', 50],
  ['EXO', 'Exodus', 'Exod', 40],
  ['LEV', 'Leviticus', 'Lev', 27],
  ['NUM', 'Numbers', 'Num', 36],
  ['DEU', 'Deuteronomy', 'Deut', 34],
  ['JOS', 'Joshua', 'Josh', 24],
  ['JDG', 'Judges', 'Judg', 21],
  ['RUT', 'Ruth', 'Ruth', 4],
  ['1SA', '1 Samuel', '1 Sam', 31],
  ['2SA', '2 Samuel', '2 Sam', 24],
  ['1KI', '1 Kings', '1 Kng', 22],
  ['2KI', '2 Kings', '2 Kng', 25],
  ['1CH', '1 Chronicles', '1 Chr', 29],
  ['2CH', '2 Chronicles', '2 Chr', 36],
  ['EZR', 'Ezra', 'Ezra', 10],
  ['NEH', 'Nehemiah', 'Neh', 13],
  ['EST', 'Esther', 'Esth', 10],
  ['JOB', 'Job', 'Job', 42],
  ['PSA', 'Psalms', 'Psa', 150],
  ['PRO', 'Proverbs', 'Prov', 31],
  ['ECC', 'Ecclesiastes', 'Eccl', 12],
  ['SNG', 'Song of Songs', 'Song', 8],
  ['ISA', 'Isaiah', 'Isa', 66],
  ['JER', 'Jeremiah', 'Jer', 52],
  ['LAM', 'Lamentations', 'Lam', 5],
  ['EZK', 'Ezekiel', 'Ezek', 48],
  ['DAN', 'Daniel', 'Dan', 12],
  ['HOS', 'Hosea', 'Hos', 14],
  ['JOL', 'Joel', 'Joel', 3],
  ['AMO', 'Amos', 'Amos', 9],
  ['OBA', 'Obadiah', 'Obad', 1],
  ['JON', 'Jonah', 'Jonah', 4],
  ['MIC', 'Micah', 'Mic', 7],
  ['NAM', 'Nahum', 'Nah', 3],
  ['HAB', 'Habakkuk', 'Hab', 3],
  ['ZEP', 'Zephaniah', 'Zeph', 3],
  ['HAG', 'Haggai', 'Hag', 2],
  ['ZEC', 'Zechariah', 'Zech', 14],
  ['MAL', 'Malachi', 'Mal', 4],
  ['MAT', 'Matthew', 'Matt', 28],
  ['MRK', 'Mark', 'Mark', 16],
  ['LUK', 'Luke', 'Luke', 24],
  ['JHN', 'John', 'John', 21],
  ['ACT', 'Acts', 'Acts', 28],
  ['ROM', 'Romans', 'Rom', 16],
  ['1CO', '1 Corinthians', '1 Cor', 16],
  ['2CO', '2 Corinthians', '2 Cor', 13],
  ['GAL', 'Galatians', 'Gal', 6],
  ['EPH', 'Ephesians', 'Eph', 6],
  ['PHP', 'Philippians', 'Phil', 4],
  ['COL', 'Colossians', 'Col', 4],
  ['1TH', '1 Thessalonians', '1 Th', 5],
  ['2TH', '2 Thessalonians', '2 Th', 3],
  ['1TI', '1 Timothy', '1 Tim', 6],
  ['2TI', '2 Timothy', '2 Tim', 4],
  ['TIT', 'Titus', 'Titus', 3],
  ['PHM', 'Philemon', 'Phlm', 1],
  ['HEB', 'Hebrews', 'Heb', 13],
  ['JAS', 'James', 'James', 5],
  ['1PE', '1 Peter', '1 Pet', 5],
  ['2PE', '2 Peter', '2 Pet', 3],
  ['1JN', '1 John', '1 Jhn', 5],
  ['2JN', '2 John', '2 Jhn', 1],
  ['3JN', '3 John', '3 Jhn', 1],
  ['JUD', 'Jude', 'Jude', 1],
  ['REV', 'Revelation', 'Rev', 22],
]

export function getPreviousChapter(passageId: PassageId) {
  return getChapter(passageId, -1)
}

export function getNextChapter(passageId: PassageId) {
  return getChapter(passageId, 1)
}

function getChapter(passageId: PassageId, direction: -1 | 1) {
  const bookIndex = bookInfo.findIndex(([id]) => id === passageId.book)
  const totalChapters = bookInfo[bookIndex][3]

  let bookId = passageId.book
  let chapterId = parseInt(passageId.chapter) + direction

  if (direction === -1 && chapterId < 1) {
    bookId = bookInfo[bookIndex - 1]?.[0]
    chapterId = bookInfo[bookIndex - 1]?.[3]

    if (!bookId || !chapterId) {
      return null
    }
  } else if (direction === 1 && chapterId > totalChapters) {
    bookId = bookInfo[bookIndex + 1]?.[0]
    chapterId = 1

    if (!bookId) {
      return null
    }
  }

  return `/${buildPassageId({
    book: bookId,
    chapter: chapterId.toString(),
    translation: passageId.translation,
    range: null,
  })}`
}

export function getPassageName(passageId: PassageId) {
  const bookName = bookInfo.find(([Id]) => Id === passageId.book)![1]

  if (passageId.range) {
    const [start, end] = passageId.range
    const versePart = start === end ? start : `${start}-${end}`
    return `${bookName} ${passageId.chapter}:${versePart} ${passageId.translation}`
  }

  return `${bookName} ${passageId.chapter} ${passageId.translation}`
}

export function getBookIdFromTitle(title: string): string | null {
  const book = bookInfo.find(([, bookTitle]) => bookTitle === title)
  return book ? book[0] : null
}

export function getBookTitle(book: string) {
  return bookInfo.find(([id]) => id === book)?.[1] ?? book
}

export function parsePassageQuery(
  query: string,
): { book: string; chapter: string; verses: [string, string] | null } | null {
  const normalized = query.trim().toLowerCase()

  // Try to match: "book chapter:verse" or "book chapter:verse-verse" or "book chapter" or "book:chapter" or "book.chapter"
  // Examples: "Romans 15:13", "Romans 15:13-15", "Romans 15", "Romans 15.13"
  const verseMatch = normalized.match(/^(.+?)[\s:.](\d+):(\d+)(?:-(\d+))?$/)
  const chapterMatch = normalized.match(/^(.+?)[\s:.](\d+)$/)

  let bookName: string
  let chapter: string
  let verses: [string, string] | null = null

  if (verseMatch) {
    // Has verse reference: "book chapter:verse" or "book chapter:verse-verse"
    const [, bookPart, chapterPart, verseStart, verseEnd] = verseMatch
    bookName = bookPart.trim()
    chapter = chapterPart
    verses = [verseStart, verseEnd ?? verseStart]
  } else if (chapterMatch) {
    // Has chapter number: "book chapter" or "book:chapter" or "book.chapter"
    const [, bookPart, chapterPart] = chapterMatch
    bookName = bookPart.trim()
    chapter = chapterPart
  } else {
    // No chapter number, try to match just the book name
    bookName = normalized
    chapter = '1' // Default to chapter 1
  }

  // Try to find book by:
  // 1. Alias map lookup (includes abbreviations, misspellings, alternate forms)
  // 2. Exact ID match (e.g., "JHN")
  // 3. Full title match (e.g., "John", "Genesis")
  // 4. Abbreviation match (e.g., "Gen", "Matt")
  // 5. Partial title match (e.g., "1 cor" -> "1 Corinthians")

  // First check the alias map
  const aliasMatch = bookAliases[bookName]
  let book = aliasMatch ? bookInfo.find(([id]) => id === aliasMatch) : null

  // If not found in alias map, try the existing matching logic
  if (!book) {
    book = bookInfo.find(([id, title, abbreviation]) => {
      const idLower = id.toLowerCase()
      const titleLower = title.toLowerCase()
      const abbrevLower = abbreviation.toLowerCase()

      return (
        idLower === bookName ||
        titleLower === bookName ||
        abbrevLower === bookName ||
        titleLower.startsWith(bookName) ||
        abbrevLower.startsWith(bookName)
      )
    })
  }

  if (!book) {
    return null
  }

  // Validate chapter number is within valid range
  const chapterNum = parseInt(chapter, 10)
  const [, , , maxChapters] = book
  if (chapterNum < 1 || chapterNum > maxChapters) {
    return null
  }

  return {
    book: book[0],
    chapter,
    verses,
  }
}
