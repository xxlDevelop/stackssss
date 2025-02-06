package org.yx.hoststack.center.common.exception;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = -4359300931593648625L;

    public UnauthorizedException() {
        super();
    }

    public UnauthorizedException(String message) {
        super(message);
    }

}
