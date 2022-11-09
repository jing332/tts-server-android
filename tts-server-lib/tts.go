package tts_server_lib

import (
	"context"
	tts_server_go "github.com/jing332/tts-server-go"
	"github.com/jing332/tts-server-go/service"
	"github.com/jing332/tts-server-go/service/azure"
	"github.com/jing332/tts-server-go/service/creation"
	"github.com/jing332/tts-server-go/service/edge"
	"io"
	"net/http"
)

type VoiceProperty service.VoiceProperty
type VoiceProsody service.Prosody
type VoiceExpressAs service.ExpressAs

func (v *VoiceProperty) Proto(prosody *VoiceProsody, exp *VoiceExpressAs) *service.VoiceProperty {
	v.Prosody = (*service.Prosody)(prosody)
	v.ExpressAs = (*service.ExpressAs)(exp)
	return (*service.VoiceProperty)(v)
}

type EdgeApi struct {
	tts          *edge.TTS
	useDnsLookup bool
}

func (e *EdgeApi) GetEdgeAudio(text, format string, property *VoiceProperty,
	prosody *VoiceProsody) ([]byte, error) {
	if e.tts == nil {
		e.tts = &edge.TTS{UseDnsLookup: e.useDnsLookup}
	}
	property.Api = service.ApiEdge
	proto := property.Proto(prosody, nil)

	text = tts_server_go.SpecialCharReplace(text)
	ssml := `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='en-US'>` +
		proto.ElementString(text) +
		`</speak>`
	audio, err := e.tts.GetAudio(ssml, format)
	if err != nil {
		e.tts = nil
		return nil, err
	}
	return audio, nil
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

func (a *AzureApi) GetAudio(text, format string, property *VoiceProperty,
	prosody *VoiceProsody, expressAS *VoiceExpressAs) ([]byte, error) {
	if a.tts == nil {
		a.tts = &azure.TTS{}
	}
	property.Api = service.ApiAzure
	proto := property.Proto(prosody, expressAS)

	text = tts_server_go.SpecialCharReplace(text)
	ssml := `<speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="en-US">` +
		proto.ElementString(text) + `</speak > `
	audio, err := a.tts.GetAudio(ssml, format)
	if err != nil {
		a.tts = nil
		return nil, err
	}
	return audio, nil
}

func (a *AzureApi) GetAudioBySsml(ssml, format string) ([]byte, error) {
	return a.tts.GetAudio(ssml, format)
}

func GetAzureVoice() ([]byte, error) {
	return azure.GetVoices()
}

type CreationApi struct {
	tts    *creation.TTS
	cancel context.CancelFunc
}

func (c *CreationApi) Cancel() {
	if c.cancel != nil {
		c.cancel()
	}
}

func (c *CreationApi) GetCreationAudio(text, format string, property *VoiceProperty,
	prosody *VoiceProsody, expressAS *VoiceExpressAs) ([]byte, error) {
	if c.tts == nil {
		c.tts = creation.New()
	}

	var ctx context.Context
	ctx, c.cancel = context.WithCancel(context.Background())

	property.Api = service.ApiCreation
	proto := property.Proto(prosody, expressAS)

	text = tts_server_go.SpecialCharReplace(text)
	audio, err := c.tts.GetAudioUseContext(ctx, text, format, proto)
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
