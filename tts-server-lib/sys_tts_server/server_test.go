package conv_server

import (
	"os"
	"testing"
)

func TestServer(t *testing.T) {
	server := ConvServer{}
	server.OnGetEngines = func() (string, error) {
		return `{["name": "com.google.android.tts", "Google语音服务"]}`, nil
	}
	server.OnGetWavAudio = func(engine string, voice string, text string, rate int32, pitch int32) ([]byte, error) {
		file, err := os.ReadFile("./ms_audio.wav")
		if err != nil {
			return nil, err
		}
		return file, nil
	}

	err := server.Start(1221)
	if err != nil {
		t.Fatal(err)
	}
}

type testCancelCallback struct {
}

func (testCancelCallback) Cancel() {

}
