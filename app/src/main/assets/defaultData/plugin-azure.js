// 请点击保存后在 更多选项按钮(垂直三个点) -> 设置变量 中设置密钥和区域
// Please set the key and region in "More options" -> "Variables" after clicking save.

let key = ttsrv.userVars['key'] || 'Default_KEY'
let region = ttsrv.userVars['region'] || 'eastus'

let format = "audio-24khz-48kbitrate-mono-mp3"
let sampleRate = 24000 // 对应24khz. 格式后带有opus的实际采样率是其2倍
let isNeedDecode = true // 是否需要解码，如 format 为 raw 请设为 false

let PluginJS = {
    "name": "Azure",
    "id": "com.microsoft.azure",
    "author": "TTS Server",
    "description": "",
    "version": 3,
    "vars": { // 声明变量，再由用户设置。
        key: {label: "密钥 Key"},
        region: {label: "区域 Region", hint: "为空时使用默认'eastus'"},
    },

    "onLoad": function () {
      checkKeyRegion()
    },

    "getAudio": function (text, locale, voice, rate, volume, pitch) {
        rate = (rate * 2) - 100
        pitch = pitch - 50

        let styleDegree = ttsrv.tts.data['styleDegree']
        if (!styleDegree || Number(styleDegree) < 0.01) {
            styleDegree = '1.0'
        }

        let style = ttsrv.tts.data['style']
        let role = ttsrv.tts.data['role']
        if (!style || style === "") {
            style = 'general'
        }
        if (!role || role === "") {
            role = 'default'
        }

        let textSsml = ''
        let langSkill = ttsrv.tts.data['languageSkill']
        if (langSkill === "" || langSkill == null) {
            textSsml = escapeXml(text)
        } else {
            textSsml = `<lang xml:lang="${langSkill}">${escapeXml(text)}</lang>`
        }

        let ssml = `
        <speak xmlns="http://www.w3.org/2001/10/synthesis" xmlns:mstts="http://www.w3.org/2001/mstts" xmlns:emo="http://www.w3.org/2009/10/emotionml" version="1.0" xml:lang="zh-CN">
            <voice name="${voice}">
                <mstts:express-as style="${style}" styledegree="${styleDegree}" role="${role}">
                    <prosody rate="${rate}%" pitch="${pitch}%" volume="${volume}">${textSsml}</prosody>
                </mstts:express-as>
            </voice >
         </speak >
        `

        return getAudioInternal(ssml, format)
    },
}

