package com.terminus.testfaces;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Task {
    BlockingDeque<Object> workQueue = new LinkedBlockingDeque();
    private int queueSize = 30;
    private boolean isRun = false;
    private Thread myThread;
    private QueueListener listener;

    public Task(){

    }

    public Task(int queueSize){
        this.queueSize = queueSize;
    }

    public void setInfinite(){
        this.queueSize = 0;
    }

    public void setQueueListener(QueueListener listener){
        this.listener = listener;
    }

    public void start(){
        if(myThread == null) {
            isRun = true;
            myThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    while (isRun && workQueue != null) {
                        try {
                            if(listener != null) {
                                listener.onTake(workQueue.takeFirst());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
        }
        myThread.start();
    }

    public void enqueue(Object object) {
        if(workQueue != null) {
            if (queueSize != 0 && workQueue.size() > queueSize) {
                workQueue.pollFirst();//将最老的任务抛弃
            }
            workQueue.offerLast(object);
        }
    }

    public void clearTask(){
        if(workQueue != null){
            workQueue.clear();
        }
    }

    public void stop(){
        if(workQueue != null){
            workQueue.clear();
            workQueue = null;
        }
        isRun = false;
        myThread = null;
    }

    public interface QueueListener{
        void onTake(Object object);
    }
}
