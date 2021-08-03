package com.leader.api.util.response;

import org.bson.Document;

public class CodeResponse extends Document {

    public static CodeResponse code(int code) {
        return new CodeResponse(code);
    }

    public CodeResponse(int code) {
        super("code", code);
    }
}