function escapeXml(s) {
    return s.replace(/'/g, '&apos;').replace(/"/g, '&quot;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/&/g, '&amp;').replace(/\//g, '').replace(/\\/g, '');
}

function checkKeyRegion() {
    key = (key + '').trim()
    region = (region + '').trim()
    if (key === '' || region === '') {
        throw "请设置变量: 密钥Key与区域Region。 Please set the key and region."
    }
}

let ttsUrl = 'https://' + region + '.tts.speech.microsoft.com/cognitiveservices/v1'

function getAudioInternal(ssml, format) {
    let headers = {
        'Ocp-Apim-Subscription-Key': key,
        "X-Microsoft-OutputFormat": format,
        "Content-Type": "application/ssml+xml",
    }
    let resp = ttsrv.httpPost(ttsUrl, ssml, headers)
    if (resp.code() !== 200) {
        if (resp.code() === 401) {
            throw "401 Unauthorized 未授权，请检查密钥与区域是否正确。"
        }else if (resp.code() === 403) {
            throw "403 Forbidden 被禁止，您的Azure账户可能已被禁用。"
        }

        throw "音频获取失败: HTTP-" + resp.code()
    }

    return resp.body().byteStream()
}

// 全部voice数据
let voices = {}
// 当前语言下的voice
let currentVoices = new Map()

// 语言技能 二级语言
let skillSpinner

let styleSpinner
let roleSpinner
let seekStyle

let EditorJS = {
    //音频的采样率 编辑TTS界面保存时调用
    "getAudioSampleRate": function (locale, voice) {
        return sampleRate
    },

    "isNeedDecode": function (locale, voice) {
        return isNeedDecode
    },

    "getLocales": function () {
        let locales = new Array()

        voices.forEach(function (v) {
            let loc = v["Locale"]
            if (!locales.includes(loc)) {
                locales.push(loc)
            }
        })

        return locales
    },

    // 当语言变更时调用
    "getVoices": function (locale) {
        currentVoices = new Map()
        voices.forEach(function (v) {
            if (v['Locale'] === locale) {
                currentVoices.set(v['ShortName'], v)
            }
        })

        let mm = {}
        for (let [key, value] of currentVoices.entries()) {
            mm[key] = new java.lang.String(value['LocalName'] + ' (' + key + ')')
        }
        return mm
    },

    // 加载本地或网络数据，运行在IO线程。
    "onLoadData": function () {
        // 获取数据并缓存以便复用
        let jsonStr = ''
        if (ttsrv.fileExist('voices.json')) {
            jsonStr = ttsrv.readTxtFile('voices.json')
        } else {
            checkKeyRegion()
            let url = 'https://' + region + '.tts.speech.microsoft.com/cognitiveservices/voices/list'
            let header = {
                "Ocp-Apim-Subscription-Key": key,
                "Content-Type": "application/json",
            }
            jsonStr = ttsrv.httpGetString(url, header)


            ttsrv.writeTxtFile('voices.json', jsonStr)
        }

        voices = JSON.parse(jsonStr)
    },

    "onLoadUI": function (ctx, linerLayout) {
        let layout = new LinearLayout(ctx)
        layout.orientation = LinearLayout.HORIZONTAL // 水平布局
        let params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1)

        skillSpinner = JSpinner(ctx, "语言技能 (language skill)")
        linerLayout.addView(skillSpinner)
        ttsrv.setMargins(skillSpinner, 2, 4, 0, 0)
        skillSpinner.setOnItemSelected(function (spinner, pos, item) {
            ttsrv.tts.data['languageSkill'] = item.value + ''
        })

        styleSpinner = JSpinner(ctx, "风格 (style)")
        styleSpinner.layoutParams = params
        layout.addView(styleSpinner)
        ttsrv.setMargins(styleSpinner, 2, 4, 0, 0)
        styleSpinner.setOnItemSelected(function (spinner, pos, item) {
            ttsrv.tts.data['style'] = item.value
            // 默认 || value为空 || value空字符串
            if (pos === 0 || !item.value || item.value === "") {
                seekStyle.visibility = View.GONE // 移除风格强度
            } else {
                seekStyle.visibility = View.VISIBLE // 显示
            }
        })

        roleSpinner = JSpinner(ctx, "角色 (role)")
        roleSpinner.layoutParams = params
        layout.addView(roleSpinner)
        ttsrv.setMargins(roleSpinner, 0, 4, 2, 0)
        roleSpinner.setOnItemSelected(function (spinner, pos, item) {
            ttsrv.tts.data['role'] = item.value
        })
        linerLayout.addView(layout)

        seekStyle = JSeekBar(ctx, "风格强度 (Style degree)：")
        linerLayout.addView(seekStyle)
        ttsrv.setMargins(seekStyle, 0, 4, 0, -4)
        seekStyle.setFloatType(2) // 二位小数
        seekStyle.max = 200 //最大200个刻度

        let styleDegree = Number(ttsrv.tts.data['styleDegree'])
        if (!styleDegree || isNaN(styleDegree)) {
            styleDegree = 1.0
        }
        seekStyle.value = new java.lang.Float(styleDegree)

        seekStyle.setOnChangeListener(
            {
                // 开始时
                onStartTrackingTouch: function (seek) {

                },
                // 进度滑动更改时
                onProgressChanged: function (seek, progress, fromUser) {

                },
                // 停止时
                onStopTrackingTouch: function (seek) {
                    ttsrv.tts.data['styleDegree'] = Number(seek.value).toFixed(2)
                },
            }
        )
    },

    "onVoiceChanged": function (locale, voiceCode) {
        let vic = currentVoices.get(voiceCode)

        let locale2List = vic['SecondaryLocaleList']
        let locale2Items = []
        let locale2Pos = 0

        if (locale2List) {
            locale2Items.push(Item("默认 (default)", ""))
            locale2List.map(function (v, i) {
                let loc = java.util.Locale.forLanguageTag(v)
                let name = loc.getDisplayName(loc)
                locale2Items.push(Item(name, v))
                if (v === ttsrv.tts.data['languageSkill'] + '') {
                    locale2Pos = i + 1
                }
            })
        }
        skillSpinner.items = locale2Items
        skillSpinner.selectedPosition = locale2Pos

        if (locale2Items.length === 0) {
            skillSpinner.visibility = View.GONE
        } else {
            skillSpinner.visibility = View.VISIBLE
        }

        let styles = vic['StyleList']
        let styleItems = []
        let stylePos = 0
        if (styles) {
            styleItems.push(Item("默认 (general)", ""))
            styles.map(function (v, i) {
                styleItems.push(Item(getString(v), v))
                if (v === ttsrv.tts.data['style'] + '') {
                    stylePos = i + 1 //算上默认的item 所以要 +1
                }
            })
        } else {
            seekStyle.visibility = View.GONE
        }
        styleSpinner.items = styleItems
        styleSpinner.selectedPosition = stylePos

        let roles = vic['RolePlayList']
        let roleItems = []
        let rolePos = 0
        if (roles) {
            roleItems.push(Item("默认 (default)", ""))
            roles.map(function (v, i) {
                roleItems.push(Item(getString(v), v))
                if (v === ttsrv.tts.data['role'] + '') {
                    rolePos = i + 1 //算上默认的item 所以要 +1
                }
            })
        }
        roleSpinner.items = roleItems
        roleSpinner.selectedPosition = rolePos
    }
}

