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
  const previousHref = getPreviousChapter(parsedId)
  const nextHref = getNextChapter(parsedId)

  return (
    <main className="mx-auto w-fit">
      <div className="flex items-start">
        {previousHref ? (
          <ReaderNavLink
            href={previousHref}
            icon={<ChevronLeft />}
            label="Previous chapte"
            side="left"
          />
        ) : null}

        <div className="mt-4 text-lg max-w-xl px-6 py-12">
          <Reader passage={passage} />
        </div>

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
