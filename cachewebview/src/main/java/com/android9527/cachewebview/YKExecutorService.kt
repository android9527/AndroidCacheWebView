package com.android9527.cachewebview

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by chenfeiyue on 2018/6/6.
 * Description ：
 */
object ExecutorServiceUtil {
    private var mExecutorService: ExecutorService? = null

    @JvmStatic
    @Synchronized
    fun getExecutorService(): ExecutorService {
        if (mExecutorService == null || mExecutorService!!.isShutdown) {
            initExecutorService()
        }
        return mExecutorService!!
    }

    private fun initExecutorService() {
        mExecutorService = Executors.newFixedThreadPool(3)
    }
}
