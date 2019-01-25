package com.terminus.testfaces;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by DELL on 2018/3/21.
 */

public class ThreadPoolUtils {
    private static ThreadPoolExecutor executor;

    private static ThreadPoolExecutor getThreadPool(){
        if(executor == null){
            synchronized (ThreadPoolUtils.class) {
                if(executor == null) {
                    executor = new ThreadPoolExecutor(5,15,
                            0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(),
                            new ThreadPoolExecutor.CallerRunsPolicy());
                }
            }
        }
        return executor;
    }

    public static void execute(Runnable runnable){
        getThreadPool().execute(runnable);
    }

    public static void shutdown(){
        if(executor != null){
            executor.shutdown();
            executor = null;
        }
    }

    public static void shutdownNow(){
        if(executor != null){
            executor.shutdownNow();
            executor = null;
        }
    }
}
