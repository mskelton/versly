import { useSyncExternalStore } from 'react'

const MOBILE_QUERY = '(max-width: 768px)'

export type ScreenSize = 'desktop' | 'mobile'

// Global variable name for SSR hydration
declare global {
  interface Window {
    __SCREEN_SIZE_SSR__?: ScreenSize
  }
}

const isMobileServerFn = () => {
  // return getCookie('screen-size') === 'mobile'
  return false
}

function getServerSnapshot(): boolean {
  // Client hydration: read from global variable set by server
  // This ensures the value matches what was rendered on the server
  if (typeof window !== 'undefined') {
    return window.__SCREEN_SIZE_SSR__ === 'mobile'
  }

  return isMobileServerFn()
}

const getClientSnapshot = () => {
  return window.matchMedia(MOBILE_QUERY).matches
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
  return useSyncExternalStore(subscribe, getClientSnapshot, getServerSnapshot)
}
