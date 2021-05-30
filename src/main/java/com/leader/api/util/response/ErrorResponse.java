package com.leader.api.util.response;

public class ErrorResponse extends CodeResponse {

    public ErrorResponse() {
        super(400);
    }

    public ErrorResponse(String error) {
        this();
        this.append("error", error);
    }
}
