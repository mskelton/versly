package main

import (
	"fmt"

	"github.com/mskelton/versly/internal/data"
	"github.com/mskelton/versly/internal/plan"
)

func main() {
	metadata := []data.ChapterMetadata{
		{Book: "Genesis", Chapter: 1, WordCount: 800},
		{Book: "Genesis", Chapter: 2, WordCount: 600},
	}

	options := plan.Options{
		DaysPerWeek:   5,
		Groups:        [][]string{{"GEN", "EXO"}, {"MAT", "MRK"}},
		WholeChapters: true,
		Duration:      52,
	}

	plan := plan.Generate(metadata, options)

	// Print the reading plan
	for _, day := range plan {
		fmt.Printf("Day %d:\n", day.Day)
		for _, reading := range day.Readings {
			if reading.Section != "" {
				fmt.Printf("  %s %d (%s)\n", reading.Book, reading.Chapter, reading.Section)
			} else {
				fmt.Printf("  %s %d\n", reading.Book, reading.Chapter)
			}
		}
	}
}
