import { useSyncExternalStore } from 'react'
import { ScreenSize, useScreenSizeContext } from '@/app/components/ScreenSizeProvider'

const MOBILE_QUERY = '(max-width: 768px)'

// Global variable name for SSR hydration
declare global {
  interface Window {
    __SCREEN_SIZE_SSR__?: ScreenSize
  }
}

function subscribe(onStoreChange: () => void): () => void {
  if (typeof window === 'undefined') {
    return () => {}
  }

  const mediaQuery = window.matchMedia(MOBILE_QUERY)

  mediaQuery.addEventListener('change', onStoreChange)
  return () => mediaQuery.removeEventListener('change', onStoreChange)
}

export function useIsMobile(): boolean {
  const ctx = useScreenSizeContext()

  return useSyncExternalStore(
    subscribe,
    () => {
      return window.matchMedia(MOBILE_QUERY).matches
    },
    () => {
      if (typeof window !== 'undefined') {
        return window.__SCREEN_SIZE_SSR__ === 'mobile'
      }

      return ctx === 'mobile'
    },
  )
}
