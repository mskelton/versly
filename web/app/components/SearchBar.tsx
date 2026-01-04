'use client'

import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { Search, X } from 'react-feather'
import { getBookIdFromTitle } from '@/app/lib/bookInfo'
import { buildChapterId } from '@/app/lib/passageId'
import type { SearchResult } from '@/app/lib/search'

export function SearchBar() {
  const [query, setQuery] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const router = useRouter()

  const handleSearch = async (searchQuery: string) => {
    if (!searchQuery.trim()) {
      return
    }

    setIsLoading(true)
    try {
      const response = await fetch(`/api/search?q=${encodeURIComponent(searchQuery)}`)
      const data = await response.json()
      const results: SearchResult[] = data.results || []

      if (results.length > 0) {
        const firstResult = results[0]
        const bookId = getBookIdFromTitle(firstResult.book)
        if (bookId) {
          const href = `/${buildChapterId({
            book: bookId,
            chapter: firstResult.chapter,
            translation: firstResult.translation_id,
            verses: null,
          })}`
          router.push(href)
          setQuery('')
        }
      }
    } catch (error) {
      console.error('Search error:', error)
    } finally {
      setIsLoading(false)
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

  return (
    <form className="relative w-full" onSubmit={handleSubmit}>
      <div className="relative">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Search className="h-5 w-5 text-gray-400" />
        </div>

        <input
          className="block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-gray-100 placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:opacity-50"
          disabled={isLoading}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Search for a passage..."
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
