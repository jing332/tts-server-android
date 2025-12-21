# GitHub Secrets 更新指南

## 签名问题修复方案

### 当前问题
GitHub Actions构建失败，错误：`Failed to read key *** from store "/home/runner/work/tts-server-android/tts-server-android/key.p12": Tag number over 30 is not supported`

### 解决方案

1. **更新 GitHub Secrets 中的 KEY_STORE**
   - 打开 GitHub 仓库设置：`https://github.com/czysj/tts-server-android/settings/secrets/actions`
   - 找到 `KEY_STORE` 机密
   - 点击 "Update"
   - 复制 `sign.p12.base64` 文件的完整内容（**无换行符**）
   - 粘贴到 `KEY_STORE` 机密值中
   - 保存更改

2. **验证签名文件生成命令**
   ```bash
   # 查看当前签名文件的基本信息
   keytool -list -v -keystore sign.p12 -storetype PKCS12 -storepass sc6597 | head -20
   ```

3. **检查 GitHub Actions 工作流**
   - 确保工作流正确解码 KEY_STORE 到 key.p12
   - 当前配置：`echo ${{ secrets.KEY_STORE }} | base64 --decode > $GITHUB_WORKSPACE/key.p12`

4. **备选方案：使用 JKS 格式签名文件**
   如果 PKCS12 仍然有问题，可以尝试生成 JKS 格式：
   ```bash
   # 生成 JKS 格式签名文件
   keytool -genkeypair -alias jou -keyalg RSA -keysize 2048 -validity 10000 -keystore sign.jks -storepass sc6597 -keypass sc6597 -dname "CN=jou, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=CN"
   
   # 转换为 Base64
   base64 sign.jks > sign.jks.base64
   ```

### 步骤

1. ✅ 复制 `sign.p12.base64` 内容到 GitHub Secrets `KEY_STORE`
2. ✅ 重新运行 GitHub Actions 工作流
3. ❌ 避免使用过高版本 JDK 生成签名文件
4. ✅ 确保使用 JDK 17 生成签名文件

### 已生成的签名文件信息
- **文件**：`sign.p12`
- **类型**：PKCS12
- **别名**：jou
- **密码**：sc6597
- **生成工具**：JDK 17

## 紧急修复方案

如果上述方法仍然失败，可以尝试：

1. **修改构建配置，使用不同的签名方式**
2. **临时禁用签名检查**（仅用于测试）
3. **使用不同版本的 Android Gradle Plugin**

请按照上述步骤更新 GitHub Secrets，然后重新运行构建。
