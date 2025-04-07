package handlers

import (
	"fmt"
	"net/http"

	"github.com/mskelton/versly/pkg/utils"
)

func Home(mux *http.ServeMux) {
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
}
