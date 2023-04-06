![MIT](https://img.shields.io/badge/license-MIT-green)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/release.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/release.yml)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/test.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/test.yml)

![GitHub release](https://img.shields.io/github/downloads/jing332/tts-server-android/total)
![GitHub release (latest by date)](https://img.shields.io/github/downloads/jing332/tts-server-android/latest/total)

# TTS Server [![](https://img.shields.io/badge/Q%E7%BE%A4-124841768-blue)](https://jq.qq.com/?_wv=1027&k=y7WCDjEA)

本APP起初为阅读APP的网络朗读所用，在原有基础上，现已支持:

* 内置微软接口(Edge大声朗读、Azure演示API)，可自定义HTTP请求，可导入其他本地TTS引擎，以及根据中文双引号的简单旁白/对话识别朗读
  ，还有自动重试，备用配置，文本替换等更多功能。

这是 [tts-server-go](https://github.com/jing332/tts-server-go)
的Android版本。使用Kotlin开发，通过 [Gomobile](https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile)
将Go编译为Lib以供Android APP调用. 关于本项目的Go Lib,见 [tts-server-lib](./tts-server-lib).

<details>
  <summary>点击展开查看截图</summary>

> 左图为服务转发日志界面，用于阅读APP的网络朗读。<br>
> 右图为系统TTS配置界面，可被使用系统TTS的APP调用。

  <img src="./images/Screenshot_Main.png" height="150px">
  <img src="./images/Screenshot_SysTTS.png" height="150px">

</details>

# Download

* [Stable - 稳定版(Releases)](https://github.com/jing332/tts-server-android/releases)

* [Dev - 开发版(Actions 需登陆Github账户)](https://github.com/jing332/tts-server-android/actions)

# FAQ

### Azure websocket: bad handshake ？

> 说明你的IP被微软服务器限制，使用低流量消耗的音频格式(16khz-32bitrate-mp3)可降低发生几率。

### 如何添加自己的Azure API？

> 使用自定义HTTP TTS，见https:
> https//github.com/jing332/tts-server-android/issues/43#issuecomment-1405072848
>
> (推荐)使用插件: 插件管理 -> 右上角添加 -> 填入Key与Region -> 保存并启用即可。

# Grateful

<details>
  <summary>开源项目</summary>

| Android Application                                                             | Microsoft TTS                                                         |
|---------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| [gedoor/legado](https://github.com/gedoor/legado)                               | [wxxxcxx/ms-ra-forwarder](https://github.com/wxxxcxx/ms-ra-forwarder) |
| [ag2s20150909/TTS](https://github.com/ag2s20150909/TTS)                         | [litcc/tts-server](https://github.com/litcc/tts-server)               |
| [benjaminwan/ChineseTtsTflite](https://github.com/benjaminwan/ChineseTtsTflite) | [asters1/tts](https://github.com/asters1/tts)                         |
| [yellowgreatsun/MXTtsEngine](https://github.com/yellowgreatsun/MXTtsEngine)     |
| [2dust/v2rayNG](https://github.com/2dust/v2rayNG)                               |

| Android Library                                                                                   | Description                                                                 |
|---------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| [Rosemoe/sora-editor](https://github.com/Rosemoe/sora-editor)                                     | sora-editor is a cool and optimized code editor on Android platform         |
| [gedoor/rhino-android](https://github.com/gedoor/rhino-android)                                   | Give access to RhinoScriptEngine from the JSR223 interfaces on Android JRE. |
| [liangjingkanji/BRV](https://github.com/liangjingkanji/BRV)                                       | Android上最好的RecyclerView框架, 比 BRVAH 更简单强大                                    |
| [liangjingkanji/Net](https://github.com/liangjingkanji/Net)                                       | Android最好的网络请求工具, 比 Retrofit/OkGo 更简单易用                                     |
| [chibatching/kotpref](https://github.com/chibatching/kotpref)                                     | Android SharedPreferences delegation library for Kotlin                     |
| [google/ExoPlayer](https://github.com/google/ExoPlayer)                                           | An extensible media player for Android                                      |
| [material-components-android](https://github.com/material-components/material-components-android) | Modular and customizable Material Design UI components for Android          |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization/)                         | Kotlin multiplatform / multi-format serialization                           |
| [kotlinx.coroutine](https://github.com/Kotlin/kotlinx.coroutines)                                 | Library support for Kotlin coroutines                                       |

</details>

其他资源：

* [阿里巴巴IconFont](https://www.iconfont.cn/)

* [酷安@沉默_9527](https://www.coolapk.com/u/230844) 本APP图标作者
