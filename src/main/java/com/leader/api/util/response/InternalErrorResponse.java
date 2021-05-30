package com.leader.api.util.response;

public class InternalErrorResponse extends CodeResponse {

    public InternalErrorResponse() {
        super(500);
    }

    public InternalErrorResponse(String errorMessage) {
        this();
        this.append("message", errorMessage);
    }
}
