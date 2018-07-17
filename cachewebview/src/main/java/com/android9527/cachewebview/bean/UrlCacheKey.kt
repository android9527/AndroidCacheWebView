package com.android9527.cachewebview.bean

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by chenfeiyue on 2018/6/5.
 * Description ：
 */
class UrlCacheKey : CacheKey {

    var mUrlKey: String
    var mSafeKey: String? = null

    constructor(url: String) {
        mUrlKey = url
    }

    @Volatile
    private var cacheKeyBytes: ByteArray? = null

    override fun updateDiskCacheKey(messageDigest: MessageDigest?) {
        messageDigest?.update(getCacheKeyBytes())
    }

    private fun getCacheKeyBytes(): ByteArray? {
        if (cacheKeyBytes == null) {
            cacheKeyBytes = getCacheKey()?.toByteArray(CacheKey.CHARSET)
        }
        return cacheKeyBytes
    }

    override fun getCacheKey(): String? {
        if (mSafeKey.isNullOrEmpty()) {
            mSafeKey = hashKeyForDisk(mUrlKey)
        }
        return mSafeKey
    }

    override fun getUrl(): String? {
        return mUrlKey
    }

    /**
     * 把缓存的key进行hash一下，得到的值将作为缓存文件的文件名，图片URL中可能包含一些特殊字符，在命名文件时是不合法。
     * 一个简单的做法就是将图片的URL进行MD5编码，编码后的字符串肯定是唯一的，并且只会包含0-F这样的字符，符合文件的命名规则
     *
     * @param key
     * @return
     */
    private fun hashKeyForDisk(key: String): String {
        val cacheKey = try {
            val mDigest = MessageDigest.getInstance("MD5")
            mDigest.update(key.toByteArray())
            bytesToHexString(mDigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            key.hashCode().toString()
        }
        return cacheKey
    }

    private fun bytesToHexString(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (aByte in bytes) {
            val hex = Integer.toHexString(0xFF and aByte.toInt())
            if (hex.length == 1) {
                sb.append('0')
            }
            sb.append(hex)
        }
        return sb.toString()
    }

    private var hashCode = 0

    override fun hashCode(): Int {
        if (hashCode == 0) {
            hashCode = mUrlKey.hashCode()
        }
        return hashCode
    }

    override fun equals(o: Any?): Boolean {
        if (o is UrlCacheKey) {
            return mUrlKey == o.mUrlKey
        }
        return false
    }

}