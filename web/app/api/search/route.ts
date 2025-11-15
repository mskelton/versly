import { NextRequest, NextResponse } from 'next/server'
import { searchVerses } from '@/app/lib/search'

export const GET = (request: NextRequest) => {
  const searchParams = request.nextUrl.searchParams
  const query = searchParams.get('q')
  const translation = searchParams.get('translation') ?? 'ESV'

  if (!query) {
    return NextResponse.json({ results: [] })
  }

  const results = searchVerses(query, translation)

  return NextResponse.json({ results })
}
