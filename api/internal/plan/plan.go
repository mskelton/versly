package plan

import (
	"slices"

	"github.com/mskelton/versly/internal/data"
	"github.com/mskelton/versly/internal/utils"
)

type Options struct {
	// Which days of the week to rest and not complete any readings. 0 = Sunday,
	// 6 = Saturday.
	RestDays []int `json:"restDays"`
	// Total number of days to complete the plan. This includes rest days.
	Duration int `json:"duration" binding:"required"`
	// Groups define which books to read together. Each group is a slice of book
	// references. The plan will from each group every day (in order) evenly
	// distributing readings in each group across the plan.
	Groups [][]string `json:"groups" binding:"required"`
	// The first day of the plan. This can be in the past.
	StartDate utils.Date `json:"startDate" binding:"required"`
	// When true, chapters can be broken into sections for more even reading.
	AllowPartialChapters bool `json:"allowPartialChapters"`
}

type Plan struct {
	// The unique identifier for the plan
	ID uint `json:"id" gorm:"primaryKey"`
	// The days in the plan
	Days []Day `json:"days"`
}

type Day struct {
	// The unique identifier for the day
	ID uint `json:"id" gorm:"primaryKey"`
	// The plan this day belongs to
	PlanID uint `json:"-"`
	// The date of the reading day
	Date utils.Date `json:"day"`
	// The readings for the day
	Readings []Reading `json:"readings"`
}

type Reading struct {
	// The unique identifier for the reading
	ID uint `json:"id" gorm:"primaryKey"`
	// The day this reading belongs to
	DayID uint `json:"-"`
	// The book reference
	Book string `json:"book"`
	// The chapter number
	Chapter int `json:"chapter"`
	// The verse range
	Range utils.IntArray `json:"range"`
}

// Calculate total reading days, which is the total duration, minus the number
// of rest days that will occur during the plan lifetime.
func calculateTotalReadingDays(options Options) int {
	startWeekDay := options.StartDate.Weekday()
	totalRestDays := 0

	for i := 0; i < options.Duration; i++ {
		currentWeekDay := (int(startWeekDay) + i) % 7

		if slices.Contains(options.RestDays, currentWeekDay) {
			totalRestDays++
		}
	}

	return options.Duration - totalRestDays
}

func Generate(metadata []data.ChapterMetadata, options Options) []Day {
	var plan []Day

	// Create a map of book to its chapters for easier lookup
	bookMap := map[string][]data.ChapterMetadata{}
	for _, chapter := range metadata {
		bookMap[chapter.Book] = append(bookMap[chapter.Book], chapter)
	}

	// Prepare grouped metadata
	groupMetadata := make([][]data.ChapterMetadata, len(options.Groups))
	for i, group := range options.Groups {
		for _, book := range group {
			if chunks, found := bookMap[book]; found {
				groupMetadata[i] = append(groupMetadata[i], chunks...)
			}
		}
	}

	totalReadingDays := calculateTotalReadingDays(options)

	// Calculate word count per group
	groupWordCounts := make([]int, len(groupMetadata))
	for i, group := range groupMetadata {
		for _, chunk := range group {
			groupWordCounts[i] += chunk.WordCount
		}
	}

	// Count the total words per day for each group
	groupWordsPerDay := make([]int, len(groupMetadata))
	for i, wordCount := range groupWordCounts {
		groupWordsPerDay[i] = wordCount / totalReadingDays
	}

	// Store the total words read from each group as we build the plan
	groupWordsRead := make([]int, len(groupMetadata))

	// Store the reading progress for each group
	groupProgress := make([]int, len(groupMetadata))

	for day := 0; day < totalReadingDays; day++ {
		readings := []Reading{}

		for groupIndex, chunks := range groupMetadata {

			// Each day, determine the total number of words that should have been
			// read by this point in the plan. From that, we try to get as close as
			// possible to the target number of words for the day.
			accruedWords := groupWordsPerDay[groupIndex] * (day + 1)

			for groupProgress[groupIndex] < len(chunks) {
				progress := groupProgress[groupIndex]
				chunk := chunks[progress]

				readWords := groupWordsRead[groupIndex]
				remainingWords := accruedWords - readWords
				if remainingWords < chunk.WordCount {
					break
				}

				// Add the chunk to the readings
				readings = append(readings, Reading{
					Book:    chunk.Book,
					Chapter: chunk.Chapter,
					Range:   chunk.Range,
				})

				// Update the total words read from this group
				groupWordsRead[groupIndex] += chunk.WordCount

				// Update the progress
				groupProgress[groupIndex]++
			}
		}

		// Create a day with the readings
		plan = append(plan, Day{
			Date:     utils.Date{Time: options.StartDate.AddDate(0, 0, day)},
			Readings: readings,
		})
	}

	return plan
}
