package plan

import (
	"github.com/mskelton/versly/internal/data"
)

type Options struct {
	DaysPerWeek   int
	Duration      int
	Groups        [][]string
	WholeChapters bool
}

type Day struct {
	Day      int
	Readings []Reading
}

type Reading struct {
	Book    string
	Chapter int
	Section string
}

func Generate(metadata []data.ChapterMetadata, options Options) []Day {
	var plan []Day

	// Create a map of book to its chapters for easier lookup
	bookMap := map[string][]data.ChapterMetadata{}
	for _, chapter := range metadata {
		bookMap[chapter.Book] = append(bookMap[chapter.Book], chapter)
	}

	// // Calculate total words per day
	// totalWordCount := 0
	// for _, chapters := range bookMap {
	// 	for _, chapter := range chapters {
	// 		totalWordCount += chapter.WordCount
	// 	}
	// }
	//
	// wordsPerDay := totalWordCount / (options.DaysPerWeek * 52)
	//
	// // Prepare iterators for each group
	// groupIterators := make([]int, len(options.Groups))
	//
	// currentDay := 1
	// currentWordCount := 0
	// var currentDayReadings []Reading
	//
	// for {
	// 	done := true
	// 	for groupIndex, group := range options.Groups {
	// 		// If we have books left in this group
	// 		if groupIterators[groupIndex] < len(group) {
	// 			book := group[groupIterators[groupIndex]]
	// 			chapters, exists := bookMap[book]
	// 			if exists && len(chapters) > 0 {
	// 				chapter := chapters[0]
	// 				currentDayReadings = append(currentDayReadings, Reading{
	// 					Book:    chapter.Book,
	// 					Chapter: chapter.Chapter,
	// 				})
	// 				currentWordCount += chapter.WordCount
	//
	// 				// Remove chapter from bookMap
	// 				bookMap[book] = chapters[1:]
	// 				if len(bookMap[book]) == 0 {
	// 					groupIterators[groupIndex]++
	// 				}
	//
	// 				done = false
	// 			}
	// 		}
	// 	}
	//
	// 	// Finalize day if word count is reached or no more readings
	// 	if currentWordCount >= wordsPerDay || done {
	// 		plan = append(plan, Day{
	// 			Day:      currentDay,
	// 			Readings: currentDayReadings,
	// 		})
	//
	// 		currentDay++
	// 		currentWordCount = 0
	// 		currentDayReadings = nil
	// 	}
	//
	// 	// Break if all groups are exhausted
	// 	if done {
	// 		break
	// 	}
	// }

	return plan
}
