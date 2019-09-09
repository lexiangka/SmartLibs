package com.wedo.utils;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Rx定时器
 * 简化使用流程的Rx定时器
 * @see Observable,Subscription
 * @since [定时器]
 */
public class RxTimer {
    private Subscription timeSub;
    private TimerCallBack timerCallBack;
    private int time;
    private int countTime;
    /**
     * 构造
     * @param time 时间（秒）
     * @param timerCallBack 回调
     * **/
    public RxTimer(int time, TimerCallBack timerCallBack){
        this.time=time;
        this.timerCallBack=timerCallBack;
    }

    /**
     * 重新开始
     * 仅重新调用 start 方法，此方法只是为明晰逻辑
     * @see #start
     * **/
    public void restart(){
        start();
    }
    /**
     * 开始计时
     * 开始时会重新刷新时间
     * **/
    public void start() {
        stop();
        if (time < 0) {
            time = 0;
        }
        countTime = time;
        timeSub=Observable.interval(0,1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<Long, Integer>() {
                    @Override
                    public Integer call(Long increaseTime) {
                        return countTime-increaseTime.intValue();
                    }
                })
                .take(countTime+1)
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        timerCallBack.OnTimeFinish();
                        stop();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("com.wedo.utils.RxTimer",e.getMessage());

                    }

                    @Override
                    public void onNext(Integer integer) {
                        timerCallBack.OnTimeTick(integer);
                    }
                });
    }

    /**
     * 停止计时
     * **/
    public void stop(){
        if(timeSub!=null&&!timeSub.isUnsubscribed()){
            timeSub.unsubscribe();
        }
    }

    /** 计时器回调接口 **/
    public interface TimerCallBack{
        /** 计时完成 **/
        void OnTimeFinish();
        /**
         * 计时中
         * @param time 倒计时时间
         * **/
        void OnTimeTick(long time);
    }
}
