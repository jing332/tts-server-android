package tts_server_lib

import (
	"github.com/jing332/tts-server-go/server"
	log "github.com/sirupsen/logrus"
	"io"
)

type LogCallback interface {
	Log(level int32, msg string)
}

var w LogHandler
var s *server.GracefulServer

func Init(cb LogCallback) {
	w.cb = cb //转发log到android
	log.SetOutput(w)
	log.SetFormatter(new(MyFormatter))

	s = &server.GracefulServer{}
	s.HandleFunc()
}

func RunServer(port int64, token string, useDnsEdge bool) {
	s.Token = token
	s.UseDnsEdge = useDnsEdge
	err := s.ListenAndServe(port)
	if err != nil {
		log.Error(err)
	}
	s = nil
}

func CloseServer() {
	if s != nil {
		s.Close()
	}
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
	OnLog func(level int32, msg string)
}

func (f *MyFormatter) Format(entry *log.Entry) ([]byte, error) {
	w.cb.Log(int32(entry.Level), entry.Message)
	return nil, nil
}
