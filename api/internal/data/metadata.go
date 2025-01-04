package data

import (
	"encoding/json"
	"os"
	"path/filepath"
	"runtime"
)

type ChapterMetadata struct {
	Book      string `json:"book"`
	Chapter   int    `json:"chapter"`
	Range     []int  `json:"range"`
	WordCount int    `json:"wordCount"`
}

func LoadMetadata() ([]ChapterMetadata, error) {
	_, dir, _, _ := runtime.Caller(0)
	parentDir := filepath.Dir(dir)
	filename := filepath.Join(parentDir, "metadata.json")

	data, err := os.ReadFile(filename)
	if err != nil {
		return nil, err
	}

	var metadata []ChapterMetadata
	err = json.Unmarshal(data, &metadata)
	if err != nil {
		return nil, err
	}

	return metadata, nil
}
