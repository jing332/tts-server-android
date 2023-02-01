package tts_server_lib

import (
	"fmt"
	"testing"
)

func TestServer(t *testing.T) {
	s := new(SysTtsForwarder)
	s.InitCallback(new(testCallback))
	err := s.Start(1221)
	if err != nil {
		t.Fatal(err)
	}
}

type testCallback struct {
}

func (testCallback) Log(level int32, msg string) {
	fmt.Printf("log: %d, %s\n", level, msg)
}

func (testCallback) GetAudio(engine string, text string, rate int32) (file string, err error) {
	return "", nil
}

func (testCallback) CancelAudio(engine string) {
	fmt.Printf("CancelAudio: %s", engine)
}

func (testCallback) GetEngines() (json string, err error) {
	return `{["name":"com.google.tts", "label":"Google语音引擎"]}`, nil
}

func (testCallback) GetVoices(engine string) (json string, err error) {
	return `{["name":"voiceName", "locale":"zh-CN", "localeName":"中文"]}`, nil
}
