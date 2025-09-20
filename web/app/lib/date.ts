export function parseISO(s: string) {
  if (s.includes('T')) {
    return new Date(s)
  }

  return new Date(`${s}T00:00:00`)
}

export function isToday(date: Date) {
  const today = new Date()

  return (
    today.getFullYear() === date.getFullYear() &&
    today.getMonth() === date.getMonth() &&
    today.getDate() === date.getDate()
  )
}
