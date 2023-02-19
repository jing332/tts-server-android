let PluginJS = {
    "name": "示例插件(无TTS功能)",
    "id": "com.microsoft.azure", //插件的唯一ID 建议以此种方式命名
    "author": "TTS Server",
    "description": "插件描述",
    "version": 1, // 插件版本号

    // 获取音频
    // text: 文本，locale：地区代码，voice：声音key，rate：语速，volume：音量，pitch：音高。(后三者范围都在100内, 当随系统时在200内)
    "getAudio": function(text, locale, voice, rate, volume, pitch){
        // 日志： 错误 logger.e(), 警告 logger.w(), 信息 logger.i()
        // 输出某对象所有属性/方法:  for (v in 某对象) logger.i(v)
        // ttsrv为实用方法的包装对象，如
        // let resp =  ttsrv.httpGet('https://xx.com/xx.mp3', {"Content-Type", "jsonxxx"})
        // resp.body().bytes() 获取字节数组, 使用ttsrv.bytesToStr() 转为String

        // 更多方法见项目文件夹：java/com/github/jing332/tts_server_android/help/plugin/ext
    },
}

// 全部voice数据
var voices = {}
// 当前语言下的voice
var currentVoices ={}

let EditorJS = {
    //音频的采样率 编辑TTS界面保存时调用
    "getAudioSampleRate": function(locale, voice) {
        // 根据voice判断返回的采样率
        // 也可以动态获取：
        // let audio = PluginJS.getAudio('test测试', locale, voice, 50, 50, 1)
        // return ttsrv.getAudioSampleRate(audio)

        return 16000
    },

    "getLocales": function() {
        let locales = new  Array()
        
        voices.forEach(function(v){
            let loc = v["Locale"]
            if (!locales.includes(loc)){
                locales.push(loc)
            }
        }) 
        
        return locales
    },

    // 当语言变更时调用
    "getVoices": function(locale){
        currentVoices = []
        voices.forEach(function(v){
            if (v['Locale'] === locale){
                currentVoices.push(v)
            }    
        }) 
        
        let mm = {}
         currentVoices.map(function(v){
              mm[v['ShortName']]= v['LocalName']
           })
        return mm
    },
    
    // 加载本地或网络数据，运行在IO线程。
    "onLoadData": function(){
        // 获取数据并缓存以便复用
        var jsonStr = ''
        if (ttsrv.fileExist('voices.json')){
            jsonStr = ttsrv.readTxtFile('voices.json')
        }else{
            let url  = "https://eastus.api.speech.microsoft.com/cognitiveservices/voices/list"
            let header = {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 Edg/107.0.1418.26",
                "X-Ms-Useragent":"SpeechStudio/2021.05.001",
            	"Content-Type": "application/json",
            	"Origin": "https://azure.microsoft.com",
            	"Referer":"https://azure.microsoft.com"
               }
             jsonStr = ttsrv.httpGetString(url, header)
            ttsrv.writeTxtFile('voices.json', jsonStr)
        }
       
       voices = JSON.parse(jsonStr)
       // 每一个TTS配置的附加数据
       ttsrv.extraData = 'qwqwqwqwqwq'
    },
    
    "onLoadUI": function(ctx, linerLayout) {
        //下拉框
        let spinner = JSpinner(ctx, '下拉框提示')  
        // 必须addView后才能设置间距margins, 参数依次为 view , 左，上，右，下
        linerLayout.addView(spinner)
        ttsrv.setMargins(spinner, 0, 8, 0,0)

        //选择变更时
        spinner.setOnItemSelected(function(view, pos, key){
            ttsrv.toast(pos+ key)   
        })
        spinner.items = [Item("display1Name", "value")]

        // 追加
        let arr = spinner.items
        arr.push(Item("displa", "qq"))
        spinner.items = arr
        
        spinner.selectedPosition = 0
        
        //文本输入框
        let tl = JTextInput(ctx,'')
        tl.hint = '输入框提示'
        linerLayout.addView(tl)
        ttsrv.setMargins(tl, 0, 4, 0, 0)
        //文本变更时调用
        tl.addTextChangedListener(function(txt){
           ttsrv.toast(txt)
        })
     
        //滑动条
        let seek = JSeekBar(ctx, '强度: ')
        // 当停止滑动时调用
        seek.onSeekBarChangeListener = {
            onStopTrackingTouch: function(view){
               ttsrv.toast(view.value)
            }
            
        }
        
        linerLayout.addView(seek)
        ttsrv.setMargins(seek, 0,4, 0, 0)
        
    }
    
}
