package tts_server_lib

import (
	"github.com/jing332/tts-server-go/service/creation"
	"github.com/jing332/tts-server-go/service/edge"
	"io"
	"net/http"
)

type EdgeApi struct {
	tts *edge.TTS
}

func (e *EdgeApi) GetEdgeAudio(voiceName, text, rate, pitch, volume, format string) ([]byte, error) {
	if e.tts == nil {
		e.tts = &edge.TTS{}
	}
	ssml := `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='en-US'><voice name='`+ voiceName + `'><prosody pitch='`+ pitch +`' rate ='`+rate+`' volume='`+volume+`'>`+text+`</prosody></voice></speak>`
	return e.tts.GetAudio(ssml, format)
}

func GetEdgeVoices() ([]byte, error) {
	resp, err := http.Get("https://speech.platform.bing.com/consumer/speech/synthesize/readaloud/voices/list?trustedclienttoken=6A5AA1D4EAFF4E9FB37E23D68491D6F4")
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

type CreationArg creation.SpeakArg

type CreationApi struct {
	tts *creation.TTS
}

func (c *CreationApi) GetCreationAudio(arg *CreationArg) ([]byte, error) {
	if c.tts == nil {
		c.tts = &creation.TTS{}
	}

	s := creation.SpeakArg(*arg)
	audio, err := c.tts.GetAudio(&s)
	if err != nil {
		return nil, err
	}
	return audio, nil
}

func GetCreationVoices() ([]byte, error) {
	token, err := creation.GetToken()
	if err != nil {
		return nil, err
	}
	data, err := creation.GetVoices(token)
	if err != nil {
		return nil, err
	}
	return data, nil
}
