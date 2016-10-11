package org.paidaki.pogofinder.util.threading;

public abstract class MyRunnable implements Runnable {

    private boolean running;
    private Runnable callback = null;

    public MyRunnable() {
        super();
    }

    public MyRunnable(Runnable callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        running = true;

        try {
            runTask();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        running = false;

        if (callback != null) {
            callback.run();
        }
    }

    public abstract void runTask() throws InterruptedException;

    public boolean isRunning() {
        return running;
    }
}
