package storage

import (
	"github.com/mskelton/versly/pkg/models"
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

	db.AutoMigrate(&models.Plan{})
	db.AutoMigrate(&models.Day{})
	db.AutoMigrate(&models.Reading{})

	return db, nil
}
