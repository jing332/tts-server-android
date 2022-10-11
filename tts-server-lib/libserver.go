package tts_server_lib

import (
	"github.com/jing332/tts-server-go/service"
	log "github.com/sirupsen/logrus"
	"io"
	"time"
)

type LogCallback interface {
	Log(level int32, msg string)
}

var w LogHandler
var s *service.GracefulServer

func Init(cb LogCallback) {
	w.cb = cb //转发log到android
	log.SetOutput(w)
	log.SetFormatter(new(MyFormatter))

	s = &service.GracefulServer{}
	s.HandleFunc()
}

func RunServer(port int64) {
	err := s.ListenAndServe(port)
	if err != nil {
		log.Error(err)
	}
}

func CloseServer() string {
	err := s.Shutdown(time.Second * 5)
	if err != nil {
		return err.Error()
	}
	return ""
}

type LogHandler struct {
	w  io.Writer
	cb LogCallback
}

func (lh LogHandler) Write(p []byte) (n int, err error) {
	//lh.cb.Log(string(p))
	return 0, err
}

func (lh LogHandler) Close() {
	w.Close()
}

type MyFormatter struct {
}

func (f *MyFormatter) Format(entry *log.Entry) ([]byte, error) {
	w.cb.Log(int32(entry.Level), entry.Message)
	return nil, nil
}
