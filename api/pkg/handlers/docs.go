package handlers

import (
	"net/http"

	"github.com/MarceloPetrucio/go-scalar-api-reference"
	"github.com/mskelton/versly/pkg/utils"
)

func Docs(mux *http.ServeMux) {
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
}