let cnLocales = {
    "narrator": "旁白",
    "girl": "女孩",
    "boy": "男孩",
    "youngadultfemale": "年轻女性",
    "youngadultmale": "年轻男性",
    "olderadultfemale": "年长女性",
    "olderadultmale": "年长男性",
    "seniorfemale": "年老女性",
    "seniormale": "年老男性",

    "advertisement_upbeat": "广告推销",
    "affectionate": "亲切",
    "angry": "生气",
    "assistant": "数字助理",
    "calm": "平静",
    "chat": "闲聊",
    "cheerful": "愉快",
    "customerservice": "客户服务",
    "depressed": "沮丧",
    "disgruntled": "不满",
    "documentary-narration": "纪录片",
    "embarrassed": "尴尬",
    "empathetic": "同情",
    "envious": "嫉妒",
    "excited": "兴奋",
    "fearful": "恐惧",
    "friendly": "友好",
    "gentle": "温柔",
    "hopeful": "希望",
    "lyrical": "抒情",
    "narration-professional": "专业",
    "narration-relaxed": "轻松",
    "newscast": "新闻",
    "newscast-casual": "新闻-休闲",
    "newscast-formal": "新闻-正式",
    "poetry-reading": "诗歌朗诵",
    "sad": "悲伤",
    "serious": "严肃",
    "shouting": "喊叫",
    "sports_commentary": "体育",
    "sports_commentary_excited": "体育-兴奋",
    "whispering": "耳语",
    "terrified": "恐惧",
    "unfriendly": "不友好",
}

let isZh = java.util.Locale.getDefault().getLanguage() == 'zh'

function getString(key) {
    if (isZh) {
        return cnLocales[key.toLowerCase()] || key
    } else {
        return key
    }
}