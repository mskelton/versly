'use client'

import { useEffect } from 'react'

export function SetScreenSize() {
  useEffect(() => {
    const mediaQuery = window.matchMedia('(max-width: 768px)')

    const updateScreenSize = () => {
      const screenSize = mediaQuery.matches ? 'mobile' : 'desktop'
      document.cookie = `screen-size=${screenSize}; path=/; max-age=31536000`
    }

    mediaQuery.addEventListener('change', updateScreenSize)
    updateScreenSize()

    return () => {
      mediaQuery.removeEventListener('change', updateScreenSize)
    }
  }, [])

  return null
}
