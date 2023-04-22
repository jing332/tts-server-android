package tts_server_lib

import (
	tts_server_go "github.com/jing332/tts-server-go"
	"net/http"
	"strings"
)

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

func GetOutboundIP() string {
	return tts_server_go.GetOutboundIPString()
}

type UploadConfigJson struct {
	Data string `json:"data"`
	Msg  string `json:"msg"`
}
