package org.yx.hoststack.center.common.exception;

import lombok.Getter;

import java.io.Serial;

@Getter
public class InvalidHttpStatusException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 7368590265883678585L;
    private final int status;
    private final String body;
    private final String traceId;

    public InvalidHttpStatusException(int status, String body, String traceId, String errorMsg) {
        super(errorMsg);
        this.status = status;
        this.body = body;
        this.traceId = traceId;
    }
}
