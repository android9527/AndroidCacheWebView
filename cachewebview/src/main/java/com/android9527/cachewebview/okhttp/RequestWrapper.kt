package com.android9527.cachewebview.okhttp

import android.annotation.SuppressLint
import android.support.annotation.StringDef
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.util.*

/**
 * Created by chenfeiyue on 2018/6/7.
 * Description ：RequestWrapper OkHttpClient请求封装类
 */
class RequestWrapper {

    var url: String = ""
    @Method.MethodDef
    var method: String? = null

    var requestBody: RequestBody? = null

    /**
     * header信息
     */
    var header: MutableMap<String, String?>? = null
    /**
     * 参数列表
     */
    var parameters: MutableMap<String, String>? = null

    /**
     * 是否异步请求，默认true
     */
    var asyncExec = true

    internal var _success: (Response?) -> Unit = { }
    internal var _fail: (Throwable) -> Unit = {}

    fun onSuccess(onSuccess: (Response?) -> Unit) {
        this._success = onSuccess
    }

    fun onError(onError: (Throwable) -> Unit) {
        this._fail = onError
    }
}

class Method {

    companion object {
        const val GET = "GET"
        const val POST = "POST"
        const val HEAD = "HEAD"
        const val DELETE = "DELETE"
        const val PUT = "PUT"
        const val PATCH = "PATCH"
    }

    @StringDef(GET, POST, HEAD, DELETE, PUT, PATCH)
    @kotlin.annotation.Retention
    annotation class MethodDef
}

fun http(init: RequestWrapper.() -> Unit) {
    val wrap = RequestWrapper()

    wrap.init()

    executeForResult(wrap)
}

@SuppressLint("CheckResult")
private fun executeForResult(wrap: RequestWrapper) {

    val flowable = Flowable.create<Response>(
            { e ->
                onExecute(wrap)?.let {
                    e.onNext(it)
                }
            },
            BackpressureStrategy.BUFFER)

    val anotherFlowable = if (wrap.asyncExec) {
        flowable.subscribeOn(Schedulers.io())
    } else {
        flowable
    }
    anotherFlowable.subscribe(
            { response ->
                wrap._success(response)
            },

            { exception ->
                wrap._fail(exception)
            }
    )
}

private fun onExecute(wrap: RequestWrapper): Response? {

    val request: Request?
    val builder = Request.Builder().url(wrap.url)
    var headers: Headers? = null
    if (wrap.header != null && !wrap.header!!.isNotEmpty()) {
        headers = Headers.of(wrap.header!!)
    }
    if (headers != null) {
        builder.headers(headers)
    }

    var formBody: RequestBody? = null

    when (wrap.method) {
        Method.GET, Method.HEAD -> {

            val httpBuilder = HttpUrl.parse(wrap.url)!!.newBuilder()
            if (wrap.parameters != null) {
                for (param in wrap.parameters!!.entries) {
                    httpBuilder.addQueryParameter(param.key, param.value)
                }
            }
            builder.url(httpBuilder.build())
        }

        else -> {
            // Initialize Builder (not RequestBody)
            if (wrap.parameters != null /*&& wrap.queryMap!!.isNotEmpty()*/) {
                val bodyBuilder = FormBody.Builder()
                // Add Params to Builder
                for (entry in wrap.parameters!!.entries) {
                    bodyBuilder.add(entry.key, entry.value)
                }
                // Create RequestBody
                formBody = bodyBuilder.build()
            }
        }
    }
    request = builder.method(wrap.method, formBody).build()
//    val http = OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).build()
    val response = OkHttp3ClientUtil.getOkHttpClient().newCall(request).execute()
    return response
}


private fun main(args: Array<String>) {
    testGet()
}

private fun testGet(): String? {
    var result: String? = null
    http {
        url = "https://www.baidu.com"
        method = Method.GET
        parameters = HashMap()
        parameters!!.put("key", "value")
        asyncExec = false
        onSuccess { response ->
            println(response?.headers()?.toMultimap())
            result = response?.body()?.string()
        }

        onError { e ->
            e.printStackTrace()
        }
    }
    println("---------->" + result)
    return result
}
