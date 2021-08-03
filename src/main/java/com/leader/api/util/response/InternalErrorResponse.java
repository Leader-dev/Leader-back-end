package com.leader.api.util.response;

public class InternalErrorResponse extends CodeResponse {

    public static InternalErrorResponse internalError(String errorMessage) {
        return new InternalErrorResponse(errorMessage);
    }

    public InternalErrorResponse(String errorMessage) {
        super(500);
        this.append("message", errorMessage);
    }
}
