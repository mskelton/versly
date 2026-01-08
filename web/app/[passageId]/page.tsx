import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { Passage } from '@/app/components/Passage'
import { getPassageName } from '@/app/lib/bookInfo'
import { getPassage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'

type Props = {
  params: Promise<{ passageId: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { passageId } = await params
  const parsedId = parsePassageId(passageId)
  if (!parsedId) {
    notFound()
  }

  return {
    title: getPassageName(parsedId),
  }
}

export default async function Page({ params }: Props) {
  const { passageId } = await params
  const parsedId = parsePassageId(passageId)
  if (!parsedId) {
    notFound()
  }
  const passage = await getPassage(parsedId)

  return <Passage passage={passage} passageId={parsedId} />
}
