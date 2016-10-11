package org.paidaki.pogofinder.exceptions;

public class NotEnoughAccountsException extends Exception {

    public NotEnoughAccountsException() {
        super();
    }

    public NotEnoughAccountsException(String reason) {
        super(reason);
    }

    public NotEnoughAccountsException(Throwable exception) {
        super(exception);
    }

    public NotEnoughAccountsException(String reason, Throwable exception) {
        super(reason, exception);
    }
}
