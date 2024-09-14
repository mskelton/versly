<script lang="ts">
  import { john } from "../lib/bible"
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

  const book = john
  const chapterIndex = (daysDiff % (book.length ?? 0)) + -8
  const chapter = book[chapterIndex]
</script>

<main class="px-6 py-12 mx-auto">
  <h1 class="mb-10 text-5xl font-bold text-center">
    John {chapterIndex + 1}
  </h1>

  <div class="mt-4 text-lg max-w-lg mx-auto">
    <Passage html={chapter} />
  </div>
</main>
