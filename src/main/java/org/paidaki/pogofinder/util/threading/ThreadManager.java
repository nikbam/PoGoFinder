package org.paidaki.pogofinder.util.threading;

import javafx.application.Platform;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public abstract class ThreadManager {

    private static Set<Stoppable> timers = new HashSet<>();

    public static void addThread(Stoppable timer) {
        timers.add(timer);
    }

    public static void shutdownAllThreads() {
        timers.stream().filter(t -> t != null).forEach(Stoppable::forceStop);
    }

    public static Object runAndWaitOnFXThread(Callable<Object> callable) {
        Object result = null;
        FutureTask<Object> task = new FutureTask<>(callable);

        try {
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                Platform.runLater(task);
            }
            result = task.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void runOnFXThread(Runnable callable) {
        if (Platform.isFxApplicationThread()) {
            callable.run();
        } else {
            Platform.runLater(callable);
        }
    }
}
