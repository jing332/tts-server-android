package conv_server

import (
	"context"
	"embed"
	"errors"
	log "github.com/sirupsen/logrus"
	"io/fs"
	"net/http"
	"strconv"
	"sync"
	"time"
)

//go:embed public/*
var webFiles embed.FS

type ConvServer struct {
	OnCancelAudio func(engine string)
	OnGetWavAudio func(engine string, voice string, text string, rate int32, pitch int32) ([]byte, error)
	OnGetEngines  func() (string, error)
	OnGetVoices   func(engine string) (string, error)

	serveMux *http.ServeMux
	server   *http.Server

	Log *log.Logger
}

func (c *ConvServer) handleFunc() {
	if c.serveMux == nil {
		c.serveMux = http.NewServeMux()
	}

	webFilesFs, _ := fs.Sub(webFiles, "public")

	c.serveMux.Handle("/", http.FileServer(http.FS(webFilesFs)))

	c.serveMux.Handle("/api/tts", http.TimeoutHandler(http.HandlerFunc(c.ttsAPIHandler), 15*time.Second, "timeout"))
	c.serveMux.Handle("/api/engines", http.TimeoutHandler(http.HandlerFunc(c.enginesApiHandler), 15*time.Second, "timeout"))
	c.serveMux.Handle("/api/voices", http.TimeoutHandler(http.HandlerFunc(c.voicesApiHandler), 15*time.Second, "timeout"))

	c.serveMux.Handle("/api/legado", http.TimeoutHandler(http.HandlerFunc(c.legadoUrlApiHandler), 15*time.Second, "timeout"))
}

func (c *ConvServer) Start(port int64) error {
	if c.Log == nil {
		c.Log = log.New()
	}

	if c.serveMux == nil {
		c.handleFunc()
	}
	c.server = &http.Server{
		Addr:           ":" + strconv.FormatInt(port, 10),
		ReadTimeout:    10 * time.Second,
		WriteTimeout:   60 * time.Second,
		MaxHeaderBytes: 1 << 20,
		Handler:        c.serveMux,
	}

	c.Log.Infoln("\nstarting server...")
	err := c.server.ListenAndServe()
	if err == http.ErrServerClosed { // 说明调用Shutdown关闭
		err = nil
	} else if err != nil {
		return err
	}
	c.Log.Infoln("server closed")
	return nil
}

func (c *ConvServer) Close(timeout time.Duration) error {
	c.Log.Infoln("shutting down server...")
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()
	return c.server.Shutdown(ctx)
}

var ttsLock sync.Mutex

func (c *ConvServer) ttsAPIHandler(w http.ResponseWriter, r *http.Request) {
	ttsLock.Lock()
	defer ttsLock.Unlock()

	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

	params := r.URL.Query()
	engine := params.Get("engine")
	voice := params.Get("voice")

	text := params.Get("text")
	rate, _ := strconv.ParseInt(params.Get("rate"), 10, 32)
	pitch, _ := strconv.ParseInt(params.Get("pitch"), 10, 32)

	c.Log.Infof("api/tts: %v\n", params)

	var success = make(chan []byte)
	var failure = make(chan error)
	go func() {
		audio, err := c.OnGetWavAudio(engine, voice, text, int32(rate), int32(pitch))
		if err != nil {
			failure <- err
			return
		}
		success <- audio
	}()

	select {
	case audio := <-success:
		if err := writeAudioData(w, audio); err != nil {
			c.handleError(err)
		}
	case err := <-failure:
		c.writeErrorMsg(w, err)
	case <-r.Context().Done():
		c.OnCancelAudio(engine)
	}
}

var enginesLock sync.Mutex

func (c *ConvServer) enginesApiHandler(w http.ResponseWriter, r *http.Request) {
	enginesLock.Lock()
	enginesLock.Unlock()

	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

	if c.OnGetEngines == nil {
		c.writeErrorMsg(w, errors.New("OnGetEngines() == nil"))
		return
	}

	s, err := c.OnGetEngines()
	if err != nil {
		c.writeErrorMsg(w, err)
		return
	}

	w.WriteHeader(http.StatusOK)
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	_, err = w.Write([]byte(s))
	if err != nil {
		c.Log.Errorln(err)
	}
}

var voicesLock sync.Mutex

func (c *ConvServer) voicesApiHandler(w http.ResponseWriter, r *http.Request) {
	voicesLock.Lock()
	voicesLock.Unlock()

	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

	if c.OnGetVoices == nil {
		c.writeErrorMsg(w, errors.New("OnGetVoices() == nil"))
		return
	}

	engine := r.URL.Query().Get("engine")
	c.Log.Infof("api/voices: %s\n", engine)

	json, err := c.OnGetVoices(engine)
	if err != nil {
		c.writeErrorMsg(w, err)
		return
	}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	_, err = w.Write([]byte(json))
	if err != nil {
		c.handleError(err)
		return
	}
}

func (c *ConvServer) legadoUrlApiHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Access-Control-Allow-Origin", "*")
	w.Header().Set("Access-Control-Allow-Headers", "Content-Type")

	params := r.URL.Query()
	api := params.Get("api")
	name := params.Get("name")
	engine := params.Get("engine")
	voice := params.Get("voice")
	pitch := params.Get("pitch")

	json, err := getLegadoJson(api, name, engine, voice, pitch)
	if err != nil {
		c.writeErrorMsg(w, err)
		return
	}
	w.Header().Set("Content-Type", "application/json; charset=utf-8")
	_, err = w.Write([]byte(json))
	if err != nil {
		c.handleError(err)
		return
	}
}

func writeAudioData(w http.ResponseWriter, data []byte) error {
	w.Header().Set("Content-Type", "audio/x-wav")
	w.Header().Set("Content-Length", strconv.FormatInt(int64(len(data)), 10))
	w.Header().Set("Connection", "keep-alive")
	w.Header().Set("Keep-Alive", "timeout=5")
	_, err := w.Write(data)
	return err
}

func (c *ConvServer) writeErrorMsg(w http.ResponseWriter, e error) {
	c.Log.Errorln(e)
	w.WriteHeader(http.StatusInternalServerError)
	w.Header().Set("Content-Type", "text/plain; charset=utf-8")
	_, err := w.Write([]byte(e.Error()))
	if err != nil {
		c.Log.Warnf("write error to client failed: %v", err)
		return
	}
}

func (c *ConvServer) handleError(err error) {
	c.Log.Warnln(err.Error())
}

type EngineInfo struct {
	Name  string `json:"name"`
	Label string `json:"label"`
}

type ErrorResult struct {
}
