package sync_server

import (
	"context"
	log "github.com/sirupsen/logrus"
	"io"
	"net/http"
	"strconv"
	"time"
)

type ScriptCodeSyncServer struct {
	// OnPush 接收来自客户端代码
	OnPush func(code string)
	// OnPull 发送代码到客户端
	OnPull func() (string, error)
	// OnAction 更多动作
	OnAction func(name string, body []byte)

	serveMux *http.ServeMux
	server   *http.Server

	Log *log.Logger
}

func (p *ScriptCodeSyncServer) handleFunc() {
	if p.serveMux == nil {
		p.serveMux = http.NewServeMux()
	}

	p.serveMux.Handle("/api/sync/push", http.TimeoutHandler(http.HandlerFunc(p.pluginPushHandler), 15*time.Second, "timeout"))
	p.serveMux.Handle("/api/sync/pull", http.TimeoutHandler(http.HandlerFunc(p.pluginPullHandler), 15*time.Second, "timeout"))
	p.serveMux.Handle("/api/sync/action", http.TimeoutHandler(http.HandlerFunc(p.editorActionHandler), 15*time.Second, "timeout"))
}

func (p *ScriptCodeSyncServer) Start(port int64) error {
	if p.Log == nil {
		p.Log = log.New()
	}

	if p.serveMux == nil {
		p.handleFunc()
	}
	p.server = &http.Server{
		Addr:           ":" + strconv.FormatInt(port, 10),
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   60 * time.Second,
		MaxHeaderBytes: 1 << 20,
		Handler:        p.serveMux,
	}

	p.Log.Infoln("\nstarting server...")
	err := p.server.ListenAndServe()
	if err == http.ErrServerClosed { // 说明调用Shutdown关闭
		err = nil
	} else if err != nil {
		return err
	}
	p.Log.Infoln("server closed")
	return nil
}
func (p *ScriptCodeSyncServer) Close(timeout time.Duration) error {
	p.Log.Infoln("shutting down server...")
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()
	return p.server.Shutdown(ctx)
}

func (p *ScriptCodeSyncServer) pluginPushHandler(w http.ResponseWriter, r *http.Request) {
	bytes, err := io.ReadAll(r.Body)
	if err != nil {
		p.Log.Warnln(err)
		return
	}
	p.OnPush(string(bytes))
}

func (p *ScriptCodeSyncServer) pluginPullHandler(w http.ResponseWriter, r *http.Request) {
	code, err := p.OnPull()
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(err.Error()))
		return
	}
	_, err = w.Write([]byte(code))
	if err != nil {
		p.Log.Warnln(err)
		return
	}
}

func (p *ScriptCodeSyncServer) editorActionHandler(w http.ResponseWriter, r *http.Request) {
	u := r.URL.Query()
	action := u.Get("action")

	var body []byte
	if r.Method == http.MethodPost {
		bytes, err := io.ReadAll(r.Body)
		if err != nil {
			p.Log.Warnln(err)
			return
		}
		body = bytes
	} else {
		body = []byte(u.Get("body"))
	}

	p.OnAction(action, body)
}
