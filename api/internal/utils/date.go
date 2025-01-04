package utils

import (
	"encoding/json"
	"time"
)

type Date struct {
	time.Time
}

func (c *Date) UnmarshalJSON(b []byte) error {
	str := string(b)
	parsedTime, err := time.Parse(`"`+time.DateOnly+`"`, str)
	if err != nil {
		return err
	}

	c.Time = parsedTime
	return nil
}

func (c Date) MarshalJSON() ([]byte, error) {
	return json.Marshal(c.Format(time.DateOnly))
}
