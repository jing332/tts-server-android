package tts_server_lib

import (
	log "github.com/sirupsen/logrus"
	"io"
	"time"
)

import (
	"fmt"
	"github.com/jing332/tts-server-go/service"
	"github.com/jing332/tts-server-go/service/azure"
	"github.com/jing332/tts-server-go/service/edge"
	"strings"
)

type LogCallback interface {
	Log(msg string)
}

var w LogHandler
var s *service.GracefulServer

func Init() {
	log.SetFormatter(new(MyFormatter))
	s = &service.GracefulServer{}
	s.HandleFunc()
}

func RunServer(port int64, cb LogCallback) {
	w.onWrite = cb //转发log到android
	log.SetOutput(w)
	s.ListenAndServe(port)
}

func CloseServer(timeout int64) string {
	edge.CloseConn()
	azure.CloseConn() //time.Duration(timeout)
	err := s.Shutdown(time.Second * 5)
	if err != nil {
		return err.Error()
	}
	return ""
}

type LogHandler struct {
	w       io.Writer
	onWrite LogCallback
}

func (lh LogHandler) Write(p []byte) (n int, err error) {
	lh.onWrite.Log(string(p))
	return 0, err
}

func (lh LogHandler) Close() {
	w.Close()
}

type MyFormatter struct {
}

func (f *MyFormatter) Format(entry *log.Entry) ([]byte, error) {
	msg := fmt.Sprintf("[%s] %s", strings.ToUpper(entry.Level.String()), entry.Message)
	return []byte(msg), nil
}
