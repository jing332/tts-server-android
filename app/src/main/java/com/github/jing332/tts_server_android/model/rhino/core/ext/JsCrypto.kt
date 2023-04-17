@file:Suppress("unused")

package com.github.jing332.tts_server_android.model.rhino.core.ext

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.HexUtil
import cn.hutool.crypto.symmetric.SymmetricCrypto
import com.github.jing332.tts_server_android.constant.AppConst.dateFormat
import com.github.jing332.tts_server_android.utils.EncoderUtils
import com.github.jing332.tts_server_android.utils.MD5Utils
import java.text.SimpleDateFormat
import java.util.*

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


    fun base64DecodeToBytes(str: String): ByteArray {
        return Base64.decode(str)
    }

    fun base64DecodeToBytes(bytes: ByteArray): ByteArray {
        return Base64.decode(bytes)
    }


    fun base64Decode(str: String, flags: Int): String {
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
    }

    fun base64Encode(src: ByteArray): String? {
        return EncoderUtils.base64Encode(src)
    }

    fun base64Encode(src: ByteArray, flags: Int = android.util.Base64.NO_WRAP): String? {
        return EncoderUtils.base64Encode(src, flags)
    }


    /* HexString 解码为字节数组 */
    fun hexDecodeToByteArray(hex: String): ByteArray? {
        return HexUtil.decodeHex(hex)
    }

    /* hexString 解码为utf8String*/
    fun hexDecodeToString(hex: String): String? {
        return HexUtil.decodeHexStr(hex)
    }

    /* utf8 编码为hexString */
    fun hexEncodeToString(utf8: String): String? {
        return HexUtil.encodeHexStr(utf8)
    }

    /**
     * 格式化时间
     */
    fun timeFormatUTC(time: Long, format: String, sh: Int): String? {
        val utc = SimpleTimeZone(sh, "UTC")
        return SimpleDateFormat(format, Locale.getDefault()).run {
            timeZone = utc
            format(Date(time))
        }
    }

    /**
     * 时间格式化
     */
    fun timeFormat(time: Long): String {
        return dateFormat.format(Date(time))
    }

    /**
     * utf8编码转gbk编码
     */
    fun utf8ToGbk(str: String): String {
        val utf8 = String(str.toByteArray(charset("UTF-8")))
        val unicode = String(utf8.toByteArray(), charset("UTF-8"))
        return String(unicode.toByteArray(charset("GBK")))
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