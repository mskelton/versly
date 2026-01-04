'use client'

import clsx from 'clsx'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { SearchBar } from './SearchBar'

export function Navbar() {
  const pathname = usePathname()
  // Check if we're on a passage page (format: /BOOK.CHAPTER.TRANSLATION)
  const isPassagePage = pathname.match(/^\/[A-Z]{3}\.\d+\.?[A-Z]*$/)
  const isRead = pathname === '/' || isPassagePage
  const isPlan = pathname === '/plan' || pathname.startsWith('/plan/')

  return (
    <nav className="sticky top-0 z-50 bg-white dark:bg-gray-950 border-b border-gray-200 dark:border-gray-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center space-x-8">
            <Link className="text-xl font-bold text-gray-900 dark:text-gray-100" href="/">
              Versly
            </Link>

            <div className="flex items-center space-x-1">
              <Link
                className={clsx(
                  'px-3 py-2 rounded-md text-sm font-medium transition-colors',
                  isRead
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
                    : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-gray-50 dark:hover:bg-gray-900',
                )}
                href="/"
              >
                Read
              </Link>
              <Link
                className={clsx(
                  'px-3 py-2 rounded-md text-sm font-medium transition-colors',
                  isPlan
                    ? 'bg-gray-100 dark:bg-gray-800 text-gray-900 dark:text-gray-100'
                    : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-gray-50 dark:hover:bg-gray-900',
                )}
                href="/plan"
              >
                Plan
              </Link>
            </div>
          </div>

          <div className="flex-1 max-w-lg mx-4">
            <SearchBar />
          </div>
        </div>
      </div>
    </nav>
  )
}
