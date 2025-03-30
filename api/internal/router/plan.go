package router

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/mskelton/versly/internal/data"
	"github.com/mskelton/versly/internal/plan"
	"github.com/mskelton/versly/internal/storage"
	"gorm.io/gorm"
)

// GetPlans godoc
// @Summary List plans
// @Description Get a list of plans, with a preview of the first 5 days of readings
// @Accept json
// @Produce json
// @Success 200 {array} plan.Plan
// @Router /plans [get]
func GetPlans(r *gin.Engine) *gin.Engine {
	r.GET("/plans", func(c *gin.Context) {
		db, err := storage.DB()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to connect to database"})
			return
		}

		plans := []plan.Plan{}
		tx := db.
			Preload("Days", func(db *gorm.DB) *gorm.DB {
				return db.Limit(5)
			}).
			Preload("Days.Readings").
			Find(&plans)

		if tx.Error != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to load plans"})
			return
		}

		c.JSON(200, gin.H{"plans": plans})
	})

	return r
}

func GetPlan(r *gin.Engine) *gin.Engine {
	r.GET("/plans/:id", func(c *gin.Context) {
		id := c.Param("id")
		db, err := storage.DB()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to connect to database"})
			return
		}

		plan := plan.Plan{}
		tx := db.Preload("Days.Readings").First(&plan, "id = ?", id)

		if tx.Error != nil {
			c.JSON(http.StatusNotFound, gin.H{"error": "Plan not found"})
			return
		}

		c.JSON(200, plan)
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
		plan := plan.Plan{Days: days}

		db, err := storage.DB()
		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to connect to database"})
			return
		}

		tx := db.Create(&plan)
		if tx.Error != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to save plan"})
			return
		}

		c.JSON(200, gin.H{"plan": plan})
	})

	return r
}
