package tts_server_lib

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/asters1/tools"
	tts_server_go "github.com/jing332/tts-server-go"
	"io"
	"mime/multipart"
	"net/http"
	"strings"
)

func HttpGet(url, header string) ([]byte, error) {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, err
	}
	if header != "" {
		req.Header = tools.GetHeader(header)
	}
	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return nil, err
	}
	defer resp.Body.Close()
	data, err := io.ReadAll(resp.Body)
	if err != nil {
		return nil, err
	}
	return data, nil
}

func UploadLog(log string) (string, error) {
	uploadUrl := "https://bin.kv2.dev"
	req, err := http.NewRequest(http.MethodPost, uploadUrl, strings.NewReader(log))
	if err != nil {
		return "", err
	}

	res, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	return uploadUrl + res.Request.URL.Path, nil
}

func UploadConfig(str string) (string, error) {
	url := "https://sy.mgz6.cc/shuyuan"

	payload := &bytes.Buffer{}
	writer := multipart.NewWriter(payload)
	part, err := writer.CreateFormFile("file", "xx.json")
	if err != nil {
		return "", err
	}

	_, err = io.Copy(part, bytes.NewReader([]byte(str)))
	if err != nil {
		return "", err

	}
	writer.Close()

	req, err := http.NewRequest(http.MethodPost, url, payload)
	if err != nil {
		return "", err
	}
	req.Header.Add("Content-Type", writer.FormDataContentType())
	res, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer res.Body.Close()

	data, err := io.ReadAll(res.Body)
	if err != nil {
		return "", err
	}

	var v UploadConfigJson
	err = json.Unmarshal(data, &v)
	if err != nil {
		return "", err
	}

	if v.Msg == "success" {
		return url + "/" + v.Data, nil
	} else {
		return "", fmt.Errorf("上传失败：%v", v.Msg)
	}
}

func GetOutboundIP() string {
	return tts_server_go.GetOutboundIPString()
}

type UploadConfigJson struct {
	Data string `json:"data"`
	Msg  string `json:"msg"`
}
