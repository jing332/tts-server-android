let PluginJS = {
    "name": "插件名称",
    "id": "com.example.tts", //插件的唯一ID 建议以此种方式命名
    "author": "作者名称",
    "description": "插件描述",
    "version": 1, // 插件版本号

    //音频的采样率 编辑TTS界面保存时调用
    "getAudioSampleRate": function {
        return 16000
    }

    // 获取音频
    "getAudio": function(text, rate, volume, pitch){
        // 日志： 错误 logger.e(), 警告 logger.w(), 信息 logger.i()
        // 输出某对象所有属性/方法:  for (v : 某对象) logger.i(v)
        // ttsrv为实用方法的包装对象，如
        // let resp =  ttsrv.httpGet('https://xx.com/xx.mp3', {"Content-Type", "jsonxxx"})
        // resp.body().bytes() 获取字节数组, 使用ttsrv.bytesToStr() 转为String

        // 更多方法见项目文件夹：java/com/github/jing332/tts_server_android/help/plugin/ext
    },
}