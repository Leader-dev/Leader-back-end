package com.leader.api.test.util;

public class JSONErrorResponse extends JSONCodeResponse {
    public JSONErrorResponse() throws Exception {
        super(400);
    }

    public JSONErrorResponse(String error) throws Exception {
        this();
        put("error", error);
    }
}
