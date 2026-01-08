import { ChevronLeft, ChevronRight } from 'react-feather'
import { Reader } from '@/app/components/Reader'
import { getNextChapter, getPreviousChapter } from '@/app/lib/bookInfo'
import { Passage as PassageData } from '@/app/lib/passage'
import { PassageId } from '@/app/lib/passageId'
import { MobileReaderNavLink } from './MobileReaderNavLink'
import { ReaderNavLink } from './ReaderNavLink'

interface PassageProps {
  passage: PassageData
  passageId: PassageId
}

export function Passage({ passage, passageId }: PassageProps) {
  const previousHref = getPreviousChapter(passageId)
  const nextHref = getNextChapter(passageId)

  return (
    <>
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

          <div className="mt-4 text-lg max-w-xl px-6 py-12 pb-24 md:pb-12">
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

      {previousHref || nextHref ? (
        <footer className="fixed bottom-0 left-0 right-0 z-40 bg-white dark:bg-neutral-950 border-t border-gray-200 dark:border-gray-800 md:hidden">
          <div className="flex justify-between px-4 py-3">
            {previousHref ? (
              <MobileReaderNavLink
                direction="left"
                href={previousHref}
                icon={<ChevronLeft />}
                label="Previous"
              />
            ) : (
              <div />
            )}

            {nextHref ? (
              <MobileReaderNavLink
                direction="right"
                href={nextHref}
                icon={<ChevronRight />}
                label="Next"
              />
            ) : (
              <div />
            )}
          </div>
        </footer>
      ) : null}
    </>
  )
}
