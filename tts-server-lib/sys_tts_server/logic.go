package conv_server

import (
	"encoding/json"
	"time"
)

type LegadoJson struct {
	ContentType    string `json:"contentType"`
	Header         string `json:"header"`
	ID             int64  `json:"id"`
	LastUpdateTime int64  `json:"lastUpdateTime"`
	Name           string `json:"name"`
	URL            string `json:"url"`
	ConcurrentRate string `json:"concurrentRate"`
	//EnabledCookieJar bool   `json:"enabledCookieJar"`
	//LoginCheckJs   string `json:"loginCheckJs"`
	//LoginUI        string `json:"loginUi"`
	//LoginURL       string `json:"loginUrl"`
}

func getLegadoJson(api string, displayName string, engine string, voice string, pitch string) (string, error) {
	t := time.Now().UnixNano() / 1e6
	url := api + `?engine=` + engine + `&text={{java.encodeURI(speakText)}}&rate={{speakSpeed * 2}}&pitch=` +
		pitch + `&voice=` + voice

	data := &LegadoJson{Name: displayName, LastUpdateTime: t, ID: t, URL: url, ContentType: "audio/x-wav", ConcurrentRate: "100"}
	jsonStr, err := json.Marshal(data)
	if err != nil {
		return "", err
	}
	return string(jsonStr), nil
}
