package utils

import (
	"database/sql/driver"
	"encoding/json"
	"fmt"
)

type IntArray []int

func (a IntArray) Value() (driver.Value, error) {
	return json.Marshal(a)
}

func (a *IntArray) Scan(value interface{}) error {
	bytes, ok := value.([]byte)
	if !ok {
		return fmt.Errorf("failed to scan IntArray")
	}

	return json.Unmarshal(bytes, a)
}
