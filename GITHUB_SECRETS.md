# GitHub Secrets 配置指南

## 完整仓库机密格式（一键复制）

以下是需要在 GitHub 仓库设置中添加的所有机密，直接复制对应的键值对即可：

### 1. 签名配置
```
ALIAS_NAME=jou
ALIAS_PASSWORD=sc6597
KEY_PASSWORD=sc6597
```

### 2. 签名文件（Base64 编码）
```
KEY_STORE=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
```

### 3. GitHub API Token
```
TOKEN=<请输入您的 GitHub API Token>
```

## 获取签名文件 Base64 编码

1. 确保项目根目录下存在 `sign.p12` 文件
2. 查看生成的 Base64 编码：
   ```bash
   cat sign.p12.base64
   ```
3. 将输出的完整内容（不包含任何换行符）粘贴到 `KEY_STORE` 机密值中

## GitHub Secrets 设置步骤

1. 打开 GitHub 仓库页面
2. 点击 "Settings" → "Secrets and variables" → "Actions"
3. 点击 "New repository secret"
4. 逐个添加上述机密，确保键名完全一致
5. 保存所有机密后，重新运行 GitHub Actions 工作流

## 本地构建说明

1. 确保 JDK 17 已正确安装
2. 检查 `local.properties` 文件中的 JAVA_HOME 配置
3. 运行构建命令：
   ```bash
   ./gradlew assembleDevRelease --no-daemon
   ```

## 已实现的功能

1. ✅ **TTS 转发器自动启动**：在 `App.onCreate()` 中使用 `GlobalScope.launch` 自动启动 `SysTtsForwarderService`
2. ✅ **存储权限配置**：已在 `AndroidManifest.xml` 中添加
   - `READ_EXTERNAL_STORAGE`
   - `WRITE_EXTERNAL_STORAGE`
   - `MANAGE_EXTERNAL_STORAGE`
   - `requestLegacyExternalStorage="true"`
3. ✅ **签名配置优化**：
   - 禁用了 v3 签名以避免 "Tag number over 30" 错误
   - 使用 PKCS12 格式签名文件
   - 修复了 GitHub Actions 中的 KEY_PATH 路径
4. ✅ **多渠道打包**：支持原版和 DEV 共存版

## 解决的构建错误

- ✅ JDK 版本兼容性问题：指定使用 JDK 17
- ✅ 签名文件格式错误：从 JKS 转换为 PKCS12
- ✅ Tag number over 30 错误：禁用 v3 签名
- ✅ 签名文件路径错误：修正 GitHub Actions 中的 KEY_PATH
- ✅ PowerShell 语法错误：使用正确的命令格式
- ✅ Git push SSL 错误：配置 SSL 验证

## 注意事项

1. 确保 GitHub Token 具有 `repo` 和 `workflow` 权限
2. 签名文件不要提交到版本控制中
3. 定期更新签名文件，确保安全性
4. 构建前检查 `local.properties` 文件配置是否正确

## 构建命令

```bash
# 构建 DEV 共存版（推荐）
./gradlew assembleDevRelease --no-daemon

# 构建原版
./gradlew assembleAppRelease --no-daemon

# 构建 Debug 版
./gradlew assembleAppDebug --no-daemon
```


