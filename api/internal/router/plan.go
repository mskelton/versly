package router

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/mskelton/versly/internal/data"
	"github.com/mskelton/versly/internal/plan"
	"github.com/mskelton/versly/internal/storage"
)

func GetPlans(r *gin.Engine) *gin.Engine {
	r.GET("/plans", func(c *gin.Context) {
		c.JSON(200, gin.H{"plans": []string{}})
	})

	return r
}

func CreatePlan(r *gin.Engine) *gin.Engine {
	r.POST("/plans", func(c *gin.Context) {
		metadata, err := data.LoadMetadata()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to load metadata"})
			return
		}

		var json plan.Options
		if err := c.ShouldBindJSON(&json); err != nil {
			c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
			return
		}

		days := plan.Generate(metadata, json)

		db, err := storage.DB()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to connect to database"})
			return
		}

		tx := db.Create(&days)
		if tx.Error != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to save plan"})
			return
		}

		c.JSON(200, gin.H{"days": days})
	})

	return r
}
