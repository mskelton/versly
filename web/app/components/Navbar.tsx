'use client'

import clsx from 'clsx'
import Image from 'next/image'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import { SearchBar } from './SearchBar'

export function Navbar() {
  const pathname = usePathname()
  const isPlan = pathname.startsWith('/plan')
  const isRead = !isPlan

  return (
    <nav className="sticky top-0 z-50 bg-white dark:bg-neutral-950 border-b border-gray-200 dark:border-gray-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-2 md:gap-4">
            <Link
              className="flex items-center gap-2 text-xl font-bold text-gray-900 dark:text-gray-100"
              href="/"
            >
              <Image alt="Versly" height={24} src="/versly.svg" width={24} />
              <span className="hidden md:block">Versly</span>
              <span className="sr-only md:hidden">Versly</span>
            </Link>

            <div className="flex items-center space-x-1">
              <NavLink href="/" isActive={isRead}>
                Read
              </NavLink>

              <NavLink href="/plan" isActive={isPlan}>
                Plan
              </NavLink>
            </div>
          </div>

          <div className="flex-1 max-w-lg ml-6">
            <SearchBar />
          </div>
        </div>
      </div>
    </nav>
  )
}

function NavLink({
  children,
  href,
  isActive,
}: {
  children: React.ReactNode
  href: string
  isActive: boolean
}) {
  return (
    <Link
      className={clsx(
        'px-4 py-2 rounded-md text-sm font-medium transition-colors',
        isActive
          ? 'bg-neutral-100 dark:bg-neutral-800 text-gray-900 dark:text-gray-100'
          : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-neutral-50 dark:hover:bg-neutral-900',
      )}
      href={href}
    >
      {children}
    </Link>
  )
}
