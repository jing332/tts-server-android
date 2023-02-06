let PluginJS = {
    "name": "插件名称",
    "id": "com.example.tts", //插件的唯一ID 建议以此种方式命名
    "author": "作者名称",
    "description": "插件描述",
    "version": 1, // 插件版本号

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

let EditorJS = {
    //音频的采样率 编辑TTS界面保存时调用
    "getAudioSampleRate": function() {
        return 16000
    },

    "getLocales": function() {
      //返回通用语言代码, 可以带附加信息如陕西：zh-CN-shanxi
      return ["zh-CN", "zh-CN-shanxi", "zh-CN-sichuan"]
    },

    // 当语言变更时调用
    "getVoices": function(locale){
        // 根据locale变量 返回其语言下的发音人
        if (locale == "zh-CN"){
            return {"voice1": "中文发音人1", "voice2": "中文发音人2"}
        } else if (locale == "zh-CN-sichuan"){
            return {"voiceSichuan1": "方言四川1","voiceSichuan2": "方言四川2"}
        } else if (locale == "zh-CN-shanxi"){
            return {"voiceShanxi1": "方言陕西1","voiceSichuan2": "方言陕西2"}
        }
    },
}