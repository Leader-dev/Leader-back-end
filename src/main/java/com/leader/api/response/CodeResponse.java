package com.leader.api.response;

import org.bson.Document;

public class CodeResponse extends Document {

    public CodeResponse(int code) {
        super("code", code);
    }
}
