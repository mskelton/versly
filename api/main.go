package main

import (
	"log"
	"net/http"

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
	mux := http.NewServeMux()

	handlers.Home(mux)
	handlers.Docs(mux)
	handlers.GetPlans(mux)
	handlers.GetPlan(mux)
	handlers.CreatePlan(mux)
	handlers.CreatePlanFromTemplate(mux)

	log.Fatal(http.ListenAndServe(":8000", mux))
}
