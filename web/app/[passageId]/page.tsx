import { Metadata } from 'next'
import { notFound } from 'next/navigation'
import { ChevronLeft, ChevronRight } from 'react-feather'
import { Reader } from '@/app/components/Reader'
import { getNextChapter, getPassageName, getPreviousChapter } from '@/app/lib/bookInfo'
import { getPassage } from '@/app/lib/passage'
import { parsePassageId } from '@/app/lib/passageId'
import { ReaderNavLink } from '../components/ReaderNavLink'

type Props = {
  params: Promise<{ passageId: string }>
}

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const passageId = parsePassageId((await params).passageId)
  if (!passageId) {
    notFound()
  }

  return {
    title: getPassageName(passageId),
  }
}

export default async function Page({ params }: Props) {
  const { passageId } = await params
  const parsedId = parsePassageId(passageId)
  if (!parsedId) {
    notFound()
  }
  const passage = await getPassage(parsedId)
  const previousHref = getPreviousChapter(parsedId)
  const nextHref = getNextChapter(parsedId)

  return (
    <main className="px-6 py-12 mx-auto">
      <div className="mt-4 text-lg max-w-lg mx-auto">
        {previousHref ? (
          <ReaderNavLink
            href={previousHref}
            icon={<ChevronLeft />}
            label="Previous chapte"
            side="left"
          />
        ) : null}

        <Reader passage={passage} />

        {nextHref ? (
          <ReaderNavLink
            href={nextHref}
            icon={<ChevronRight />}
            label="Next chapter"
            side="right"
          />
        ) : null}
      </div>
    </main>
  )
}
