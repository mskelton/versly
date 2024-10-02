<script lang="ts">
  import { DEFAULT_BOOK, DEFAULT_TRANSLATION, translations } from "../lib/bible"
  import Passage from "./Passage.svelte"

  let start = new Date("2024-08-31")
  let now = new Date()

  // Set the hours to 0 to compare the dates without the time since we want to
  // calculate calendar dates.
  start.setHours(0, 0, 0, 0)
  now.setHours(0, 0, 0, 0)

  // Get time difference in milliseconds and convert to days
  const timeDiff = Math.abs(now.getTime() - start.getTime())
  const daysDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24))

  const translation = translations[DEFAULT_TRANSLATION]
  const book = translation.books.find((book) => book.ref === DEFAULT_BOOK)
  const chapterIndex = daysDiff % (book?.chapters.length ?? 0)
  const chapter = book?.chapters[chapterIndex]
</script>

{#if book && chapter}
  <main class="px-6 py-12 mx-auto">
    <h1 class="mb-10 text-5xl font-bold text-center">
      {book.title}
      {chapter.ref}
    </h1>

    <div class="mt-4 text-lg max-w-lg mx-auto">
      <Passage html={chapter.content} />
    </div>
  </main>
{/if}
