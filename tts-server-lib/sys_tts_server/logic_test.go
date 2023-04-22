package conv_server

import "testing"

func TestLegado(t *testing.T) {
	json, err := getLegadoJson("http://127.0.0.1/api/tts", "ni好啊", "com.gggoel.gtts", "voiceqwq", "50")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(json)
}
