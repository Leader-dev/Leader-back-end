package com.leader.api.util.response;

public class ErrorResponse extends CodeResponse {

    public static ErrorResponse error(String error) {
        return new ErrorResponse(error);
    }

    public ErrorResponse(String error) {
        super(400);
        this.append("error", error);
    }
}
