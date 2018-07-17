package com.android9527.cachewebview.okhttp;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by chenfeiyue on 2018/6/7.
 * Description ：OkHttp3ClientUtil
 */
public class OkHttp3ClientUtil {
    private static OkHttpClient mClient = null;

    private static void initClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .hostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .connectTimeout(20 * 1000, TimeUnit.MILLISECONDS)
                .readTimeout(20 * 1000, TimeUnit.MILLISECONDS);
        HttpLoggingInterceptor.Level level = /*ConstValue.DEBUG_MODE ? HttpLoggingInterceptor.Level.HEADERS : */HttpLoggingInterceptor.Level.BODY;

//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new MyLogger()).setLevel(level);
//
//        builder.addInterceptor(loggingInterceptor);
        try {
            SSLSocketFactory socketFactory = getSslSocketFactory();
            assert socketFactory != null;
            builder.sslSocketFactory(socketFactory, getTrustManager());

        } catch (Exception e) {
            e.printStackTrace();
        }
        mClient = builder.build();
    }

    private static String[] javaNames(List<CipherSuite> cipherSuites) {
        String[] result = new String[cipherSuites.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cipherSuites.get(i).javaName();
        }
        return result;
    }

    public void get(String url) {
        Request request = new Request.Builder()
                .url(url)
//                .addHeader("Accept-Encoding", "gzip")
                .build();
        try {
            mClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    byte[] result = response.body() != null ? response.body().bytes() : new byte[0];

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SSLSocketFactory getSslSocketFactory() {
        try {
            return YKTLSSocketFactory.getSslSocketFactory();
//            return new YKTLSSocketFactory(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static X509TrustManager getTrustManager() {
        try {
//            return YKTLSSocketFactory.getTrustManagers();
            return new UnSafeTrustManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized static OkHttpClient getOkHttpClient() {
        if (mClient == null) {
            initClient();
        }
        return mClient;
    }

    private static class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    static class MyLogger implements HttpLoggingInterceptor.Logger {

        @SuppressWarnings("HardCodedStringLiteral")
        @Override
        public void log(String message) {
            System.out.println("OKHTTP3 ---->" + message);
        }
    }
}
