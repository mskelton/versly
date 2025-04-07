package main

import (
	"fmt"
	"log"
	"net/http"

	"github.com/MarceloPetrucio/go-scalar-api-reference"
	"github.com/mskelton/versly/pkg/handlers"
	"github.com/mskelton/versly/pkg/utils"
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
	mux := http.NewServeMux()

	mux.HandleFunc("GET /", func(w http.ResponseWriter, req *http.Request) {
		fmt.Println(req.URL.Path)
		// The "/" pattern matches everything, so we need to check that we're at the root
		if req.URL.Path != "/" {
			http.NotFound(w, req)
			return
		}

		utils.JSON(w, http.StatusOK, utils.H{
			"message": "Welcome to the Versly API. See /docs for API documentation.",
		})
	})

	mux.HandleFunc("GET /docs", func(w http.ResponseWriter, req *http.Request) {
		htmlContent, err := scalar.ApiReferenceHTML(&scalar.Options{
			SpecURL:  "./docs/swagger.json",
			DarkMode: true,
			CustomOptions: scalar.CustomOptions{
				PageTitle: "Versly API",
			},
		})

		if err != nil {
			utils.JSON(w, http.StatusInternalServerError, utils.H{"error": err.Error()})
		}

		w.Header().Add("Content-Type", "text/html")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(htmlContent))
	})

	handlers.GetPlans(mux)
	handlers.GetPlan(mux)
	handlers.CreatePlan(mux)
	handlers.CreatePlanFromTemplate(mux)

	log.Fatal(http.ListenAndServe(":8000", mux))
}
