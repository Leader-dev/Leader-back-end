package com.leader.api.util.component;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class RandomUtil {

    private final SecureRandom random = new SecureRandom();

    public double nextDouble() {
        return random.nextDouble();
    }

    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public UUID nextUUID() {
        return UUID.randomUUID();
    }
}
