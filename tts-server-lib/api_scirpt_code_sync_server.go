package tts_server_lib

import (
	log "github.com/sirupsen/logrus"
	"io"
	"time"
	"tts-server-lib/sync_server"
	"tts-server-lib/wrapper"
)

type ScriptCodeSyncServerCallback interface {
	Log(level int32, msg string)
	Push(code string)
	Pull() (string, error)

	Action(name string, body []byte)
}

type ScriptSyncServer struct {
	server   sync_server.ScriptCodeSyncServer
	callback ScriptCodeSyncServerCallback
}

func (p *ScriptSyncServer) Init(cb ScriptCodeSyncServerCallback) {
	p.callback = cb
	p.server.Log = log.New()
	p.server.Log.Out = io.Discard
	p.server.Log.Formatter = &wrapper.MyFormatter{
		OnLog: func(level int32, msg string) {
			p.callback.Log(level, msg)
		},
	}

	p.server.OnPush = cb.Push
	p.server.OnPull = cb.Pull

	p.server.OnAction = cb.Action
}

func (p *ScriptSyncServer) Start(port int64) error {
	return p.server.Start(port)
}

func (p *ScriptSyncServer) Close() error {
	return p.server.Close(time.Second * 5)
}
