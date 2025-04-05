package handlers

import (
	"encoding/json"
	"net/http"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"slices"

	"github.com/mskelton/versly/pkg/models"
	"github.com/mskelton/versly/pkg/storage"
	"github.com/mskelton/versly/pkg/types"
	"github.com/mskelton/versly/pkg/utils"
	"gorm.io/gorm"
)

type ChapterMetadata struct {
	Book      string `json:"book"`
	Chapter   int    `json:"chapter"`
	Range     []int  `json:"range"`
	WordCount int    `json:"wordCount"`
}

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
	StartDate types.Date `json:"startDate" binding:"required"`
	// When true, chapters can be broken into sections for more even reading.
	AllowPartialChapters bool `json:"allowPartialChapters"`
}

func LoadMetadata() ([]ChapterMetadata, error) {
	_, dir, _, _ := runtime.Caller(0)
	parentDir := filepath.Dir(dir)
	filename := filepath.Join(parentDir, "metadata.json")

	data, err := os.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	var metadata []ChapterMetadata
	err = json.Unmarshal(data, &metadata)
	if err != nil {
		return nil, err
	}

	return metadata, nil
}

// Calculate total reading days, which is the total duration, minus the number
// of rest days that will occur during the plan lifetime.
func calculateTotalReadingDays(options Options) int {
	startWeekDay := time.Time(options.StartDate).Weekday()
	totalRestDays := 0

	for i := 0; i < options.Duration; i++ {
		currentWeekDay := (int(startWeekDay) + i) % 7

		if slices.Contains(options.RestDays, currentWeekDay) {
			totalRestDays++
		}
	}

	return options.Duration - totalRestDays
}

func Generate(metadata []ChapterMetadata, options Options) []models.Day {
	var plan []models.Day

	// Create a map of book to its chapters for easier lookup
	bookMap := map[string][]ChapterMetadata{}
	for _, chapter := range metadata {
		bookMap[chapter.Book] = append(bookMap[chapter.Book], chapter)
	}

	// Prepare grouped metadata
	groupMetadata := make([][]ChapterMetadata, len(options.Groups))
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
		readings := []models.Reading{}

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
				readings = append(readings, models.Reading{
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
		plan = append(plan, models.Day{
			Date:     types.Date(time.Time(options.StartDate).AddDate(0, 0, day)),
			Readings: readings,
		})
	}

	return plan
}

// GetPlans godoc
// @Summary List plans
// @Description Get a list of plans, with a preview of the first 5 days of readings
// @Accept json
// @Produce json
// @Success 200 {array} models.Plan
// @Router /plans [get]
func GetPlans(mux *http.ServeMux) {
	mux.HandleFunc("GET /plans", func(w http.ResponseWriter, req *http.Request) {
		db, err := storage.DB()
		if err != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to connect to database"})
			return
		}

		plans := []models.Plan{}
		tx := db.
			Preload("Days", func(db *gorm.DB) *gorm.DB {
				return db.Limit(5)
			}).
			Preload("Days.Readings").
			Find(&plans)

		if tx.Error != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to load plans"})
			return
		}

		utils.JSON(w, http.StatusOK, utils.H{"plans": plans})
	})
}

func GetPlan(mux *http.ServeMux) {
	mux.HandleFunc("GET /plans/{id}", func(w http.ResponseWriter, req *http.Request) {
		id := req.PathValue("id")
		db, err := storage.DB()
		if err != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to connect to database"})
			return
		}

		plan := models.Plan{}
		tx := db.Preload("Days.Readings").First(&plan, "id = ?", id)

		if tx.Error != nil {
			utils.JSON(w, http.StatusNotFound, utils.H{"error": "Plan not found"})
			return
		}

		utils.JSON(w, http.StatusOK, plan)
	})
}

func CreatePlan(mux *http.ServeMux) {
	mux.HandleFunc("POST /plans", func(w http.ResponseWriter, req *http.Request) {
		metadata, err := LoadMetadata()
		if err != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to load metadata"})
			return
		}

		var json Options
		if err := utils.ShouldBindJSON(req, &json); err != nil {
			utils.JSON(w, http.StatusBadRequest, utils.H{"error": err.Error()})
			return
		}

		days := Generate(metadata, json)
		plan := models.Plan{Days: days}

		db, err := storage.DB()
		if err != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to connect to database"})
			return
		}

		tx := db.Create(&plan)
		if tx.Error != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": "Failed to save plan"})
			return
		}

		utils.JSON(w, http.StatusOK, utils.H{"plan": plan})
	})
}
