package router_test

import (
	"io"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gavv/httpexpect"
	"github.com/gin-gonic/gin"
	"github.com/mskelton/versly/internal/router"
	"github.com/stretchr/testify/assert"
)

func TestMain(m *testing.M) {
	gin.SetMode(gin.TestMode)
	gin.DefaultWriter = io.Discard
	gin.DefaultErrorWriter = io.Discard

	m.Run()
}

func TestGetPlans(t *testing.T) {
	r := router.Setup()
	r = router.GetPlans(r)

	w := httptest.NewRecorder()
	req, _ := http.NewRequest("GET", "/plans", nil)
	r.ServeHTTP(w, req)

	assert.Equal(t, 200, w.Code)
	assert.JSONEq(t, `{"plans":[]}`, w.Body.String())
}

func TestCreatePlan(t *testing.T) {
	r := router.Setup()
	r = router.CreatePlan(r)
	server := httptest.NewServer(r)
	defer server.Close()
	e := httpexpect.New(t, server.URL)

	options := map[string]interface{}{
		"startDate": "2021-01-01",
		"duration":  30,
		"groups":    [][]string{{"MAT", "MRK", "LUK", "JHN"}},
	}

	days := e.POST("/plans").
		WithJSON(options).
		Expect().
		Status(200).
		JSON().Object().ContainsKey("days").
		Value("days").Array().NotEmpty()

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
