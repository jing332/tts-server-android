@file:Suppress("unused")

package com.github.jing332.tts_server_android.help.plugin.ext

import cn.hutool.core.codec.Base64
import cn.hutool.crypto.symmetric.SymmetricCrypto
import io.legado.app.utils.MD5Utils

interface JsCrypto {
    fun md5Encode(str: String): String {
        return MD5Utils.md5Encode(str)
    }

    fun md5Encode16(str: String): String {
        return MD5Utils.md5Encode16(str)
    }

    //******************对称加密解密************************//

    /**
     * 在js中这样使用
     * java.createSymmetricCrypto(transformation, key, iv).decrypt(data)
     * java.createSymmetricCrypto(transformation, key, iv).decryptStr(data)

     * java.createSymmetricCrypto(transformation, key, iv).encrypt(data)
     * java.createSymmetricCrypto(transformation, key, iv).encryptBase64(data)
     * java.createSymmetricCrypto(transformation, key, iv).encryptHex(data)
     */

    /* 调用SymmetricCrypto key为null时使用随机密钥*/
    fun createSymmetricCrypto(
        transformation: String,
        key: ByteArray?,
        iv: ByteArray?
    ): SymmetricCrypto {
        val symmetricCrypto = SymmetricCrypto(transformation, key)
        return if (iv != null && iv.isNotEmpty()) symmetricCrypto.setIv(iv) else symmetricCrypto
    }

    fun createSymmetricCrypto(
        transformation: String,
        key: ByteArray
    ): SymmetricCrypto = createSymmetricCrypto(transformation, key, null)

    fun createSymmetricCrypto(
        transformation: String,
        key: String
    ): SymmetricCrypto = createSymmetricCrypto(transformation, key, null)

    fun createSymmetricCrypto(
        transformation: String,
        key: String,
        iv: String?
    ): SymmetricCrypto =
        createSymmetricCrypto(transformation, key.encodeToByteArray(), iv?.encodeToByteArray())


    /**
     * js实现base64解码,不能删
     */
    fun base64Decode(str: String?): String {
        return Base64.decodeStr(str)
    }

    fun base64Decode(str: String?, charset: String): String {
        return Base64.decodeStr(str, charset(charset))
    }

  /*  fun base64Decode(str: String, flags: Int): String {
        return EncoderUtils.base64Decode(str, flags)
    }

    fun base64DecodeToByteArray(str: String?): ByteArray? {
        if (str.isNullOrBlank()) {
            return null
        }
        return EncoderUtils.base64DecodeToByteArray(str, 0)
    }

    fun base64DecodeToByteArray(str: String?, flags: Int): ByteArray? {
        if (str.isNullOrBlank()) {
            return null
        }
        return EncoderUtils.base64DecodeToByteArray(str, flags)
    }

    fun base64Encode(str: String): String? {
        return EncoderUtils.base64Encode(str, 2)
    }

    fun base64Encode(str: String, flags: Int): String? {
        return EncoderUtils.base64Encode(str, flags)
    }*/

}