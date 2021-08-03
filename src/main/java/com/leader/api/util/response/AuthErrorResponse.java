package com.leader.api.util.response;

public class AuthErrorResponse extends CodeResponse {

    private static final AuthErrorResponse INSTANCE = new AuthErrorResponse();

    public static AuthErrorResponse authError() {
        return INSTANCE;
    }

    public AuthErrorResponse() {
        super(403);
    }
}
