package service

import (
	"bytes"
	"context"
	"errors"
	"github.com/chai2010/webp"
	"image"
	"image-converter/model"
	"image/gif"
	"image/jpeg"
	"image/png"
	"log"
	"path/filepath"
	"strings"
)

type ConverterService struct {
	UnimplementedImageConverterServer
}

func (service *ConverterService) ConvertToWebP(_ context.Context, mediaValue *model.MediaValue) (*model.MediaValue, error) {
	img, err := detectFileFormat(mediaValue)
	if err != nil {
		log.Printf("failed to detect image format: %v", mediaValue.FileName)
		return mediaValue, nil
	}
	var buffer bytes.Buffer
	err = webp.Encode(&buffer, img, &webp.Options{Lossless: true})
	if err != nil {
		return nil, err
	}

	return &model.MediaValue{
		FileName:    changeFileExtension(mediaValue.FileName, ".webp"),
		Content:     buffer.Bytes(),
		ContentType: "image/webp",
	}, nil
}

// detectFileFormat - detect file format for webp encoding
func detectFileFormat(mediaValue *model.MediaValue) (image.Image, error) {
	switch {
	case strings.HasSuffix(mediaValue.FileName, ".png"):
		return png.Decode(bytes.NewReader(mediaValue.Content))
	case strings.HasSuffix(mediaValue.FileName, ".jpg"):
		return jpeg.Decode(bytes.NewReader(mediaValue.Content))
	case strings.HasSuffix(mediaValue.FileName, ".gif"):
		return gif.Decode(bytes.NewReader(mediaValue.Content))
	default:
		return nil, errors.New("unsupported file format")
	}
}

// changeFileExtension - change file extension
func changeFileExtension(fileName, newExt string) string {
	ext := filepath.Ext(fileName)
	return fileName[0:len(fileName)-len(ext)] + newExt
}
