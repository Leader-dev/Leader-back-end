package com.leader.api.util.response;

import org.bson.Document;

public class SuccessResponse extends CodeResponse {

    public SuccessResponse() {
        super(200);
    }

    public SuccessResponse(Object... data) {
        this();
        if (data.length % 2 != 0) {
            throw new RuntimeException("Invalid number of parameters");
        }
        Document dataDocument = new Document();
        for (int i = 0; i < data.length; i += 2) {
            dataDocument.append((String) data[i], data[i + 1]);
        }
        this.append("data", dataDocument);
    }
}
