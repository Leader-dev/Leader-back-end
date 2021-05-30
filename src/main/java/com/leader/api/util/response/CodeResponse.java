package com.leader.api.util.response;

import org.bson.Document;

public class CodeResponse extends Document {

    public CodeResponse(int code) {
        super("code", code);
    }
}
