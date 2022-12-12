package tts_server_lib

import (
	"context"
	"crypto/tls"
	"fmt"
	core "github.com/jing332/tts-server-go"
	"github.com/jing332/tts-server-go/tts"
	"github.com/jing332/tts-server-go/tts/azure"
	"github.com/jing332/tts-server-go/tts/creation"
	"github.com/jing332/tts-server-go/tts/edge"
	"io"
	"net/http"
	"time"
)

func init() {
	// 跳过证书验证
	http.DefaultClient.Transport = &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
}

type VoiceProperty tts.VoiceProperty
type VoiceProsody tts.Prosody
type VoiceExpressAs tts.ExpressAs

// Proto 转成原类型
func (v *VoiceProperty) Proto(prosody *VoiceProsody, exp *VoiceExpressAs) *tts.VoiceProperty {
	v.Prosody = (*tts.Prosody)(prosody)
	v.ExpressAs = (*tts.ExpressAs)(exp)
	return (*tts.VoiceProperty)(v)
}

type EdgeApi struct {
	Timeout      int32
	UseDnsLookup bool
	tts          *edge.TTS
}

func (e *EdgeApi) GetEdgeAudio(text, format string, property *VoiceProperty,
	prosody *VoiceProsody) ([]byte, error) {
	if e.tts == nil {
		e.tts = &edge.TTS{DnsLookupEnabled: e.UseDnsLookup}
	}
	property.Api = tts.ApiEdge
	proto := property.Proto(prosody, nil)

	text = core.SpecialCharReplace(text)
	ssml := `<speak version='1.0' xmlns='http://www.w3.org/2001/10/synthesis' xmlns:mstts='https://www.w3.org/2001/mstts' xml:lang='en-US'>` +
		proto.ElementString(text) +
		`</speak>`

	succeed := make(chan []byte)
	failed := make(chan error)
	go func() {
		audio, err := e.tts.GetAudio(ssml, format)
		if err != nil {
			e.tts = nil
			failed <- err
			return
		}
		succeed <- audio
	}()

	select {
	case audio := <-succeed:
		return audio, nil
	case err := <-failed:
		return nil, err
	case <-time.After(time.Duration(e.Timeout) * time.Millisecond):
		e.tts.CloseConn()
		e.tts = nil
		return nil, fmt.Errorf("已超时：%dms", e.Timeout)
	}
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

type ReadCallBack interface {
	OnRead([]byte)
}

type AzureApi struct {
	Timeout int32
	tts     *azure.TTS
}

func (a *AzureApi) GetAudio(text, format string, property *VoiceProperty,
	prosody *VoiceProsody, expressAS *VoiceExpressAs) ([]byte, error) {
	if a.tts == nil {
		a.tts = &azure.TTS{}
	}
	property.Api = 1
	proto := property.Proto(prosody, expressAS)

	text = core.SpecialCharReplace(text)
	ssml := `<speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="en-US">` +
		proto.ElementString(text) + `</speak > `

	succeed := make(chan []byte)
	failed := make(chan error)
	go func() {
		audio, err := a.tts.GetAudio(ssml, format)
		if err != nil {
			a.tts = nil
			failed <- err
			return
		}
		succeed <- audio
	}()

	select {
	case audio := <-succeed:
		return audio, nil
	case err := <-failed:
		return nil, err
	case <-time.After(time.Duration(a.Timeout) * time.Millisecond):
		a.tts.CloseConn()
		a.tts = nil
		return nil, fmt.Errorf("已超时：%dms", a.Timeout)
	}
}

func (a *AzureApi) GetAudioStream(text, format string, property *VoiceProperty,
	prosody *VoiceProsody, expressAS *VoiceExpressAs, readCb ReadCallBack) error {
	if a.tts == nil {
		a.tts = &azure.TTS{}
	}
	property.Api = tts.ApiAzure
	proto := property.Proto(prosody, expressAS)

	text = core.SpecialCharReplace(text)
	ssml := `<speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="en-US">` +
		proto.ElementString(text) + `</speak > `

	succeed := make(chan []byte)
	failed := make(chan error)
	go func() {
		err := a.tts.GetAudioStream(ssml, format, func(data []byte) {
			readCb.OnRead(data)
		})
		if err != nil {
			a.tts = nil
			failed <- err
			return
		}
		succeed <- nil
	}()
	select {
	case <-succeed:
		return nil
	case err := <-failed:
		return err
	}
}

func GetAzureVoice() ([]byte, error) {
	return azure.GetVoices()
}

type CreationApi struct {
	Timeout int32
	tts     *creation.TTS
	cancel  context.CancelFunc
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
	ctx, c.cancel = context.WithTimeout(context.Background(), time.Duration(c.Timeout)*time.Millisecond)

	property.Api = tts.ApiCreation
	proto := property.Proto(prosody, expressAS)

	text = core.SpecialCharReplace(text)

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
