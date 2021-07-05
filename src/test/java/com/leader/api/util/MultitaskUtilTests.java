package com.leader.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultitaskUtilTests {

    @Test
    public void forIThrowTest() {
        Executable executable = () -> MultitaskUtil.forI(9, i -> {
            if (i == 3) {
                throw new RuntimeException();
            }
        });

        assertThrows(RuntimeException.class, executable);
    }

    @Test
    public void forEachThrowTest() {
        Executable executable = () -> MultitaskUtil.forEach(Arrays.asList(1, 2, 3, 4, 5), i -> {
            if (i == 3) {
                throw new RuntimeException();
            }
        });

        assertThrows(RuntimeException.class, executable);
    }
}
