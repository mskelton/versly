package main

import "github.com/mskelton/versly/internal/router"

func main() {
	r := router.Setup()
	r = router.GetPlans(r)
	r = router.CreatePlan(r)
	r.Run(":8000")
}
