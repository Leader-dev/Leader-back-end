package com.leader.api.response;

public class ErrorResponse extends CodeResponse {

    public ErrorResponse() {
        super(400);
    }

    public ErrorResponse(String error) {
        this();
        this.append("error", error);
    }
}
