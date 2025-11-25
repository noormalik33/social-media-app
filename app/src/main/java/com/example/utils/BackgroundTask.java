package com.example.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;

public abstract class BackgroundTask {

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    public BackgroundTask() {
        onPreExecute();
    }

    private void startBackground() {
        executor.execute(() -> {
            boolean isSuccess = doInBackground();
            handler.post(() -> onPostExecute(isSuccess));
        });
    }

    public void execute() {
        startBackground();
    }

    public abstract void onPreExecute();

    public abstract boolean doInBackground();

    public abstract void onPostExecute(Boolean isExecutionSuccess);
}
