package tts_server_lib

import (
	"github.com/asters1/tools"
	tts_server_go "github.com/jing332/tts-server-go"
	"io"
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

func GetOutboundIP() string{
	return tts_server_go.GetOutboundIPString()
}