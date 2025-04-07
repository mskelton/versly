package types

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
)

type Range [2]int

func (a Range) Value() (driver.Value, error) {
	return json.Marshal(a)
}

func (a *Range) Scan(value interface{}) error {
	bytes, ok := value.([]byte)
	if !ok {
		return fmt.Errorf("failed to scan IntArray")
	}

	return json.Unmarshal(bytes, a)
}
