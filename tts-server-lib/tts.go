package tts_server_lib

import (
	"context"
	"github.com/jing332/tts-server-go/service/azure"
	"github.com/jing332/tts-server-go/service/creation"
	"github.com/jing332/tts-server-go/service/edge"
	"io"
	"net/http"
)

type EdgeApi struct {
	tts          *edge.TTS
	useDnsLookup bool
}

func (e *EdgeApi) GetEdgeAudio(voiceName, text, rate, pitch, volume, format string) ([]byte, error) {
	if e.tts == nil {
		e.tts = &edge.TTS{UseDnsLookup: e.useDnsLookup}
	}
	ssml := `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='en-US'><voice name='` + voiceName + `'><prosody pitch='` + pitch + `' rate ='` + rate + `' volume='` + volume + `'>` + text + `</prosody></voice></speak>`
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

type AzureApi struct {
	tts *azure.TTS
}

func (a *AzureApi) GetAudio(voiceName, text, style, styleDegree, role, rate, pitch, volume, format string) ([]byte, error) {
	if a.tts == nil {
		a.tts = &azure.TTS{}
	}
	ssml := `<speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="en-US">` +
		`<voice name="` + voiceName + `"><mstts:express-as style="` + style + `" styledegree="` + styleDegree + `" role="` + role + `">` +
		`<prosody rate="` + rate + `" pitch="` + pitch + `" volume="` + volume + `">` + text + `</prosody></mstts:express-as></voice></speak>`
	return a.tts.GetAudio(ssml, format)
}

func GetAzureVoice() ([]byte, error) {
	return azure.GetVoices()
}

type CreationArg creation.SpeakArg

type CreationApi struct {
	tts    *creation.TTS
	cancel context.CancelFunc
}

func (c *CreationApi) Cancel() {
	if c.cancel != nil {
		c.cancel()
	}
}

func (c *CreationApi) GetCreationAudio(arg *CreationArg) ([]byte, error) {
	if c.tts == nil {
		c.tts = creation.New()
	}

	var ctx context.Context
	ctx, c.cancel = context.WithCancel(context.Background())

	s := creation.SpeakArg(*arg)
	audio, err := c.tts.GetAudioUseContext(ctx, &s)
	if err != nil {
		return nil, err
	}

	c.cancel = nil
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
