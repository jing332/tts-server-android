package tts_server_lib

import (
	"os"
	"testing"
	"time"
)

func TestEdge(t *testing.T) {
	e := &EdgeApi{}
	pro := &VoiceProperty{VoiceName: "zh-CN-XiaoxiaoNeural"}
	prosody := &VoiceProsody{Rate: 10, Volume: 50, Pitch: 0}
	data, err := e.GetEdgeAudio("我是测试文本",
		"webm-24khz-16bit-mono-opus", pro, prosody)
	if err != nil {
		t.Error(err)
	}

	t.Log(len(data))
	file, err := os.OpenFile("./edge.mp3", os.O_RDWR|os.O_TRUNC|os.O_CREATE, 0766)
	if err != nil {
		t.Fatal(err)
		return
	}
	file.Write(data)
	file.Close()
}

func TestAzure(t *testing.T) {
	a := &AzureApi{}
	pro := &VoiceProperty{VoiceName: "zh-CN-XiaoxiaoNeural"}
	prosody := &VoiceProsody{Rate: 10, Volume: 50, Pitch: 0}
	exp := &VoiceExpressAs{Style: "general", StyleDegree: 1.5, Role: "default"}
	data, err := a.GetAudio("我是测试文本", "webm-24khz-16bit-mono-opus", pro, prosody, exp)
	if err != nil {
		t.Fatal(err)
	}
	t.Log(len(data))
	file, err := os.OpenFile("./azure.mp3", os.O_RDWR|os.O_TRUNC|os.O_CREATE, 0766)
	if err != nil {
		t.Fatal(err)
		return
	}
	file.Write(data)
	file.Close()
}

func TestCreation(t *testing.T) {
	c := &CreationApi{}
	pro := &VoiceProperty{VoiceName: "zh-CN-XiaoxiaoNeural", VoiceId: "5f55541d-c844-4e04-a7f8-1723ffbea4a9"}
	prosody := &VoiceProsody{Rate: 10, Volume: 50, Pitch: 0}
	exp := &VoiceExpressAs{Style: "general", StyleDegree: 1.5, Role: "default"}

	go func() {
		time.Sleep(time.Second * 5)
		c.Cancel()
	}()
	audio, err := c.GetCreationAudio("text", "audio-48khz-96kbitrate-mono-mp3", pro, prosody, exp)
	if err != nil {
		t.Fatal(err)
	}
	t.Log(len(audio))
	file, err := os.OpenFile("./creation.mp3", os.O_RDWR|os.O_TRUNC|os.O_CREATE, 0766)
	if err != nil {
		t.Fatal(err)
		return
	}
	file.Write(audio)
	file.Close()
}

func TestUploadLog(t *testing.T) {
	url, err := UploadLog("myqqwqwqiasdslog")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(url)
}

func TestUploadConfig(t *testing.T) {
	s := "{\n    \"Name\": \"Microsoft Server Speech Text to Speech Voice (af-ZA, AdriNeural)\",\n    \"DisplayName\": \"Adri\",\n    \"LocalName\": \"Adri\",\n    \"ShortName\": \"af-ZA-AdriNeural\",\n    \"Gender\": \"Female\",\n    \"Locale\": \"af-ZA\",\n    \"LocaleName\": \"Afrikaans (South Africa)\",\n    \"SampleRateHertz\": \"24000\",\n    \"VoiceType\": \"Neural\",\n    \"Status\": \"GA\",\n    \"WordsPerMinute\": \"147\"\n  }"
	url, err := UploadConfig(s)
	if err != nil {
		t.Fatal(err)
	}
	t.Log(url)
}