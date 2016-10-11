package org.paidaki.pogofinder.util.threading;

public interface Stoppable {

    void forceStop();

    default void stop() {
        forceStop();
    }
}
