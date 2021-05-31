package com.leader.api.test.util;

import org.bson.Document;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Util {

    public static void assertSuccessResponse(Document response) {
        assertEquals(200, response.get("code"));
    }

    public static void assertErrorResponse(Document response, String error) {
        assertEquals(400, response.get("code"));
        assertEquals(error, response.get("error"));
    }
}
