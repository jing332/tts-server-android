package tts_server_lib

import (
	"crypto/tls"
	"fmt"
	core "github.com/jing332/tts-server-go"
	"github.com/jing332/tts-server-go/tts"
	"github.com/jing332/tts-server-go/tts/edge"
	"io"
	"net/http"
	"time"
)

func init() {
	// 跳过证书验证
	http.DefaultClient.Transport = &http.Transport{TLSClientConfig: &tls.Config{InsecureSkipVerify: true}}
	http.DefaultClient.Timeout = time.Second * 15
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
		return nil, fmt.Errorf("timed out：%dms", e.Timeout)
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
