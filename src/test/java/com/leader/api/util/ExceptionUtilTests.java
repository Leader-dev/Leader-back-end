package com.leader.api.util;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class ExceptionUtilTests {

    @Test
    public void ignoreExceptionTest() {
        assertNotNull(ExceptionUtil.ignoreException(() -> ""));
        assertNull(ExceptionUtil.ignoreException(() -> { throw new Exception(); }));
    }
}
