package com.leader.api.test.util;

import org.json.JSONObject;

public class JSONCodeResponse extends JSONObject {

    public JSONCodeResponse(int code) throws Exception {
        super();
        put("code", code);
    }
}
