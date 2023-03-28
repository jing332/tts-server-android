package tts_server_lib

import (
	log "github.com/sirupsen/logrus"
	"io"
	"time"
	"tts-server-lib/plugin_sync_server"
	"tts-server-lib/wrapper"
)

type PluginCodeSyncServerCallback interface {
	Log(level int32, msg string)
	Push(code string)
	Pull() (string, error)
	Debug()
	UI()
}

type PluginSyncServer struct {
	server   plugin_sync_server.PluginCodeSyncServer
	callback PluginCodeSyncServerCallback
}

func (p *PluginSyncServer) Init(cb PluginCodeSyncServerCallback) {
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
	p.server.OnDebug = cb.Debug
	p.server.OnUi = cb.UI
}

func (p *PluginSyncServer) Start(port int64) error {
	return p.server.Start(port)
}

func (p *PluginSyncServer) Close() error {
	return p.server.Close(time.Second * 5)
}
