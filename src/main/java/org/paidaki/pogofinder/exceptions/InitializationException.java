package org.paidaki.pogofinder.exceptions;

public class InitializationException extends Exception {

    public InitializationException() {
        super();
    }

    public InitializationException(String reason) {
        super(reason);
    }

    public InitializationException(Throwable exception) {
        super(exception);
    }

    public InitializationException(String reason, Throwable exception) {
        super(reason, exception);
    }
}

