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
KEY_STORE=<请将 sign.p12.base64 文件内容粘贴到此处>
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