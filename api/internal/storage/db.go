package storage

import (
	"github.com/mskelton/versly/internal/plan"
	"gorm.io/driver/sqlite"
	"gorm.io/gorm"
)

var db *gorm.DB

func DB() (*gorm.DB, error) {
	if db == nil {
		var err error
		db, err = connect()
		if err != nil {
			return nil, err
		}
	}

	return db, nil
}

func connect() (*gorm.DB, error) {
	db, err := gorm.Open(sqlite.Open("versly.db"), &gorm.Config{})
	if err != nil {
		return nil, err
	}

	db.AutoMigrate(&plan.Day{})
	db.AutoMigrate(&plan.Reading{})

	return db, nil
}
