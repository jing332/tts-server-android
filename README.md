![MIT](https://img.shields.io/badge/license-MIT-green)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/release.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/release.yml)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/test.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/test.yml)

![GitHub release](https://img.shields.io/github/downloads/jing332/tts-server-android/total)
![GitHub release (latest by date)](https://img.shields.io/github/downloads/jing332/tts-server-android/latest/total)

# 介绍 
本APP起初为阅读APP的网络朗读所用，在原有基础上，现已支持系统TTS，并可以自定义HttpTTS。

这是 [tts-server-go](https://github.com/jing332/tts-server-go) 的Android版本。使用Kotlin开发，通过 [Gomobile](https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile) 将Go编译为Lib以供Android APP调用. 关于本项目的Go Lib,见 [tts-server-lib](./tts-server-lib).

<details>
  <summary>点击展开查看截图</summary>
  
> 左图为服务转发日志界面，用于阅读APP的网络朗读。<br>
右图为系统TTS配置界面，可被使用系统TTS的APP调用。

  <img src="./images/Screenshot_Main.png" height="150px">
  <img src="./images/Screenshot_SysTTS.png" height="150px">
  
</details>

# 下载

[稳定版(Release)](https://github.com/jing332/tts-server-android/relaease) |
[开发版(Actions)](https://github.com/jing332/tts-server-android/actions)


# 感谢

开源项目：
|  Android项目   | 微软TTS实现  |
|  ----  | ----  |
| [gedoor/legado](https://github.com/gedoor/legado)        | [wxxxcxx/ms-ra-forwarder](https://github.com/wxxxcxx/ms-ra-forwarder) |
| [ag2s20150909/TTS](https://github.com/ag2s20150909/TTS)  | [litcc/tts-server](https://github.com/litcc/tts-server) |
| [benjaminwan/ChineseTtsTflite](https://github.com/benjaminwan/ChineseTtsTflite)| [asters1/tts](https://github.com/asters1/tts) |
| [2dust/v2rayNG](https://github.com/2dust/v2rayNG) |

其他资源：
* [阿里巴巴iconfont](https://www.iconfont.cn/)
 
* [酷安@沉默_9527](https://www.coolapk.com/u/230844) 本APP图标作者
