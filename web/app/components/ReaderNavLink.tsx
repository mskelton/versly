import clsx from 'clsx'
import Link from 'next/link'
import { cloneElement } from 'react'

export function ReaderNavLink({
  href,
  icon,
  label,
  side,
}: {
  href: string
  icon: React.ReactElement<{ className: string }>
  label: string
  side: 'left' | 'right'
}) {
  return (
    <Link
      className={clsx(
        'h-[calc(100vh-var(--header-height)-2rem)] sticky top-[calc(var(--header-height)+1rem)] hover:bg-gray-200 dark:hover:bg-gray-900 rounded-lg p-2 items-center justify-center px-8 py-2 text-gray-500 hover:text-gray-200 transition-colors hidden md:flex',
        side === 'left' ? 'left-4' : 'right-4',
      )}
      href={href}
    >
      <span className="sr-only">{label}</span>
      {cloneElement(icon, { className: 'size-12' })}
    </Link>
  )
}
