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
				'fixed transform top-4 bottom-4 hover:bg-gray-200 dark:hover:bg-gray-800 rounded p-2 flex items-center justify-center px-8 py-2 text-gray-500 hover:text-gray-300 transition-colors duration-300',
				side === 'left' ? 'left-4' : 'right-4',
			)}
			href={href}
		>
			<span className="sr-only">{label}</span>
			{cloneElement(icon, { className: 'size-12' })}
		</Link>
	)
}
