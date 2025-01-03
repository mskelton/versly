package data

import (
	"encoding/json"
	"os"
)

type ChapterMetadata struct {
	Book      string `json:"book"`
	Chapter   int    `json:"chapter"`
	Range     []int  `json:"range"`
	WordCount int    `json:"wordCount"`
}

func LoadMetadata() ([]ChapterMetadata, error) {
	data, err := os.ReadFile("internal/data/metadata.json")
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
