package tts_server_lib

import (
	"fmt"
	"testing"
)

func TestEdge(t *testing.T) {
	e := &EdgeApi{}
	for i := 0; i < 9; i++ {
		data, err := e.GetEdgeAudio("zh-CN-XiaoxiaoNeural", fmt.Sprintf("%d我是测试文本", i),
			"12%", "0%","0%", "webm-24khz-16bit-mono-opus")
		if err != nil {
			t.Error(err)
		}

		t.Log(len(data))
	}
}

func TestAzure(t *testing.T) {
	a := &AzureApi{}
	data,err := a.GetAudio("zh-CN-XiaoxiaoNeural", "我是测试文本", "", "1.0", "", "0%", "0%",
		"0%", "webm-24khz-16bit-mono-opus")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(len(data))
}

func TestUploadLog(t *testing.T) {
	url, err := UploadLog("myqqwqwqiasdslog")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(url)
}
