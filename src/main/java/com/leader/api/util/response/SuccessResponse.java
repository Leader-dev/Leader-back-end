package com.leader.api.util.response;

import org.bson.Document;

public class SuccessResponse extends CodeResponse {

    private static final SuccessResponse INSTANCE = new SuccessResponse();

    private static void appendValues(Document document, Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new RuntimeException("Invalid number of parameters");
        }
        for (int i = 0; i < keyValues.length; i += 2) {
            document.append((String) keyValues[i], keyValues[i + 1]);
        }
    }

    /**
     * Do not ever alter the value of this document directly through methods like
     * put, append, remove, etc.
     */
    public static SuccessResponse success() {
        return INSTANCE;
    }

    /**
     * success("flag", true, "name", "test") will give the following response:
     * {
     *     code: 200,
     *     flag: true,
     *     name: "test"
     * }
     * @param keyValues key-value pairs representing fields in response document except code: 200
     */
    public static SuccessResponse success(Object... keyValues) {
        return new SuccessResponse(keyValues);
    }

    public SuccessResponse() {
        super(200);
    }

    /**
     * success("flag", true, "name", "test") will give the following response:
     * {
     *     code: 200,
     *     flag: true,
     *     name: "test"
     * }
     * @param keyValues key-value pairs representing fields in response document except code: 200
     */
    public SuccessResponse(Object... keyValues) {
        this();
        appendValues(this, keyValues);
    }

    /**
     * success().data("flag", true, "name", "test") will give the following response:
     * {
     *     code: 200,
     *     data: {
     *         flag: true,
     *         name: "test"
     *     }
     * }
     * This method will create a field 'data' with document type besides code: 200, putting key-value pairs inside the document.
     * @param data key-value pairs representing fields in data document
     */
    public SuccessResponse data(Object... data) {
        SuccessResponse response = new SuccessResponse();
        Document dataDocument = new Document();
        appendValues(dataDocument, data);
        response.append("data", dataDocument);
        return response;
    }
}
