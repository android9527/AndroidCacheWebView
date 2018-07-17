package com.android9527.cachewebview

import java.io.InputStream

/**
 * Created by chenfeiyue on 2018/6/4.
 * Description ：ResourceInputStream
 */
internal class WebResourceInputStream(val innerInputStream: InputStream?, var headers: Map<String, String>?)
