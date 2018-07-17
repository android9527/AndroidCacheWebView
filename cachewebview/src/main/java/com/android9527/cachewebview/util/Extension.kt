package com.android9527.cachewebview.util

import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.android9527.cachewebview.CacheWebViewClient
import com.android9527.cachewebview.WebResourceInputStream
import com.android9527.cachewebview.bean.CacheKey
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

/**
 * Created by chenfeiyue on 2018/6/5.
 * Description ：kotlin Extension
 */


/**
 * 从HttpURLConnection获取Response Header
 */
fun getResponseHeader(maps: Map<String, List<String>>?): HashMap<String, String> {
    val map = HashMap<String, String>()

    if (maps == null || maps.isEmpty()) {
        return map
    }

    for (entry in maps.entries) {
        val key = entry.key ?: continue
        val values = entry.value as List<String>?
        if (values != null && values.isNotEmpty()) {
            map[entry.key] = values[0]
        }
    }
    return map
}

fun InputStream.copy(): InputStream? {
    val baos = this.copyToOutputStream()

    // 打开一个新的输入流
    return ByteArrayInputStream(baos?.toByteArray())
}

fun ByteArrayOutputStream.copy2InputStream() : InputStream? {
    // 打开一个新的输入流
    return ByteArrayInputStream(this.toByteArray())
}

/**
 * InputStream to ByteArrayOutputStream
 */
fun InputStream.copyToOutputStream(): ByteArrayOutputStream? {
    val baos = ByteArrayOutputStream()

    val buffer = ByteArray(1024)
    var len: Int = this.read(buffer)
    while (len != -1) {
        baos.write(buffer, 0, len)
        len = this.read(buffer)
    }
    baos.flush()
    return baos
}

fun inputStream2Str(inputStream: InputStream?): String? {
    val sb = StringBuffer()
    if (inputStream == null) {
        return sb.toString()
    }
    try {
        val bufferedInputStream = BufferedInputStream(inputStream)
        val buffer = ByteArray(1024)
        var len = bufferedInputStream.read(buffer)
        while (len != -1) {
            sb.append(String(buffer, 0, len))
            len = bufferedInputStream.read(buffer)
        }
        bufferedInputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return sb.toString()

}

fun getFileExtensionFromUrl(url: String): String {
    var url = url.toLowerCase()
    if (!TextUtils.isEmpty(url)) {
        val fragment = url.lastIndexOf('#')
        if (fragment > 0) {
            url = url.substring(0, fragment)
        }
        val query = url.lastIndexOf('?')
        if (query > 0) {
            url = url.substring(0, query)
        }

        val filenamePos = url.lastIndexOf('/')
        val filename = if (0 <= filenamePos) url.substring(filenamePos + 1) else url

        // if the filename contains special characters, we don't
        // consider it valid for our matching purposes:
        if (!filename.isEmpty()) {
            val dotPos = filename.lastIndexOf('.')
            if (0 <= dotPos) {
                return filename.substring(dotPos + 1)
            }
        }
    }

    return ""
}

fun getMimeTypeFromExtension(extension: String): String? {
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
}

fun map2Str(map: Map<String, String?>?): String? {
    try {
        val result = JSONObject(map)
        return result.toString()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return ""
}

fun str2Map(jsonString: String?): HashMap<String, String?>? {
    val result = HashMap<String, String?>()
    try {
        val jsonObject = JSONObject(jsonString)
        val iterator = jsonObject.keys()
        var key: String?
        var value: String?
        while (iterator.hasNext()) {
            try {
                key = iterator.next() as String
                value = jsonObject.getString(key)
                result.put(key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return result
}


/**
 * 网络请求下载资源
 */
private fun httpRequest(client: CacheWebViewClient, key: CacheKey): WebResourceInputStream? {

    try {
        YKLogUtil.e("WebViewCache", "start download" + key.getUrl() + "\n" + Thread.currentThread().name)
        val urlRequest = URL(key.getUrl())

        val httpURLConnection = urlRequest.openConnection() as HttpURLConnection
        httpURLConnection.requestMethod = "GET"
        httpURLConnection.useCaches = false
        httpURLConnection.connectTimeout = 5000
        httpURLConnection.readTimeout = 10000

        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }


        val header = client.getHeader(key.getUrl())
        if (header != null) {
            for (entry in header.entries) {
                httpURLConnection.setRequestProperty(entry.key, entry.value)
            }
        }

        httpURLConnection.setRequestProperty("Origin", client.getOriginUrl())
        httpURLConnection.setRequestProperty("Referer", client.getRefererUrl())
        httpURLConnection.setRequestProperty("User-Agent", client.getUserAgent())

        httpURLConnection.connect()
        val responseCode = httpURLConnection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val inputStream = httpURLConnection.inputStream

            val outputStream = inputStream.copyToOutputStream()
            val responseHeaders = getResponseHeader(httpURLConnection.headerFields)

//            cacheResource(key, outputStream, responseHeaders)

            return WebResourceInputStream(outputStream?.copy2InputStream(), responseHeaders)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}