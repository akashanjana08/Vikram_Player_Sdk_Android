package com.multitv.multitvplayersdk.utils;

import com.google.common.util.concurrent.Monitor;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

public class PausableExecutor extends ScheduledThreadPoolExecutor {

    private boolean isPaused;

    private final Monitor monitor = new Monitor();
    private final Monitor.Guard paused = new Monitor.Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return isPaused;
        }
    };

    private final Monitor.Guard notPaused = new Monitor.Guard(monitor) {
        @Override
        public boolean isSatisfied() {
            return !isPaused;
        }
    };

    public PausableExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public PausableExecutor(int corePoolSize) {
        super(corePoolSize);
    }

    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        monitor.enterWhenUninterruptibly(notPaused);
        try {
            monitor.waitForUninterruptibly(notPaused);
        } finally {
            synchronized (monitor) {
                monitor.leave();
            }
            //monitor.leave();
        }
    }

    synchronized public void pause() {
        monitor.enterIf(notPaused);
        try {
            isPaused = true;
        } finally {
            synchronized (monitor) {
                try {
                    monitor.leave();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //monitor.leave();
        }
    }

    synchronized public void resume() {
        monitor.enterIf(paused);
        try {
            isPaused = false;
        } finally {
            synchronized (monitor) {
                try {
                    monitor.leave();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //monitor.leave();
        }
    }
}