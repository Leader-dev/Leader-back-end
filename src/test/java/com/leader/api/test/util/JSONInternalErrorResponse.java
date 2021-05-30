package com.leader.api.test.util;

public class JSONInternalErrorResponse extends JSONCodeResponse {

    public JSONInternalErrorResponse() throws Exception {
        super(500);
    }

    public JSONInternalErrorResponse(String message) throws Exception {
        this();
        put("message", message);
    }
}
