package main

import (
	"net/http"

	"github.com/MarceloPetrucio/go-scalar-api-reference"
	"github.com/gin-gonic/gin"
	"github.com/mskelton/versly/pkg/handlers"
)

// @title           Versly
// @version         1.0
// @description     Bible reading plans

// @contact.name   Mark Skelton
// @contact.url    https://mskelton.dev
// @contact.email  info@mskelton.dev

// @license.name  ISC
// @license.url   https://opensource.org/licenses/ISC

// @host      localhost:8000
// @BasePath  /v1

func main() {
	r := gin.Default()

	r.GET("/", func(c *gin.Context) {
		c.String(200, "Hello, world!")
	})

	r.GET("/docs", func(c *gin.Context) {
		htmlContent, err := scalar.ApiReferenceHTML(&scalar.Options{
			SpecURL:  "./docs/swagger.json",
			DarkMode: true,
			CustomOptions: scalar.CustomOptions{
				PageTitle: "Versly API",
			},
		})

		if err != nil {
			c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		}

		c.Data(http.StatusOK, "text/html", []byte(htmlContent))
	})

	r = handlers.GetPlans(r)
	r = handlers.GetPlan(r)
	r = handlers.CreatePlan(r)

	r.Run(":8000")
}
