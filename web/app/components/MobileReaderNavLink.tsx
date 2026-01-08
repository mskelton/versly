import Link from 'next/link'
import { cloneElement } from 'react'

export function MobileReaderNavLink({
  direction,
  href,
  icon,
  label,
}: {
  direction: 'left' | 'right'
  href: string
  icon: React.ReactElement<{ className: string }>
  label: string
}) {
  return (
    <Link
      className="flex items-center gap-2 px-4 py-2 text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-gray-100 hover:bg-gray-100 dark:hover:bg-neutral-800 rounded-lg transition-colors"
      href={href}
    >
      {direction === 'left' ? cloneElement(icon, { className: 'size-5' }) : null}
      <span className="text-sm font-medium">{label}</span>
      {direction === 'right' ? cloneElement(icon, { className: 'size-5' }) : null}
    </Link>
  )
}
