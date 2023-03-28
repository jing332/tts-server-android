package plugin_sync_server

import (
	"context"
	log "github.com/sirupsen/logrus"
	"io"
	"net/http"
	"strconv"
	"time"
)

type PluginCodeSyncServer struct {
	// OnPush 接收来自客户端代码
	OnPush func(code string)
	// OnPull 发送代码到客户端
	OnPull func() (string, error)
	// OnDebug 执行debug操作
	OnDebug func()
	// OnUi 执行预览UI操作
	OnUi func()

	serveMux *http.ServeMux
	server   *http.Server

	Log *log.Logger
}

func (p *PluginCodeSyncServer) handleFunc() {
	if p.serveMux == nil {
		p.serveMux = http.NewServeMux()
	}

	p.serveMux.Handle("/api/plugin/push", http.TimeoutHandler(http.HandlerFunc(p.pluginPushHandler), 15*time.Second, "timeout"))
	p.serveMux.Handle("/api/plugin/pull", http.TimeoutHandler(http.HandlerFunc(p.pluginPullHandler), 15*time.Second, "timeout"))
	p.serveMux.Handle("/api/plugin/action-debug", http.TimeoutHandler(http.HandlerFunc(p.pluginDebugHandler), 15*time.Second, "timeout"))
	p.serveMux.Handle("/api/plugin/action-ui", http.TimeoutHandler(http.HandlerFunc(p.pluginUiHandler), 15*time.Second, "timeout"))
}

func (p *PluginCodeSyncServer) Start(port int64) error {
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
func (p *PluginCodeSyncServer) Close(timeout time.Duration) error {
	p.Log.Infoln("shutting down server...")
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()
	return p.server.Shutdown(ctx)
}

func (p *PluginCodeSyncServer) pluginPushHandler(w http.ResponseWriter, r *http.Request) {
	bytes, err := io.ReadAll(r.Body)
	if err != nil {
		p.Log.Warnln(err)
		return
	}
	p.OnPush(string(bytes))
}

func (p *PluginCodeSyncServer) pluginPullHandler(w http.ResponseWriter, r *http.Request) {
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

func (p *PluginCodeSyncServer) pluginUiHandler(w http.ResponseWriter, r *http.Request) {
	p.OnUi()
}

func (p *PluginCodeSyncServer) pluginDebugHandler(w http.ResponseWriter, r *http.Request) {
	p.OnDebug()
}
