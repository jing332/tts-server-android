package tts_server_lib

import (
	"github.com/jing332/tts-server-go/service/creation"
)

type CreationArg creation.SpeakArg

var creationApi *creation.Creation

func GetCreationAudio(arg *CreationArg) ([]byte, error) {
	if creationApi == nil {
		creationApi = &creation.Creation{}
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
