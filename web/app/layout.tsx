import './globals.css'
import type { Metadata } from 'next'
import { Rubik as Sans } from 'next/font/google'

const fontSans = Sans({
	subsets: ['latin'],
	variable: '--font-sans',
	weight: ['400', '700'],
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
		<html className='dark:text-gray-50 dark:bg-gray-950' lang='en'>
			<head>
				<link href='/versly.svg' rel='icon' type='image/svg+xml' />
			</head>

			<body className={`${fontSans.variable} font-sans antialiased`}>
				{children}
			</body>
		</html>
	)
}
