package com.kii.kiiallconnect;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.Looper;

public class RequestExecutor {
    private ThreadPoolExecutor tpe;

    public RequestExecutor() {
        tpe = new ThreadPoolExecutor(10, Integer.MAX_VALUE, Long.MAX_VALUE,
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10));
    }

    public void execute(final HttpUriRequest request,
            final SocialAPICallback callback) {
        Callable<String> task = new Callable<String>() {

            @Override
            public String call() throws Exception {
                return httpRequest(request);
            }
        };
        Future<String> future = tpe.submit(task);
        Handler h = new Handler(Looper.getMainLooper());
        try {
            final String resp = future.get();
            h.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCompleted(resp, null);
                }
            });
        } catch (final Exception e) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCompleted(null, e);
                }
            });
        }
    }

    private String httpRequest(final HttpUriRequest request) throws Exception {
        HttpClient client = new DefaultHttpClient();
        final String resp = client.execute(request,
                new BasicResponseHandler() {
                });
        return resp;
    }
}
