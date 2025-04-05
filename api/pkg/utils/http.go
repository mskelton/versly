package utils

import (
	"encoding/json"
	"errors"
	"net/http"
)

type H map[string]any

func JSON(w http.ResponseWriter, statusCode int, data any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	json.NewEncoder(w).Encode(data)
}

func ShouldBindJSON(req *http.Request, obj any) error {
	if req.Body == nil {
		return errors.New("request body is empty")
	}

	defer req.Body.Close()
	decoder := json.NewDecoder(req.Body)
	decoder.DisallowUnknownFields()
	return decoder.Decode(obj)
}
