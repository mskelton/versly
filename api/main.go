package main

import (
	"fmt"
	"time"

	"github.com/mskelton/versly/internal/data"
	"github.com/mskelton/versly/internal/plan"
)

func main() {
	metadata, err := data.LoadMetadata()
	if err != nil {
		fmt.Println(err)
		return
	}

	options := plan.Options{
		Duration:      30,
		Groups:        [][]string{{"GEN", "EXO"}, {"MAT", "MRK"}},
		RestDays:      []int{0, 6},
		StartDate:     time.Now(),
		WholeChapters: true,
	}

	plan := plan.Generate(metadata, options)

	// Print the reading plan
	for _, day := range plan {
		fmt.Printf("Jan %d:\n", day.Date.Day())

		for _, reading := range day.Readings {
			fmt.Printf("  %s %d\n", reading.Book, reading.Chapter)
		}
	}
}
