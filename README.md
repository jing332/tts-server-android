![MIT](https://img.shields.io/badge/license-MIT-green)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/release.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/release.yml)
[![CI](https://github.com/jing332/tts-server-android/actions/workflows/test.yml/badge.svg)](https://github.com/jing332/tts-server-android/actions/workflows/test.yml)
![GitHub release (latest by date)](https://img.shields.io/github/downloads/jing332/tts-server-android/latest/total)

# 介绍
这是 [tts-server-go](https://github.com/jing332/tts-server-go) 的Android版本。使用Kotlin开发，通过 [Gomobile](https://pkg.go.dev/golang.org/x/mobile/cmd/gomobile) 将Go编译为Lib以供Android APP调用. 关于本项目的Go Lib,见 [tts-server-lib](./tts-server-lib).

<details>
  <summary>点击展开查看截图</summary>
  
> 左图为服务转发日志界面，用于阅读APP的网络朗读。<br>
右图为系统TTS配置界面，可被使用系统TTS的APP调用。

  <img src="./images/Screenshot_Main.png" height="150px">
  <img src="./images/Screenshot_SysTTS.png" height="150px">
  
</details>
