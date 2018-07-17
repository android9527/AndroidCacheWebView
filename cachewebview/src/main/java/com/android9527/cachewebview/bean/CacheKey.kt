package com.android9527.cachewebview.bean

import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * An interface that uniquely identifies some put of data. Implementations must implement [ ][Object.equals] and [Object.hashCode]. Implementations are generally expected to
 * add all uniquely identifying information used in in [java.lang.Object.equals]} and
 * [Object.hashCode]} to the given [java.security.MessageDigest] in [ ][.updateDiskCacheKey]}, although this requirement is not as strict
 * for partial cache key signatures.
 */
interface CacheKey {

    fun getCacheKey(): String?

    fun getUrl(): String?

    /**
     * Adds all uniquely identifying information to the given digest.
     *
     *
     *
     *  Note - Using [java.security.MessageDigest.reset] inside of this method will result
     * in undefined behavior.
     */
    fun updateDiskCacheKey(messageDigest: MessageDigest?)

    override fun equals(o: Any?): Boolean

    override fun hashCode(): Int

    companion object {
        val STRING_CHARSET_NAME = "UTF-8"
        val CHARSET = Charset.forName(STRING_CHARSET_NAME)
    }
}
