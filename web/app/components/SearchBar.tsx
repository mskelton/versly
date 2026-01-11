'use client'

import { useRouter } from 'next/navigation'
import { useEffect, useRef, useState } from 'react'
import { Search, X } from 'react-feather'
import { useIsMobile } from '@/app/hooks/useIsMobile'
import { parsePassageQuery } from '@/app/lib/bookInfo'
import { buildPassageId } from '@/app/lib/passageId'

export function SearchBar() {
  const [query, setQuery] = useState('')
  const router = useRouter()
  const isMobile = useIsMobile()
  const inputRef = useRef<HTMLInputElement>(null)

  const handleSearch = (searchQuery: string) => {
    if (!searchQuery.trim()) {
      return
    }

    const parsed = parsePassageQuery(searchQuery)
    if (parsed) {
      const href = `/${buildPassageId({
        book: parsed.book,
        chapter: parsed.chapter,
        translation: 'ESV',
        range: parsed.verses,
      })}`

      router.push(href)
      setQuery('')
    }
  }

  const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    handleSearch(query)
  }

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleSearch(query)
    }
  }

  const clearSearch = () => {
    setQuery('')
  }

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        inputRef.current?.focus()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  return (
    <form className="relative w-full" onSubmit={handleSubmit}>
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="h-5 w-5 text-gray-400" />
        </div>

        <input
          ref={inputRef}
          className="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-neutral-900 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder={isMobile ? 'Search' : 'Search for a passage...'}
          suppressHydrationWarning
          type="text"
          value={query}
        />

        {query && (
          <button
            className="absolute inset-y-0 right-0 pr-3 flex items-center"
            onClick={clearSearch}
            type="button"
          >
            <X className="h-5 w-5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300" />
          </button>
        )}
      </div>
    </form>
  )
}
