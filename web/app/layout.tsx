import './globals.css'
import { GoogleAnalytics } from '@next/third-parties/google'
import type { Metadata } from 'next'
import { Rubik as Sans } from 'next/font/google'
import { Navbar } from '@/app/components/Navbar'
import { themeEffect } from '@/app/lib/themeEffect'

const fontSans = Sans({
  subsets: ['latin'],
  variable: '--font-sans',
  weight: ['400', '700'],
})

export const metadata: Metadata = {
  description: 'Read the Bible',
  title: 'Versly',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html className="dark:text-gray-50 dark:bg-gray-950" lang="en" suppressHydrationWarning>
      <head>
        <script dangerouslySetInnerHTML={{ __html: `(${themeEffect.toString()})();` }} />
        <link href="/manifest.json" rel="manifest" />
        <link href="/versly.svg" rel="icon" type="image/svg+xml" />
      </head>

      <body className={`${fontSans.variable} font-sans antialiased`}>
        <Navbar />
        {children}
      </body>

      {process.env.NEXT_PUBLIC_GA_ID ? (
        <GoogleAnalytics gaId={process.env.NEXT_PUBLIC_GA_ID} />
      ) : null}
    </html>
  )
}
