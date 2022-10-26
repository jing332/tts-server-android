package tts_server_lib

import (
	"fmt"
	"testing"
	"time"
)

func TestEdge(t *testing.T) {
	e := &EdgeApi{}
	for i := 0; i < 9; i++ {
		data, err := e.GetEdgeAudio("zh-CN-XiaoxiaoNeural", fmt.Sprintf("%d我是测试文本", i),
			"12%", "0%", "0%", "webm-24khz-16bit-mono-opus")
		if err != nil {
			t.Error(err)
		}

		t.Log(len(data))
	}
}

func TestAzure(t *testing.T) {
	a := &AzureApi{}
	for i := 0; i < 5; i++ {
		data, err := a.GetAudio("zh-CN-XiaoxiaoNeural", "我是测试文本", "", "1.0", "", "0%", "0%",
			"0%", "webm-24khz-16bit-mono-opus")
		if err != nil {
			t.Fatal(err)
		}
		t.Log(len(data))
	}
}
func TestCreation(t *testing.T) {
	c := &CreationApi{}
	arg := &CreationArg{
		Text:        "我是测试文本我是测试文本我是测试文本我是测试文本我是测试文本",
		VoiceName:   "zh-CN-XiaoxiaoNeural",
		VoiceId:     "5f55541d-c844-4e04-a7f8-1723ffbea4a9",
		Rate:        "-50%",
		Style:       "general",
		StyleDegree: "1.0",
		Role:        "default",
		Volume:      "0%",
		Format:      "audio-48khz-96kbitrate-mono-mp3",
	}
	go func() {
		time.Sleep(100)
		c.Cancel()
	}()
	audio, err := c.GetCreationAudio(arg)
	if err != nil {
		t.Fatal(err)
	}
	t.Log(len(audio))
}

func TestUploadLog(t *testing.T) {
	url, err := UploadLog("myqqwqwqiasdslog")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(url)
}
