package com.android9527.cachewebview.bean

import java.io.InputStream

/**
 * Created by chenfeiyue on 2018/6/5.
 * Description ：
 */
interface CacheValue {

    fun getHeader(): Map<String, String?>?

    fun getInputStream(): InputStream?

    fun getContentSize():Int
}