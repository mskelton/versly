import './globals.css'
import type { Metadata } from 'next'
import { Geist, Geist_Mono } from 'next/font/google'

const geistSans = Geist({
	subsets: ['latin'],
	variable: '--font-geist-sans',
})

const geistMono = Geist_Mono({
	subsets: ['latin'],
	variable: '--font-geist-mono',
})

export const metadata: Metadata = {
	description: 'Read the Bible, every day, one chapter each day',
	title: 'Versly',
}

export default function RootLayout({
	children,
}: Readonly<{
	children: React.ReactNode
}>) {
	return (
		<html className="dark:text-gray-50 dark:bg-gray-950" lang="en">
			<head>
				<link href="/versly.svg" rel="icon" type="image/svg+xml" />
			</head>

			<body
				className={`${geistSans.variable} ${geistMono.variable} font-sans antialiased`}
			>
				{children}
			</body>
		</html>
	)
}
