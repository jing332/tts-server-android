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
