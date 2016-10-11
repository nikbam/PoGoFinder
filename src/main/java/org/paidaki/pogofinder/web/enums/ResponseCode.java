package org.paidaki.pogofinder.web.enums;

public enum ResponseCode {
    STATUS_OK(200),
    BAD_REQUEST(400),
    FORBIDDEN(403),
    UNAUTHORIZED(401),
    NOT_FOUND(404);

    public int value;

    ResponseCode(int value) {
        this.value = value;
    }

    public static ResponseCode getResponseCode(int status) {
        for (ResponseCode rc : ResponseCode.values()) {
            if (rc.value == status) {
                return rc;
            }
        }
        return null;
    }
}
