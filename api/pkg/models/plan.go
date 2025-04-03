package models

import "github.com/mskelton/versly/pkg/types"

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
	Date types.Date `json:"day"`
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
	Range types.IntArray `json:"range"`
}
