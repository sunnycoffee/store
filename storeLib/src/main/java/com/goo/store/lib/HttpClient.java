package com.goo.store.lib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.security.cert.X509Certificate;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
class HttpClient {

    private static final long CONNECT_TIMEOUT_DEFAULT = 6 * 1000;
    private static final long READ_TIMEOUT_DEFAULT = 30 * 1000;
    private static final long WRITE_TIMEOUT_DEFAULT = 30 * 1000;

    private OkHttpClient mClient;
    private Retrofit mRetrofit;
    private boolean mDebug;

    static final class Holder {
        static final HttpClient client = new HttpClient();
    }

    private HttpClient() {

    }

    public static HttpClient getInstance() {
        return Holder.client;
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public HttpClient init(String baseUrl) {
        return init(null, baseUrl);
    }

    public HttpClient init(OkHttpClient client, String baseUrl) {
        this.mClient = client;
        if (mClient == null) mClient = createHttpClient();

        Retrofit.Builder builder = new Retrofit.Builder().client(mClient);
        builder.addConverterFactory(GsonConverterFactory.create());
        builder.addCallAdapterFactory(RxJava2CallAdapterFactory.create());
        builder.baseUrl(baseUrl);
        mRetrofit = builder.build();
        return this;
    }

    private OkHttpClient createHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECT_TIMEOUT_DEFAULT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT_DEFAULT, TimeUnit.MILLISECONDS)
                .writeTimeout(WRITE_TIMEOUT_DEFAULT, TimeUnit.MILLISECONDS)
                .addInterceptor(createLoggerInterceptor("http-plain"))
                .addInterceptor(new EncryptionInterceptor())
                .addInterceptor(createLoggerInterceptor("http-cipher"))
                .retryOnConnectionFailure(true);

        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(socketFactory, new UnSafeTrustManager());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.build();
    }

    private Interceptor createLoggerInterceptor(String tag) {
        HttpLoggingInterceptor logger = new HttpLoggingInterceptor(new Logger(tag));
        logger.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logger;
    }

    private static Context getContext() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Object thread = activityThread.getMethod("currentActivityThread").invoke(null);
            Object app = activityThread.getMethod("getApplication").invoke(thread);
            if (app == null) {
                throw new NullPointerException("u should init first");
            } else {
                return (Context) app;
            }
        } catch (Exception ignore) {
            throw new NullPointerException("u should init first");
        }
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }


    public <T> T createService(Class<T> clazz) {
        return mRetrofit.create(clazz);
    }

    public static <T> ObservableTransformer<T, T> switcher() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    class Logger implements HttpLoggingInterceptor.Logger {

        private final Executor logExecutor;
        private final String tag;

        Logger(String tag) {
            this.tag = tag;
            logExecutor = Executors.newSingleThreadExecutor();
        }

        @Override
        public void log(String message) {
            if (mDebug) logExecutor.execute(() -> {
                Log.d(tag, message);
            });
        }
    }

    //空实现ssl
    static class UnSafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

}
