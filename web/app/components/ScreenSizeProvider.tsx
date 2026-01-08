'use client'

import { createContext, useContext } from 'react'

export type ScreenSize = 'desktop' | 'mobile'

const ScreenSizeContext = createContext<ScreenSize | null>(null)

export function ScreenSizeProvider({
  children,
  initialScreenSize,
}: {
  children: React.ReactNode
  initialScreenSize: ScreenSize
}) {
  return (
    <ScreenSizeContext.Provider value={initialScreenSize}>{children}</ScreenSizeContext.Provider>
  )
}

export function useScreenSizeContext(): ScreenSize | null {
  return useContext(ScreenSizeContext)
}
