package handlers_test

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gavv/httpexpect"
	"github.com/mskelton/versly/pkg/handlers"
	"github.com/stretchr/testify/assert"
)

func TestGetPlans(t *testing.T) {
	mux := http.NewServeMux()
	handlers.GetPlans(mux)
	w := httptest.NewRecorder()

	req, _ := http.NewRequest("GET", "/plans", nil)
	mux.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	// TODO: sqlite DB per test
	// assert.JSONEq(t, `{"plans":[]}`, w.Body.String())
}

func TestCreatePlan(t *testing.T) {
	mux := http.NewServeMux()
	handlers.CreatePlan(mux)
	server := httptest.NewServer(mux)
	defer server.Close()
	e := httpexpect.New(t, server.URL)

	options := map[string]interface{}{
		"startDate": "2021-01-01",
		"duration":  30,
		"groups":    [][]string{{"MAT", "MRK", "LUK", "JHN"}},
	}

	days := e.POST("/plans").
		WithJSON(options).
		Expect().Status(200).JSON().Object().
		ContainsKey("plan").Value("plan").Object().
		ContainsKey("days").Value("days").Array().NotEmpty()

	days.Length().Equal(options["duration"])

	// First day
	days.Element(0).Object().ValueEqual("day", "2021-01-01")
	days.Element(0).Object().Value("readings").Array().Length().Equal(4)
	days.Element(0).Object().Value("readings").Array().
		Element(0).Object().
		ValueEqual("book", "MAT").
		ValueEqual("chapter", 1).
		ValueEqual("range", []float64{0, 25})

	// Second day
	days.Element(1).Object().ValueEqual("day", "2021-01-02")
	days.Element(1).Object().Value("readings").Array().Length().Equal(4)
	days.Element(1).Object().Value("readings").Array().
		Element(0).Object().
		ValueEqual("book", "MAT").
		ValueEqual("chapter", 5).
		ValueEqual("range", []float64{0, 48})
}
