import { NextRequest, NextResponse } from 'next/server'
import type { CreatePlanRequest, PlanResponse } from '@/app/lib/plan-types'
import { generate, loadMetadata } from '@/app/lib/plan-generator'

export async function POST(request: NextRequest) {
  try {
    const body = (await request.json()) as CreatePlanRequest

    // Validate required fields
    if (!body.duration || !body.groups || !body.startDate) {
      return NextResponse.json(
        { error: 'Missing required fields: duration, groups, or startDate' },
        { status: 400 },
      )
    }

    // Validate groups
    if (!Array.isArray(body.groups) || body.groups.length === 0) {
      return NextResponse.json({ error: 'groups must be a non-empty array' }, { status: 400 })
    }

    // Validate startDate format
    const startDate = new Date(body.startDate)
    if (isNaN(startDate.getTime())) {
      return NextResponse.json(
        { error: 'Invalid startDate format. Use YYYY-MM-DD' },
        { status: 400 },
      )
    }

    // Load metadata
    const metadata = loadMetadata()

    // Generate plan
    const days = generate(metadata, body)

    // Create plan response with ID
    const planResponse: PlanResponse = {
      plan: {
        days,
        id: crypto.randomUUID(),
      },
    }

    return NextResponse.json(planResponse)
  } catch (error) {
    console.error('Error generating plan:', error)
    return NextResponse.json({ error: 'Failed to generate plan' }, { status: 500 })
  }
}
