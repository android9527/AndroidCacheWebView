package com.android9527.cachewebview.bean

import com.android9527.cachewebview.util.copy2InputStream
import com.android9527.cachewebview.util.copyToOutputStream
import com.android9527.cachewebview.util.map2Str
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Created by chenfeiyue on 2018/6/5.
 * Description ：
 */
class StreamCacheValue : CacheValue {

    var stream: InputStream? = null
    var inputStreamSize = -1
    var headerSize = -1
    var headerMap: Map<String, String?>? = null

    override fun getContentSize(): Int {

        if (headerSize != -1) {
            return inputStreamSize + headerSize
        }
        if (headerMap != null || !headerMap!!.isEmpty()) {
            headerSize = map2Str(headerMap)?.toByteArray()?.size ?: 0
        }
        return inputStreamSize + headerSize
    }

    override fun getInputStream(): InputStream? {
        return stream
    }

    override fun getHeader(): Map<String, String?>? {
        return headerMap
    }

    companion object {
        @JvmStatic
        fun copy(responseHeaders: Map<String, String?>?, outputStream: ByteArrayOutputStream?): StreamCacheValue {
            val cacheValue = StreamCacheValue()
            cacheValue.headerMap = responseHeaders
            val streamArray = outputStream?.toByteArray()
            cacheValue.inputStreamSize  = streamArray?.size ?: 0
            cacheValue.stream = ByteArrayInputStream(streamArray)
            return cacheValue
        }
        @JvmStatic
        fun copyOf(value: StreamCacheValue?): StreamCacheValue? {
            var cacheValue: StreamCacheValue? = null
            if (value?.stream != null) {
                cacheValue = StreamCacheValue()
                cacheValue.headerMap = value.headerMap
                cacheValue.inputStreamSize = value.inputStreamSize
                cacheValue.headerSize = value.headerSize
                val outputStream = value.stream?.copyToOutputStream()
                value.stream = outputStream?.copy2InputStream()
                cacheValue.stream = outputStream?.copy2InputStream()
            }
            return cacheValue
        }
    }
}