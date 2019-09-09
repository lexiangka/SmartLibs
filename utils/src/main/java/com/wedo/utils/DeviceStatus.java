package com.wedo.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.wedo.utils.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action1;

public class DeviceStatus {
     final static String TAG = DeviceStatus.class.getSimpleName();

    public static float getAvailMem(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        return (info.availMem >> 10) / 1024f;

    }

    public static float getSDCardAvailaleSize() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File path = new File(FileUtil.getSdCardPath());
            Log.v("zc", "文件目录 :" + path.getPath());
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long availableBlocks = stat.getAvailableBlocks();
            return (availableBlocks * blockSize) / 1024f / 1024f;
        }
        return 0;

    }

    public static float readUsage() {
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            String load = reader.readLine();
            // Split on one or more spaces
            String[] toks = load.split(" +");
            long idle1 = Long.parseLong(toks[4]);
            long cpu1 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            try {
                Thread.sleep(360);
            } catch (Exception e) {
                e.printStackTrace();
            }

            reader.seek(0);
            load = reader.readLine();
            reader.close();

            toks = load.split(" +");

            long idle2 = Long.parseLong(toks[4]);
            long cpu2 = Long.parseLong(toks[2]) + Long.parseLong(toks[3]) + Long.parseLong(toks[5])
                    + Long.parseLong(toks[6]) + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

            return (float) (cpu2 - cpu1) / ((cpu2 + idle2) - (cpu1 + idle1));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * Android系统（CPU/内存/存储/网络）
     * 每10秒执行一次（延迟10秒）
     *
     * @param context 上下文变量
     **/
    public static void deviceForAndroid(final Context context) {
        Observable<Long> messageObservable = Observable.interval(10, 10, TimeUnit.SECONDS);
        messageObservable.subscribe(new Action1<Long>() {
            @Override
            public void call(Long t1) {
                try {
                    float AvailMem = DeviceStatus.getAvailMem(context);
                    float freeSDsize = DeviceStatus.getSDCardAvailaleSize();
                    float cpu = DeviceStatus.readUsage();

                    Log.v("zc", "剩余内存为: " + AvailMem + "mb");
                    Log.v("zc", "剩余存储空间为: " + freeSDsize + "mb");
                    Log.v("zc", "CPU占用率为: " + cpu * 100 + "%");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.e(TAG, throwable.getMessage());
            }
        });
    }
}
