package com.leader.api.test.util;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class Util {

    public static void performRestTestAndExpectResponse(MockMvc mockMvc, String url, JSONObject res) throws Exception {
        performRestTestAndExpectResponse(mockMvc, url, null, res, null);
    }

    public static void performRestTestAndExpectResponse(MockMvc mockMvc, String url, @Nullable JSONObject req,
                                                        JSONObject res) throws Exception {
        performRestTestAndExpectResponse(mockMvc, url, req, res, null);
    }

    public static void performRestTestAndExpectResponse(MockMvc mockMvc, String url, JSONObject res,
                                                        @Nullable MockHttpSession session) throws Exception {
        performRestTestAndExpectResponse(mockMvc, url, null, res, session);
    }

    public static void performRestTestAndExpectResponse(MockMvc mockMvc, String url, @Nullable JSONObject req,
                                                        JSONObject res, @Nullable MockHttpSession session) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post(url);
        if (session != null) {
            requestBuilder = requestBuilder
                    .session(session);
        }
        if (req != null) {
            requestBuilder = requestBuilder
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(req.toString());
        }
        mockMvc.perform(requestBuilder).andExpect(
                content().json(res.toString())
        );
    }
}
