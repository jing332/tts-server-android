package wrapper

import (
	log "github.com/sirupsen/logrus"
)

type MyFormatter struct {
	log.Formatter
	OnLog func(level int32, msg string)
}

func (f *MyFormatter) Format(entry *log.Entry) ([]byte, error) {
	f.OnLog(int32(entry.Level), entry.Message)
	return nil, nil
}
