package tts_server_lib

import (
	"github.com/jing332/tts-server-go/service/creation"
	"github.com/jing332/tts-server-go/service/edge"
	"io"
	"net/http"
)

var edgeApi *edge.TTS

func GetEdgeAudio(voiceName, text, rate, pitch, format string) ([]byte, error) {
	if edgeApi == nil {
		edgeApi = &edge.TTS{}
	}
	ssml := `<speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="en-US">
<voice name="` + voiceName + `">
        <!-- <mstts:express-as style="News" styledegree="1.0"> -->
            <prosody rate="` + rate + `" pitch="` + pitch + `">` + text + `</prosody>
        <!-- </mstts:express-as> -->
    </voice>
</speak>`

	return edgeApi.GetAudio(ssml, format)
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

var creationApi *creation.TTS

func GetCreationAudio(arg *CreationArg) ([]byte, error) {
	if creationApi == nil {
		creationApi = &creation.TTS{}
	}

	s := creation.SpeakArg(*arg)
	audio, err := creationApi.GetAudio(&s)
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
