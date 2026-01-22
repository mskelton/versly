'use client'

import { useEffect } from 'react'

export function ScrollToToday() {
  useEffect(() => {
    document.getElementById('today')?.scrollIntoView({ block: 'center' })
  }, [])

  return null
}
