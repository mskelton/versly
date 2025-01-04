package router

import "github.com/gin-gonic/gin"

func Setup() *gin.Engine {
	r := gin.Default()
	r.GET("/", func(c *gin.Context) {
		c.String(200, "Hello, world!")
	})
	r.GET("/ping", func(c *gin.Context) {
		c.String(200, "pong")
	})

	return r
}
