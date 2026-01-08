import { Metadata } from 'next'
import { Passage } from '@/app/components/Passage'
import { getPassageName } from '@/app/lib/bookInfo'
import { getPassage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'

const PASSAGE_ID = 'JHN.3.ESV'

export async function generateMetadata(): Promise<Metadata> {
  const parsedId = parsePassageId(PASSAGE_ID)
  if (!parsedId) {
    return {
      title: 'Versly',
    }
  }

  return {
    title: getPassageName(parsedId),
  }
}

export default async function Page() {
  const parsedId = parsePassageId(PASSAGE_ID)
  if (!parsedId) {
    return null
  }
  const passage = await getPassage(parsedId)

  return <Passage passage={passage} passageId={parsedId} />
}
