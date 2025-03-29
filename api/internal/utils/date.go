package utils

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
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

func (d Date) Value() (driver.Value, error) {
	return d.Time, nil
}

func (d *Date) Scan(value interface{}) error {
	if t, ok := value.(time.Time); ok {
		d.Time = t
		return nil
	}

	return fmt.Errorf("cannot scan value %v into Date", value)
}
