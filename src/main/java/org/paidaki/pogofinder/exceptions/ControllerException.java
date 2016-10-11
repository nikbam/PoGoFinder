package org.paidaki.pogofinder.exceptions;

public class ControllerException extends Exception {

    public ControllerException() {
        super();
    }

    public ControllerException(String reason) {
        super(reason);
    }

    public ControllerException(Throwable exception) {
        super(exception);
    }

    public ControllerException(String reason, Throwable exception) {
        super(reason, exception);
    }
}
