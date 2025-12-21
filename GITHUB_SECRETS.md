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

