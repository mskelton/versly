package types

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
	"time"
)

type Date time.Time

func (c *Date) UnmarshalJSON(b []byte) error {
	str := string(b)
	parsedTime, err := time.Parse(`"`+time.DateOnly+`"`, str)
	if err != nil {
		return err
	}

	*c = Date(parsedTime)
	return nil
}

func (c Date) MarshalJSON() ([]byte, error) {
	return json.Marshal(time.Time(c).Format(time.DateOnly))
}

func (d Date) Value() (driver.Value, error) {
	return time.Time(d), nil
}

func (d *Date) Scan(value any) error {
	if t, ok := value.(time.Time); ok {
		*d = Date(t)
		return nil
	}

	return fmt.Errorf("cannot scan value %v into Date", value)
}
