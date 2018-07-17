package com.android9527.cachewebview.util;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Created by Wbaokang-PC on 2016/3/8.
 * modify by chenfeiyue
 */
public class YKLogUtil {

    public static void w(String tag, Object msg) { // 警告信息
        writeLog(tag, msg.toString(), 'w', "", null);
    }

    public static void e(String tag, Object msg) { // 错误信息
        writeLog(tag, msg.toString(), 'e', "", null);
    }

    public static void d(String tag, Object msg) {// 调试信息
        writeLog(tag, msg.toString(), 'd', "", null);
    }

    public static void i(String tag, Object msg) {//
        writeLog(tag, msg.toString(), 'i', "", null);
    }

    public static void v(String tag, Object msg) {
        writeLog(tag, msg.toString(), 'v', "", null);
    }

    public static void w(String tag, String text) {
        writeLog(tag, text, 'w', "", null);
    }

    public static void e(String tag, String text) {
        writeLog(tag, text, 'e', "", null);
    }

    public static void json(String tag, String className, String text) {
        writeLog(tag, text, 'j', "", className);
    }

    public static void d(String tag, String text) {
        writeLog(tag, text, 'd', "", null);
    }

    public static void i(String tag, String text) {
        writeLog(tag, text, 'i', "", null);
    }

    public static void v(String tag, String text) {
        writeLog(tag, text, 'v', "", null);
    }

    public static void w(String tag, Object msg, String fileName) { // 警告信息
        writeLog(tag, msg.toString(), 'w', fileName, null);
    }

    public static void e(String tag, Object msg, String fileName) { // 错误信息
        writeLog(tag, msg.toString(), 'e', fileName, null);
    }

    public static void d(String tag, Object msg, String fileName) {// 调试信息
        writeLog(tag, msg.toString(), 'd', fileName, null);
    }

    public static void i(String tag, Object msg, String fileName) {//
        writeLog(tag, msg.toString(), 'i', fileName, null);
    }

    public static void v(String tag, Object msg, String fileName) {
        writeLog(tag, msg.toString(), 'v', fileName, null);
    }

    public static void w(String tag, String text, String fileName) {
        writeLog(tag, text, 'w', fileName, null);
    }

    public static void e(String tag, String text, String fileName) {
        writeLog(tag, text, 'e', fileName, null);
    }

    public static void d(String tag, String text, String fileName) {
        writeLog(tag, text, 'd', fileName, null);
    }

    public static void i(String tag, String text, String fileName) {
        writeLog(tag, text, 'i', fileName, null);
    }

    public static void v(String tag, String text, String fileName) {
        writeLog(tag, text, 'v', fileName, null);
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag
     * @param msg
     * @param level
     * @return void
     * @since v 1.0
     */

    private static void writeLog(String tag, String msg, char level,
                                 String fileName, String className) {
            if ('e' == level) {
                Log.e(tag, getMsgWithLineNumber(msg));
            } else if ('w' == level) {
                Log.w(tag, getMsgWithLineNumber(msg));
            } else if ('d' == level) {
                Log.d(tag, getMsgWithLineNumber(msg));
            } else if ('i' == level) {
                Log.i(tag, getMsgWithLineNumber(msg));
            } else {
                Log.v(tag, getMsgWithLineNumber(msg));
            }
    }


    private static String classname;

    private static ArrayList<String> methods;

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String TAG = "YKLogUtil";

    static {
        classname = YKLogUtil.class.getName();
        methods = new ArrayList<>();

        Method[] ms = YKLogUtil.class.getDeclaredMethods();
        for (Method m : ms) {
            methods.add(m.getName());
        }
    }


    /**
     * 获取带行号的日志信息内容。
     *
     * @param msg 日志内容。
     * @return 带行号的日志信息内容。
     */
    private static String getMsgWithLineNumber(String msg) {
        try {
            for (StackTraceElement st : (new Throwable()).getStackTrace()) {
                if (!classname.equals(st.getClassName()) && !methods.contains(st.getMethodName())) {
//                    int index = st.getClassName().lastIndexOf(".") + 1;
//                    String tag = st.getClassName().substring(index);
                    //noinspection HardCodedStringLiteral
                    return "line + " + st.getLineNumber() + "----->" + st.getMethodName() + "(): " + msg;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msg;
    }
}
