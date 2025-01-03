package org.yx.hoststack.edge.common.exception;

import lombok.Getter;

@Getter
public class InvalidHttpStatusException extends RuntimeException {
    private int status;
    private String body;
    private String traceId;

    public InvalidHttpStatusException(int status, String body, String traceId, String errorMsg) {
        super(errorMsg);
        this.status = status;
        this.body = body;
        this.traceId = traceId;
    }
}
