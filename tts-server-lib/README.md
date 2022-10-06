这个目录是包装的 tts-server-go, 以方便Android调用。

# Build

```
go install golang.org/x/mobile/cmd/gomobile@latest
gomobile init
go get golang.org/x/mobile/bind

另外，还需设置ANDROID_NDK_HOME变量(如果在Android Studio里安装了NDK就直接设置ANDROID_HOME为SDK目录即可)
然后构建：
gomobile bind -v -target="android/arm,android/arm64" -androidapi=19

最后将 tts_server_lib.aar 移动到 app/libs 目录下
```
