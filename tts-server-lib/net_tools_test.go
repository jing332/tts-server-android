package tts_server_lib

import "testing"

func TestHttpPost(t *testing.T) {
	payload := `tex=测试HttpPOST&spd=1&per=4114&cuid=baidu_speech_demo&idx=1&cod=2&lan=zh&ctp=1&pdt=220&vol=5&aue=3&pit=5&res_tag=audio`
	data, err := HttpPost("http://tsn.baidu.com/text2audio", payload, "")
	if err != nil {
		t.Fatal(err)
	}
	t.Log(len(data))
}
