<script>
  import { bible } from "../lib/bible"

  let start = new Date("2024-08-31")
  let now = new Date()

  // Set the hours to 0 to compare the dates without the time since we want to
  // calculate calendar dates.
  start.setHours(0, 0, 0, 0)
  now.setHours(0, 0, 0, 0)

  // Get time difference in milliseconds and convert to days
  const timeDiff = Math.abs(now.getTime() - start.getTime())
  const daysDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24))

  const book = bible.books.find((book) => book.ref === "John")
  const chapterRef = daysDiff % (book?.chapters.length ?? 0)
  const chapter = book?.chapters[chapterRef]
</script>

<main class="p-12 mx-auto">
  <h1 class="mb-10 text-5xl font-bold text-center">{book?.ref} {chapterRef}</h1>
  <p class="mt-4 text-lg max-w-xl mx-auto text-justify">
    {#each chapter?.verses ?? [] as verse}
      <span>
        <sup class="text-xs text-gray-500">{verse.ref}</sup>
        {verse.text}
      </span>
    {/each}
  </p>
</main>
